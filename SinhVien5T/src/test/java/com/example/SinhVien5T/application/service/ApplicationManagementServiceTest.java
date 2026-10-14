package com.example.SinhVien5T.application.service;

import com.example.SinhVien5T.application.dto.request.ApplicationEvaluateRequest;
import com.example.SinhVien5T.application.dto.request.ApplicationFilterRequest;
import com.example.SinhVien5T.application.dto.response.ApplicationDetailResponse;
import com.example.SinhVien5T.application.dto.response.ApplicationSummaryResponse;
import com.example.SinhVien5T.application.entity.Application;
import com.example.SinhVien5T.application.entity.ApplicationStatus;
import com.example.SinhVien5T.application.mapper.ApplicationMapper;
import com.example.SinhVien5T.application.repository.ApplicationRepository;
import com.example.SinhVien5T.campaign.entity.Campaign;
import com.example.SinhVien5T.common.dto.response.PageResponse;
import com.example.SinhVien5T.user.entity.Role;
import com.example.SinhVien5T.user.entity.User;
import com.example.SinhVien5T.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationManagementServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EvaluationService evaluationService;

    @Mock
    private ApplicationMapper applicationMapper;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ApplicationManagementService applicationManagementService;

    private Application application;
    private User reviewer;
    private User student;
    private Campaign campaign;

    @BeforeEach
    void setUp() {
        student = new User();
        student.setId(10L);
        student.setFirstName("Văn A");
        student.setLastName("Nguyễn");

        reviewer = new User();
        reviewer.setId(2L);
        reviewer.setRole(Role.ADMIN);

        campaign = new Campaign();
        campaign.setId(1L);
        campaign.setName("Đợt xét tuyển 2025");

        application = Application.builder()
                .id(100L)
                .student(student)
                .campaign(campaign)
                .status(ApplicationStatus.SUBMITTED)
                .submittedAt(LocalDateTime.now())
                .passedStandardsCount(5)
                .totalStandardsCount(5)
                .build();
    }

    @Test
    @DisplayName("Lấy danh sách hồ sơ phân trang thành công")
    void getApplications_Success() {
        ApplicationFilterRequest request = new ApplicationFilterRequest();
        request.setPage(0);
        request.setSize(10);

        Page<Application> page = new PageImpl<>(List.of(application));
        when(applicationRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        ApplicationSummaryResponse summaryDto = ApplicationSummaryResponse.builder().id(100L).build();
        when(applicationMapper.toSummaryResponse(application)).thenReturn(summaryDto);

        PageResponse<ApplicationSummaryResponse> response = applicationManagementService.getApplications(request);

        assertEquals(1, response.getContent().size());
        assertEquals(100L, response.getContent().get(0).getId());
    }

    @Test
    @DisplayName("Thẩm định hồ sơ: Phê duyệt (APPROVED) thành công")
    void evaluateApplication_Approve_Success() {
        ApplicationEvaluateRequest request = new ApplicationEvaluateRequest(ApplicationStatus.APPROVED, "Hồ sơ xuất sắc");

        when(applicationRepository.findById(100L)).thenReturn(Optional.of(application));
        when(userRepository.findById(2L)).thenReturn(Optional.of(reviewer));
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApplicationSummaryResponse summaryDto = ApplicationSummaryResponse.builder()
                .id(100L)
                .status(ApplicationStatus.APPROVED)
                .build();
        when(applicationMapper.toSummaryResponse(any(Application.class))).thenReturn(summaryDto);

        ApplicationSummaryResponse result = applicationManagementService.evaluateApplication(100L, request, 2L);

        assertEquals(ApplicationStatus.APPROVED, result.getStatus());
        assertEquals(ApplicationStatus.APPROVED, application.getStatus());
        assertEquals(reviewer, application.getReviewer());
        assertNotNull(application.getReviewedAt());
    }

    @Test
    @DisplayName("Thẩm định hồ sơ: Từ chối (REJECTED) không có nhận xét vẫn thành công (nhận xét là tùy chọn)")
    void evaluateApplication_Reject_WithoutComment_Success() {
        ApplicationEvaluateRequest request = new ApplicationEvaluateRequest(ApplicationStatus.REJECTED, "  ");

        when(applicationRepository.findById(100L)).thenReturn(Optional.of(application));
        when(userRepository.findById(2L)).thenReturn(Optional.of(reviewer));
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApplicationSummaryResponse summaryDto = ApplicationSummaryResponse.builder()
                .id(100L)
                .status(ApplicationStatus.REJECTED)
                .build();
        when(applicationMapper.toSummaryResponse(any(Application.class))).thenReturn(summaryDto);

        ApplicationSummaryResponse result = applicationManagementService.evaluateApplication(100L, request, 2L);

        assertEquals(ApplicationStatus.REJECTED, result.getStatus());
        assertEquals(ApplicationStatus.REJECTED, application.getStatus());
        assertNull(application.getReviewerComment());
    }
}
