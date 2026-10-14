package com.example.SinhVien5T.application.service;

import com.example.SinhVien5T.application.dto.response.StandardEvaluationResult;
import com.example.SinhVien5T.campaign.entity.Campaign;
import com.example.SinhVien5T.campaign.entity.Criteria;
import com.example.SinhVien5T.campaign.entity.Standard;
import com.example.SinhVien5T.campaign.repository.CampaignRepository;
import com.example.SinhVien5T.evidence.entity.Evidence;
import com.example.SinhVien5T.evidence.entity.EvidenceStatus;
import com.example.SinhVien5T.evidence.entity.EvidenceType;
import com.example.SinhVien5T.evidence.repository.EvidenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EvaluationServiceTest {

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private EvidenceRepository evidenceRepository;

    @InjectMocks
    private EvaluationService evaluationService;

    private Campaign campaign;
    private Standard standard;
    private Criteria mandatoryCrit1;
    private Criteria mandatoryCrit2;
    private Criteria optionalCrit1;
    private Criteria optionalCrit2;

    @BeforeEach
    void setUp() {
        campaign = new Campaign();
        campaign.setId(1L);
        campaign.setName("Đợt xét tuyển 2025");

        standard = new Standard();
        standard.setId(10L);
        standard.setName("Đạo đức tốt");
        standard.setCampaign(campaign);

        Set<Standard> standards = new LinkedHashSet<>();
        standards.add(standard);
        campaign.setStandards(standards);

        mandatoryCrit1 = Criteria.builder()
                .id(101L)
                .name("Điểm rèn luyện >= 90")
                .isMandatory(true)
                .standard(standard)
                .evidenceType(EvidenceType.IMAGE)
                .build();

        mandatoryCrit2 = Criteria.builder()
                .id(102L)
                .name("Không vi phạm kỷ luật")
                .isMandatory(true)
                .standard(standard)
                .evidenceType(EvidenceType.NONE) // Hệ thống tự xác nhận
                .build();

        optionalCrit1 = Criteria.builder()
                .id(103L)
                .name("Tham gia Ánh sáng soi đường")
                .isMandatory(false)
                .standard(standard)
                .evidenceType(EvidenceType.IMAGE)
                .build();

        optionalCrit2 = Criteria.builder()
                .id(104L)
                .name("Giấy khen Đoàn trường")
                .isMandatory(false)
                .standard(standard)
                .evidenceType(EvidenceType.IMAGE)
                .build();
    }

    @Test
    @DisplayName("Đạt tiêu chuẩn khi đạt 100% tiêu chí bắt buộc và ít nhất 1 tiêu chí tự chọn")
    void evaluateStandards_Success() {
        List<Criteria> allCriteria = List.of(mandatoryCrit1, mandatoryCrit2, optionalCrit1, optionalCrit2);

        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(campaignRepository.findAllCriteria(1L)).thenReturn(allCriteria);

        // Sinh viên nộp minh chứng cho mandatoryCrit1 (APPROVED), và optionalCrit1 (APPROVED)
        Evidence ev1 = Evidence.builder().id(1L).criteria(mandatoryCrit1).status(EvidenceStatus.APPROVED).build();
        Evidence evOptional = Evidence.builder().id(2L).criteria(optionalCrit1).status(EvidenceStatus.APPROVED).build();

        when(evidenceRepository.findByUserAndCampaign(100L, 1L)).thenReturn(List.of(ev1, evOptional));

        List<StandardEvaluationResult> results = evaluationService.evaluateStandardsSummary(100L, 1L);

        assertEquals(1, results.size());
        StandardEvaluationResult res = results.get(0);
        assertTrue(res.getIsPassed());
        assertTrue(res.getIsMandatoryPassed());
        assertEquals(2, res.getMandatoryPassedCount());
        assertEquals(2, res.getMandatoryTotalCount());
        assertTrue(res.getIsOptionalPassed());
        assertEquals(1, res.getOptionalPassedCount());
        assertEquals(2, res.getOptionalTotalCount());
    }

    @Test
    @DisplayName("Không đạt tiêu chuẩn khi thiếu tiêu chí bắt buộc")
    void evaluateStandards_MissingMandatory() {
        List<Criteria> allCriteria = List.of(mandatoryCrit1, mandatoryCrit2, optionalCrit1, optionalCrit2);

        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(campaignRepository.findAllCriteria(1L)).thenReturn(allCriteria);

        // Sinh viên không nộp mandatoryCrit1, chỉ nộp optionalCrit1
        Evidence evOptional = Evidence.builder().id(2L).criteria(optionalCrit1).status(EvidenceStatus.APPROVED).build();
        when(evidenceRepository.findByUserAndCampaign(100L, 1L)).thenReturn(List.of(evOptional));

        List<StandardEvaluationResult> results = evaluationService.evaluateStandardsSummary(100L, 1L);

        StandardEvaluationResult res = results.get(0);
        assertFalse(res.getIsPassed());
        assertFalse(res.getIsMandatoryPassed());
        assertEquals(1, res.getMandatoryPassedCount()); // chỉ có mandatoryCrit2 (NONE) đạt
        assertEquals(2, res.getMandatoryTotalCount());
    }

    @Test
    @DisplayName("Không đạt tiêu chuẩn khi không đạt bất kỳ tiêu chí tự chọn nào")
    void evaluateStandards_MissingOptional() {
        List<Criteria> allCriteria = List.of(mandatoryCrit1, mandatoryCrit2, optionalCrit1, optionalCrit2);

        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(campaignRepository.findAllCriteria(1L)).thenReturn(allCriteria);

        // Sinh viên chỉ nộp mandatoryCrit1, không nộp tiêu chí tự chọn nào
        Evidence ev1 = Evidence.builder().id(1L).criteria(mandatoryCrit1).status(EvidenceStatus.APPROVED).build();
        when(evidenceRepository.findByUserAndCampaign(100L, 1L)).thenReturn(List.of(ev1));

        List<StandardEvaluationResult> results = evaluationService.evaluateStandardsSummary(100L, 1L);

        StandardEvaluationResult res = results.get(0);
        assertFalse(res.getIsPassed());
        assertTrue(res.getIsMandatoryPassed());
        assertFalse(res.getIsOptionalPassed());
        assertEquals(0, res.getOptionalPassedCount());
        assertEquals(2, res.getOptionalTotalCount());
    }

    @Test
    @DisplayName("Đa minh chứng cho 1 tiêu chí: Có 1 minh chứng APPROVED và 1 minh chứng REJECTED thì vẫn ĐẠT")
    void evaluateCriteria_MultipleEvidences_OneApproved() {
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(campaignRepository.findAllCriteria(1L)).thenReturn(List.of(mandatoryCrit1, mandatoryCrit2));

        // 2 minh chứng cho mandatoryCrit1
        Evidence evRejected = Evidence.builder().id(1L).criteria(mandatoryCrit1).status(EvidenceStatus.REJECTED).build();
        Evidence evApproved = Evidence.builder().id(2L).criteria(mandatoryCrit1).status(EvidenceStatus.APPROVED).build();

        when(evidenceRepository.findByUserAndCampaign(100L, 1L)).thenReturn(List.of(evRejected, evApproved));

        List<StandardEvaluationResult> results = evaluationService.evaluateStandardsSummary(100L, 1L);
        StandardEvaluationResult res = results.get(0);
        assertTrue(res.getIsPassed());
    }
}
