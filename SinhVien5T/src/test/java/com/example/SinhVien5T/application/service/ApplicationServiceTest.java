package com.example.SinhVien5T.application.service;

import com.example.SinhVien5T.application.dto.request.SubmitApplicationRequest;
import com.example.SinhVien5T.application.dto.response.ApplicationDetailResponse;
import com.example.SinhVien5T.application.dto.response.ApplicationPreviewResponse;
import com.example.SinhVien5T.application.dto.response.StandardEvaluationResult;
import com.example.SinhVien5T.application.dto.response.StudentProfileSnapshot;
import com.example.SinhVien5T.application.entity.Application;
import com.example.SinhVien5T.application.entity.ApplicationStatus;
import com.example.SinhVien5T.application.mapper.ApplicationMapper;
import com.example.SinhVien5T.application.repository.ApplicationRepository;
import com.example.SinhVien5T.campaign.entity.Campaign;
import com.example.SinhVien5T.campaign.repository.CampaignRepository;
import com.example.SinhVien5T.campaign.service.CampaignService;
import com.example.SinhVien5T.common.exception.ConflictException;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CampaignService campaignService;

    @Mock
    private EvaluationService evaluationService;

    @Mock
    private ApplicationMapper applicationMapper;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ApplicationService applicationService;

    private Campaign campaign;
    private User student;

    @BeforeEach
    void setUp() {
        campaign = new Campaign();
        campaign.setId(1L);
        campaign.setName("Đợt xét tuyển 2025");

        student = new User();
        student.setId(10L);
        student.setProfileCompleted(true);
    }

    @Test
    @DisplayName("Nộp hồ sơ thành công khi thỏa mãn toàn bộ điều kiện")
    void submitApplication_Success() throws Exception {
        SubmitApplicationRequest request = new SubmitApplicationRequest(1L);

        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(campaignService.isCampaignOpen(campaign)).thenReturn(true);
        when(userRepository.findById(10L)).thenReturn(Optional.of(student));
        when(applicationRepository.findByStudentIdAndCampaignId(10L, 1L)).thenReturn(Optional.empty());

        StandardEvaluationResult res = StandardEvaluationResult.builder()
                .standardId(1L)
                .isPassed(true)
                .build();
        when(evaluationService.evaluateStandardsSummary(10L, 1L)).thenReturn(List.of(res));

        StudentProfileSnapshot snapshot = StudentProfileSnapshot.builder().studentId(10L).fullName("Nguyễn Văn A").build();
        when(applicationMapper.toProfileSnapshot(student)).thenReturn(snapshot);
        when(objectMapper.writeValueAsString(snapshot)).thenReturn("{\"fullName\":\"Nguyễn Văn A\"}");

        Application savedApp = Application.builder()
                .id(100L)
                .student(student)
                .campaign(campaign)
                .status(ApplicationStatus.SUBMITTED)
                .submittedAt(LocalDateTime.now())
                .passedStandardsCount(1)
                .totalStandardsCount(1)
                .profileSnapshot("{\"fullName\":\"Nguyễn Văn A\"}")
                .build();
        when(applicationRepository.save(any(Application.class))).thenReturn(savedApp);
        when(evaluationService.evaluateStandardsDetail(10L, 1L)).thenReturn(Collections.emptyList());

        ApplicationDetailResponse response = applicationService.submitApplication(request, 10L);

        assertNotNull(response);
        assertEquals(ApplicationStatus.SUBMITTED, response.getStatus());
        assertEquals(1, response.getPassedStandardsCount());
        verify(applicationRepository).save(any(Application.class));
    }

    @Test
    @DisplayName("Ném ConflictException khi đợt xét tuyển không mở")
    void submitApplication_CampaignNotOpen() {
        SubmitApplicationRequest request = new SubmitApplicationRequest(1L);

        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(campaignService.isCampaignOpen(campaign)).thenReturn(false);

        assertThrows(ConflictException.class, () -> applicationService.submitApplication(request, 10L));
        verify(applicationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Ném ConflictException khi profile sinh viên chưa hoàn thiện")
    void submitApplication_ProfileNotCompleted() {
        student.setProfileCompleted(false);
        SubmitApplicationRequest request = new SubmitApplicationRequest(1L);

        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(campaignService.isCampaignOpen(campaign)).thenReturn(true);
        when(userRepository.findById(10L)).thenReturn(Optional.of(student));

        assertThrows(ConflictException.class, () -> applicationService.submitApplication(request, 10L));
        verify(applicationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Ném ConflictException khi chưa đạt đủ tiêu chuẩn")
    void submitApplication_StandardsNotPassed() {
        SubmitApplicationRequest request = new SubmitApplicationRequest(1L);

        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(campaignService.isCampaignOpen(campaign)).thenReturn(true);
        when(userRepository.findById(10L)).thenReturn(Optional.of(student));
        when(applicationRepository.findByStudentIdAndCampaignId(10L, 1L)).thenReturn(Optional.empty());

        StandardEvaluationResult res = StandardEvaluationResult.builder()
                .standardId(1L)
                .isPassed(false) // Không đạt
                .build();
        when(evaluationService.evaluateStandardsSummary(10L, 1L)).thenReturn(List.of(res));

        assertThrows(ConflictException.class, () -> applicationService.submitApplication(request, 10L));
        verify(applicationRepository, never()).save(any());
    }
}
