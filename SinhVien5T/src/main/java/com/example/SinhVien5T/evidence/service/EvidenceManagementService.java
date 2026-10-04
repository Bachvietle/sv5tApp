package com.example.SinhVien5T.evidence.service;

import com.example.SinhVien5T.evidence.repository.EvidenceRepository;
import com.example.SinhVien5T.common.dto.response.PageResponse;
import com.example.SinhVien5T.evidence.dto.request.EvidenceFilterRequest;
import com.example.SinhVien5T.evidence.dto.request.EvidenceReviewRequest;
import com.example.SinhVien5T.evidence.dto.response.EvidenceAdminDTO;
import com.example.SinhVien5T.evidence.entity.Evidence;
import com.example.SinhVien5T.evidence.entity.EvidenceStatus;
import com.example.SinhVien5T.evidence.mapper.EvidenceMapper;
import com.example.SinhVien5T.evidence.repository.specification.EvidenceSpecification;
import com.example.SinhVien5T.user.entity.User;
import com.example.SinhVien5T.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EvidenceManagementService {

    private final EvidenceRepository evidenceRepository;
    private final UserRepository userRepository;
    private final EvidenceMapper evidenceMapper;

    @Transactional(readOnly = true)
    public PageResponse<EvidenceAdminDTO> getEvidences(EvidenceFilterRequest request) {
        String sortBy = request.getSortBy() != null ? request.getSortBy() : "submittedAt";
        Sort.Direction direction = "ASC".equalsIgnoreCase(request.getSortDirection()) ? Sort.Direction.ASC : Sort.Direction.DESC;
        
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), Sort.by(direction, sortBy));
        Specification<Evidence> spec = EvidenceSpecification.filterEvidences(request);

        Page<Evidence> evidencePage = evidenceRepository.findAll(spec, pageable);

        Page<EvidenceAdminDTO> dtoPage = evidencePage.map(evidenceMapper::toEvidenceAdminDTO);

        return new PageResponse<>(
                dtoPage.getContent(),
                dtoPage.getNumber(),
                dtoPage.getSize(),
                dtoPage.getTotalElements(),
                dtoPage.getTotalPages(),
                dtoPage.isLast()
        );
    }

    @Transactional
    public EvidenceAdminDTO reviewEvidence(Long evidenceId, EvidenceReviewRequest request, Long reviewerId) {
        Evidence evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy minh chứng"));

        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người duyệt"));

        if (request.getStatus() == EvidenceStatus.PENDING) {
            throw new RuntimeException("Không thể set trạng thái thành PENDING");
        }

        if ((request.getStatus() == EvidenceStatus.REJECTED || request.getStatus() == EvidenceStatus.NEEDS_MODIFICATION)
                && (request.getReviewerComment() == null || request.getReviewerComment().trim().isEmpty())) {
            throw new RuntimeException("Vui lòng nhập lý do/ghi chú");
        }

        evidence.setStatus(request.getStatus());
        evidence.setReviewerComment(request.getReviewerComment());
        evidence.setReviewer(reviewer);
        evidence.setReviewedAt(LocalDateTime.now());

        evidenceRepository.save(evidence);

        return evidenceMapper.toEvidenceAdminDTO(evidence);
    }
}
