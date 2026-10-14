package com.example.SinhVien5T.evidence.service;

import com.example.SinhVien5T.application.entity.ApplicationStatus;
import com.example.SinhVien5T.application.repository.ApplicationRepository;
import com.example.SinhVien5T.campaign.entity.Campaign;
import com.example.SinhVien5T.campaign.entity.Criteria;
import com.example.SinhVien5T.campaign.repository.CriteriaRepository;
import com.example.SinhVien5T.common.exception.ResourceNotFoundException;
import com.example.SinhVien5T.evidence.dto.UploadEvidenceRequest;
import com.example.SinhVien5T.evidence.dto.UploadEvidenceResponse;
import com.example.SinhVien5T.evidence.entity.Evidence;
import com.example.SinhVien5T.evidence.entity.EvidenceStatus;
import com.example.SinhVien5T.evidence.entity.EvidenceType;
import com.example.SinhVien5T.evidence.exception.EvidenceLockedException;
import com.example.SinhVien5T.evidence.exception.EvidenceNotFoundException;
import com.example.SinhVien5T.evidence.exception.EvidencePermissionDeniedException;
import com.example.SinhVien5T.evidence.exception.EvidenceValidationException;
import com.example.SinhVien5T.evidence.repository.EvidenceRepository;
import com.example.SinhVien5T.user.entity.User;
import com.example.SinhVien5T.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor 
public class EvidenceService {

    private final FileStorageService fileStorageService;
    private final CriteriaRepository criteriaRepository;
    private final UserRepository userRepository;
    private final EvidenceRepository evidenceRepository;
    private final ApplicationRepository applicationRepository;

    @Transactional
    public UploadEvidenceResponse uploadFile(UploadEvidenceRequest request, Long userId) throws IOException {

        Criteria criteria = criteriaRepository.findById(request.getCriteriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tiêu chí"));

        // Kiểm tra tính hợp lệ của tiêu chí khi nộp minh chứng
        if (criteria.getEvidenceType() == EvidenceType.NONE) {
            throw new EvidenceValidationException("Tiêu chí này được hệ thống tự động xác nhận, không yêu cầu nộp minh chứng");
        }
        if (criteria.getCriteriaList() != null && !criteria.getCriteriaList().isEmpty()) {
            throw new EvidenceValidationException("Không thể nộp minh chứng trực tiếp cho tiêu chí nhóm cha, vui lòng chọn tiêu chí con");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin sinh viên"));

        // Khóa không cho sửa/thêm minh chứng nếu hồ sơ của đợt này đã được nộp/phê duyệt
        Campaign campaign = getCampaignFromCriteria(criteria);
        if (campaign != null && applicationRepository.existsByStudentIdAndCampaignIdAndStatusIn(
                userId, campaign.getId(), List.of(ApplicationStatus.SUBMITTED, ApplicationStatus.APPROVED))) {
            throw new EvidenceLockedException("Hồ sơ xét tuyển của đợt này đã được nộp/phê duyệt, không thể chỉnh sửa hoặc tải thêm minh chứng");
        }

        // Nếu request có evidenceId: Sinh viên muốn sửa/nộp lại một minh chứng cụ thể đã có
        if (request.getEvidenceId() != null) {
            Evidence evidenceExist = evidenceRepository.findById(request.getEvidenceId())
                    .orElseThrow(() -> new EvidenceNotFoundException("Không tìm thấy minh chứng cần cập nhật với ID: " + request.getEvidenceId()));

            if (!evidenceExist.getUser().getId().equals(userId)) {
                throw new EvidencePermissionDeniedException("Bạn không có quyền chỉnh sửa minh chứng này");
            }

            if (!evidenceExist.getCriteria().getId().equals(criteria.getId())) {
                throw new EvidenceValidationException("Minh chứng được chọn không thuộc tiêu chí đã chỉ định");
            }

            if (evidenceExist.getStatus() == EvidenceStatus.APPROVED) {
                throw new EvidenceValidationException("Minh chứng này đã được phê duyệt đạt, không thể chỉnh sửa lại");
            }

            fileStorageService.deleteFile(evidenceExist.getEvidenceUrl());

            String evidenceUrl = fileStorageService.storeFile(request.getFile());

            evidenceExist.setEvidenceUrl(evidenceUrl);
            evidenceExist.setStatus(EvidenceStatus.PENDING);
            evidenceExist.setSubmittedAt(LocalDateTime.now());
            evidenceExist.setSubmissionCount(evidenceExist.getSubmissionCount() != null ? evidenceExist.getSubmissionCount() + 1 : 2);
            evidenceExist.setReviewer(null);
            evidenceExist.setReviewedAt(null);
            if (request.getDescription() != null && !request.getDescription().trim().isEmpty()) {
                evidenceExist.setDescription(request.getDescription().trim());
            }

            evidenceRepository.save(evidenceExist);

            return UploadEvidenceResponse.builder()
                    .evidenceId(evidenceExist.getId())
                    .criteriaId(criteria.getId())
                    .evidenceUrl(evidenceUrl)
                    .status(EvidenceStatus.PENDING)
                    .description(evidenceExist.getDescription())
                    .build();
        } else {
            // Nộp mới một minh chứng cho tiêu chí này (hỗ trợ nhiều minh chứng)
            String evidenceUrl = fileStorageService.storeFile(request.getFile());

            String desc = request.getDescription() != null ? request.getDescription().trim() : "";

            Evidence evidence = Evidence.builder()
                    .criteria(criteria)
                    .user(user)
                    .evidenceUrl(evidenceUrl)
                    .status(EvidenceStatus.PENDING)
                    .submittedAt(LocalDateTime.now())
                    .submissionCount(1)
                    .reviewerComment(null)
                    .description(desc)
                    .build();

            Evidence evidenceSave = evidenceRepository.save(evidence);

            return UploadEvidenceResponse.builder()
                    .evidenceId(evidenceSave.getId())
                    .criteriaId(criteria.getId())
                    .evidenceUrl(evidenceUrl)
                    .status(EvidenceStatus.PENDING)
                    .description(desc)
                    .build();
        }
    }

    @Transactional
    public void deleteEvidence(Long evidenceId, Long userId) {
        Evidence evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new EvidenceNotFoundException("Không tìm thấy minh chứng với ID: " + evidenceId));

        if (!evidence.getUser().getId().equals(userId)) {
            throw new EvidencePermissionDeniedException("Bạn không có quyền xóa minh chứng này");
        }

        Campaign campaign = getCampaignFromCriteria(evidence.getCriteria());

        if (campaign != null && applicationRepository.existsByStudentIdAndCampaignIdAndStatusIn(
                userId, campaign.getId(), List.of(ApplicationStatus.SUBMITTED, ApplicationStatus.APPROVED))) {
            throw new EvidenceLockedException("Hồ sơ xét tuyển của đợt này đã được nộp/phê duyệt, không thể xóa minh chứng");
        }

        fileStorageService.deleteFile(evidence.getEvidenceUrl());
        evidenceRepository.delete(evidence);
    }

    /**
     * Truy vết ngược lên cây tiêu chí cha để tìm đợt xét tuyển (Campaign).
     * Hỗ trợ cho cả tiêu chí gốc (parent == null) lẫn tiêu chí con (parent != null).
     */
    private Campaign getCampaignFromCriteria(Criteria criteria) {
        Criteria current = criteria;
        while (current != null) {
            if (current.getStandard() != null && current.getStandard().getCampaign() != null) {
                return current.getStandard().getCampaign();
            }
            current = current.getParent();
        }
        return null;
    }
}
