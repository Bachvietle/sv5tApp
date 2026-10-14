package com.example.SinhVien5T.application.service;

import com.example.SinhVien5T.application.dto.request.SubmitApplicationRequest;
import com.example.SinhVien5T.application.dto.response.ApplicationDetailResponse;
import com.example.SinhVien5T.application.dto.response.ApplicationPreviewResponse;
import com.example.SinhVien5T.application.dto.response.ApplicationStandardDetailResponse;
import com.example.SinhVien5T.application.dto.response.StandardEvaluationResult;
import com.example.SinhVien5T.application.dto.response.StudentProfileSnapshot;
import com.example.SinhVien5T.application.entity.Application;
import com.example.SinhVien5T.application.entity.ApplicationStatus;
import com.example.SinhVien5T.application.exception.ApplicationNotFoundException;
import com.example.SinhVien5T.application.mapper.ApplicationMapper;
import com.example.SinhVien5T.application.repository.ApplicationRepository;
import com.example.SinhVien5T.campaign.entity.Campaign;
import com.example.SinhVien5T.campaign.repository.CampaignRepository;
import com.example.SinhVien5T.campaign.service.CampaignService;
import com.example.SinhVien5T.common.exception.ConflictException;
import com.example.SinhVien5T.common.exception.ResourceNotFoundException;
import com.example.SinhVien5T.user.entity.User;
import com.example.SinhVien5T.user.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final CampaignRepository campaignRepository;
    private final UserRepository userRepository;
    private final CampaignService campaignService;
    private final EvaluationService evaluationService;
    private final ApplicationMapper applicationMapper;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public ApplicationPreviewResponse previewApplication(Long campaignId, Long userId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt xét tuyển"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin sinh viên"));

        List<StandardEvaluationResult> standards = evaluationService.evaluateStandardsSummary(userId, campaignId);

        int totalStandards = standards.size();
        int passedStandards = (int) standards.stream().filter(StandardEvaluationResult::getIsPassed).count();

        boolean isProfileCompleted = user.isProfileCompleted();
        boolean isEligible = isProfileCompleted && (passedStandards == totalStandards) && totalStandards > 0;

        Optional<Application> existingApp = applicationRepository.findByStudentIdAndCampaignId(userId, campaignId);

        return ApplicationPreviewResponse.builder()
                .campaignId(campaign.getId())
                .campaignName(campaign.getName())
                .passedStandardsCount(passedStandards)
                .totalStandardsCount(totalStandards)
                .isEligibleToSubmit(isEligible)
                .isProfileCompleted(isProfileCompleted)
                .alreadySubmitted(existingApp.isPresent())
                .currentApplicationStatus(existingApp.map(Application::getStatus).orElse(null))
                .standards(standards)
                .build();
    }

    @Transactional
    public ApplicationDetailResponse submitApplication(SubmitApplicationRequest request, Long userId) {
        Campaign campaign = campaignRepository.findById(request.getCampaignId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt xét tuyển"));

        if (!campaignService.isCampaignOpen(campaign)) {
            throw new ConflictException("Đợt xét tuyển hiện không mở nhận hồ sơ");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin sinh viên"));

        if (!user.isProfileCompleted()) {
            throw new ConflictException("Vui lòng hoàn thiện hồ sơ cá nhân trước khi nộp xét duyệt");
        }

        Optional<Application> existingAppOpt = applicationRepository.findByStudentIdAndCampaignId(userId, campaign.getId());
        if (existingAppOpt.isPresent()) {
            Application existing = existingAppOpt.get();
            if (existing.getStatus() == ApplicationStatus.SUBMITTED || existing.getStatus() == ApplicationStatus.APPROVED) {
                throw new ConflictException("Bạn đã nộp hồ sơ cho đợt xét tuyển này rồi");
            }
        }

        List<StandardEvaluationResult> standards = evaluationService.evaluateStandardsSummary(userId, campaign.getId());
        int totalStandards = standards.size();
        int passedStandards = (int) standards.stream().filter(StandardEvaluationResult::getIsPassed).count();

        if (passedStandards < totalStandards || totalStandards == 0) {
            throw new ConflictException("Hồ sơ chưa đạt đủ " + totalStandards + "/" + totalStandards + " tiêu chuẩn để nộp");
        }

        // Chụp snapshot thông tin sinh viên tại thời điểm nộp
        StudentProfileSnapshot snapshot = applicationMapper.toProfileSnapshot(user);
        String snapshotJson;
        try {
            snapshotJson = objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Lỗi lưu trữ thông tin hồ sơ: " + e.getMessage());
        }

        Application application;
        if (existingAppOpt.isPresent()) {
            application = existingAppOpt.get();
            application.setStatus(ApplicationStatus.SUBMITTED);
            application.setSubmittedAt(LocalDateTime.now());
            application.setPassedStandardsCount(passedStandards);
            application.setTotalStandardsCount(totalStandards);
            application.setProfileSnapshot(snapshotJson);
            application.setReviewer(null);
            application.setReviewedAt(null);
            application.setReviewerComment(null);
        } else {
            application = Application.builder()
                    .student(user)
                    .campaign(campaign)
                    .status(ApplicationStatus.SUBMITTED)
                    .submittedAt(LocalDateTime.now())
                    .passedStandardsCount(passedStandards)
                    .totalStandardsCount(totalStandards)
                    .profileSnapshot(snapshotJson)
                    .build();
        }

        Application savedApp = applicationRepository.save(application);

        List<ApplicationStandardDetailResponse> details = evaluationService.evaluateStandardsDetail(userId, campaign.getId());

        return buildApplicationDetailResponse(savedApp, snapshot, details);
    }

    @Transactional(readOnly = true)
    public ApplicationDetailResponse getMyApplication(Long campaignId, Long userId) {
        Application application = applicationRepository.findByStudentIdAndCampaignId(userId, campaignId)
                .orElseThrow(() -> new ApplicationNotFoundException("Bạn chưa nộp hồ sơ cho đợt xét tuyển này"));

        StudentProfileSnapshot snapshot = parseProfileSnapshot(application.getProfileSnapshot());
        List<ApplicationStandardDetailResponse> standards = evaluationService.evaluateStandardsDetail(userId, campaignId);

        return buildApplicationDetailResponse(application, snapshot, standards);
    }

    private StudentProfileSnapshot parseProfileSnapshot(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, StudentProfileSnapshot.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private ApplicationDetailResponse buildApplicationDetailResponse(
            Application app,
            StudentProfileSnapshot profile,
            List<ApplicationStandardDetailResponse> standards) {

        String reviewerName = applicationMapper.buildFullName(app.getReviewer());

        return ApplicationDetailResponse.builder()
                .id(app.getId())
                .campaignId(app.getCampaign().getId())
                .campaignName(app.getCampaign().getName())
                .status(app.getStatus())
                .submittedAt(app.getSubmittedAt())
                .passedStandardsCount(app.getPassedStandardsCount())
                .totalStandardsCount(app.getTotalStandardsCount())
                .reviewerId(app.getReviewer() != null ? app.getReviewer().getId() : null)
                .reviewerName(reviewerName)
                .reviewedAt(app.getReviewedAt())
                .reviewerComment(app.getReviewerComment())
                .studentProfile(profile)
                .standards(standards)
                .build();
    }
}
