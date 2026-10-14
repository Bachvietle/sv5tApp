package com.example.SinhVien5T.application.service;

import com.example.SinhVien5T.application.dto.request.ApplicationEvaluateRequest;
import com.example.SinhVien5T.application.dto.request.ApplicationFilterRequest;
import com.example.SinhVien5T.application.dto.response.ApplicationDetailResponse;
import com.example.SinhVien5T.application.dto.response.ApplicationStandardDetailResponse;
import com.example.SinhVien5T.application.dto.response.ApplicationSummaryResponse;
import com.example.SinhVien5T.application.dto.response.StudentProfileSnapshot;
import com.example.SinhVien5T.application.entity.Application;
import com.example.SinhVien5T.application.entity.ApplicationStatus;
import com.example.SinhVien5T.application.exception.ApplicationNotFoundException;
import com.example.SinhVien5T.application.exception.ApplicationStatusInvalidException;
import com.example.SinhVien5T.application.mapper.ApplicationMapper;
import com.example.SinhVien5T.application.repository.ApplicationRepository;
import com.example.SinhVien5T.application.repository.specification.ApplicationSpecification;
import com.example.SinhVien5T.common.dto.response.PageResponse;
import com.example.SinhVien5T.common.exception.ResourceNotFoundException;
import com.example.SinhVien5T.user.entity.User;
import com.example.SinhVien5T.user.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationManagementService {

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final EvaluationService evaluationService;
    private final ApplicationMapper applicationMapper;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public PageResponse<ApplicationSummaryResponse> getApplications(ApplicationFilterRequest request) {
        String sortBy = request.getSortBy() != null ? request.getSortBy() : "submittedAt";
        Sort.Direction direction = "ASC".equalsIgnoreCase(request.getSortDirection()) ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), Sort.by(direction, sortBy));
        Specification<Application> spec = ApplicationSpecification.filterApplications(request);

        Page<Application> page = applicationRepository.findAll(spec, pageable);
        Page<ApplicationSummaryResponse> dtoPage = page.map(applicationMapper::toSummaryResponse);

        return new PageResponse<>(
                dtoPage.getContent(),
                dtoPage.getNumber(),
                dtoPage.getSize(),
                dtoPage.getTotalElements(),
                dtoPage.getTotalPages(),
                dtoPage.isLast()
        );
    }

    @Transactional(readOnly = true)
    public ApplicationDetailResponse getApplicationDetail(Long applicationId) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ApplicationNotFoundException("Không tìm thấy hồ sơ xét tuyển"));

        StudentProfileSnapshot snapshot = parseProfileSnapshot(app.getProfileSnapshot());
        if (snapshot == null && app.getStudent() != null) {
            snapshot = applicationMapper.toProfileSnapshot(app.getStudent());
        }

        List<ApplicationStandardDetailResponse> standards = evaluationService.evaluateStandardsDetail(
                app.getStudent().getId(), app.getCampaign().getId());

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
                .studentProfile(snapshot)
                .standards(standards)
                .build();
    }

    @Transactional
    public ApplicationSummaryResponse evaluateApplication(Long applicationId, ApplicationEvaluateRequest request, Long reviewerId) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ApplicationNotFoundException("Không tìm thấy hồ sơ xét tuyển"));

        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cán bộ thẩm định"));

        if (request.getStatus() != ApplicationStatus.APPROVED && request.getStatus() != ApplicationStatus.REJECTED) {
            throw new ApplicationStatusInvalidException("Trạng thái đánh giá chỉ có thể là APPROVED hoặc REJECTED");
        }

        app.setStatus(request.getStatus());
        app.setReviewer(reviewer);
        app.setReviewedAt(LocalDateTime.now());
        String comment = (request.getReviewerComment() != null && !request.getReviewerComment().trim().isEmpty())
                ? request.getReviewerComment().trim() : null;
        app.setReviewerComment(comment);

        Application saved = applicationRepository.save(app);
        return applicationMapper.toSummaryResponse(saved);
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
}
