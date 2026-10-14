package com.example.SinhVien5T.evidence.service;

import com.example.SinhVien5T.application.entity.ApplicationStatus;
import com.example.SinhVien5T.application.repository.ApplicationRepository;
import com.example.SinhVien5T.campaign.entity.Campaign;
import com.example.SinhVien5T.campaign.entity.Criteria;
import com.example.SinhVien5T.campaign.entity.Standard;
import com.example.SinhVien5T.campaign.repository.CriteriaRepository;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EvidenceServiceTest {

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private CriteriaRepository criteriaRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EvidenceRepository evidenceRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @InjectMocks
    private EvidenceService evidenceService;

    private User student;
    private Campaign campaign;
    private Standard standard;
    private Criteria parentCriteria;
    private Criteria childCriteria;
    private MockMultipartFile mockFile;

    @BeforeEach
    void setUp() {
        student = new User();
        student.setId(10L);

        campaign = new Campaign();
        campaign.setId(1L);

        standard = new Standard();
        standard.setId(5L);
        standard.setCampaign(campaign);

        parentCriteria = Criteria.builder()
                .id(100L)
                .name("Tiêu chí cha")
                .standard(standard)
                .build();

        childCriteria = Criteria.builder()
                .id(101L)
                .name("Tiêu chí con")
                .parent(parentCriteria)
                .evidenceType(EvidenceType.IMAGE)
                .build();

        parentCriteria.setCriteriaList(Set.of(childCriteria));

        mockFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", "image content".getBytes());
    }

    @Test
    @DisplayName("Nộp minh chứng mới cho tiêu chí con thành công")
    void uploadFile_NewEvidence_Success() throws IOException {
        UploadEvidenceRequest request = new UploadEvidenceRequest();
        request.setCriteriaId(101L);
        request.setFile(mockFile);
        request.setDescription("Minh chứng giải thưởng");

        when(criteriaRepository.findById(101L)).thenReturn(Optional.of(childCriteria));
        when(userRepository.findById(10L)).thenReturn(Optional.of(student));
        when(applicationRepository.existsByStudentIdAndCampaignIdAndStatusIn(eq(10L), eq(1L), any())).thenReturn(false);
        when(fileStorageService.storeFile(mockFile)).thenReturn("http://localhost:8080/evidence/uploads/test.jpg");

        Evidence saved = Evidence.builder()
                .id(500L)
                .criteria(childCriteria)
                .user(student)
                .evidenceUrl("http://localhost:8080/evidence/uploads/test.jpg")
                .status(EvidenceStatus.PENDING)
                .description("Minh chứng giải thưởng")
                .build();
        when(evidenceRepository.save(any(Evidence.class))).thenReturn(saved);

        UploadEvidenceResponse response = evidenceService.uploadFile(request, 10L);

        assertNotNull(response);
        assertEquals(500L, response.getEvidenceId());
        assertEquals(101L, response.getCriteriaId());
        assertEquals(EvidenceStatus.PENDING, response.getStatus());
        verify(evidenceRepository).save(any(Evidence.class));
    }

    @Test
    @DisplayName("Chặn nộp minh chứng khi tiêu chí có loại NONE (tự xác nhận)")
    void uploadFile_EvidenceTypeNone_ThrowsValidationException() {
        childCriteria.setEvidenceType(EvidenceType.NONE);

        UploadEvidenceRequest request = new UploadEvidenceRequest();
        request.setCriteriaId(101L);
        request.setFile(mockFile);

        when(criteriaRepository.findById(101L)).thenReturn(Optional.of(childCriteria));

        assertThrows(EvidenceValidationException.class, () -> evidenceService.uploadFile(request, 10L));
        verify(evidenceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Chặn nộp minh chứng vào tiêu chí nhóm cha")
    void uploadFile_ParentCriteria_ThrowsValidationException() {
        UploadEvidenceRequest request = new UploadEvidenceRequest();
        request.setCriteriaId(100L); // Parent criteria
        request.setFile(mockFile);

        when(criteriaRepository.findById(100L)).thenReturn(Optional.of(parentCriteria));

        assertThrows(EvidenceValidationException.class, () -> evidenceService.uploadFile(request, 10L));
    }

    @Test
    @DisplayName("Khóa không cho thêm minh chứng khi hồ sơ đã nộp (kể cả tiêu chí con)")
    void uploadFile_ApplicationSubmitted_ThrowsLockedException() {
        UploadEvidenceRequest request = new UploadEvidenceRequest();
        request.setCriteriaId(101L);
        request.setFile(mockFile);

        when(criteriaRepository.findById(101L)).thenReturn(Optional.of(childCriteria));
        when(userRepository.findById(10L)).thenReturn(Optional.of(student));
        when(applicationRepository.existsByStudentIdAndCampaignIdAndStatusIn(eq(10L), eq(1L), any())).thenReturn(true);

        assertThrows(EvidenceLockedException.class, () -> evidenceService.uploadFile(request, 10L));
    }

    @Test
    @DisplayName("Cập nhật lại minh chứng: Chặn khi minh chứng đã APPROVED")
    void uploadFile_UpdateApprovedEvidence_ThrowsValidationException() {
        UploadEvidenceRequest request = new UploadEvidenceRequest();
        request.setCriteriaId(101L);
        request.setEvidenceId(500L);
        request.setFile(mockFile);

        Evidence existing = Evidence.builder()
                .id(500L)
                .criteria(childCriteria)
                .user(student)
                .status(EvidenceStatus.APPROVED)
                .build();

        when(criteriaRepository.findById(101L)).thenReturn(Optional.of(childCriteria));
        when(userRepository.findById(10L)).thenReturn(Optional.of(student));
        when(applicationRepository.existsByStudentIdAndCampaignIdAndStatusIn(eq(10L), eq(1L), any())).thenReturn(false);
        when(evidenceRepository.findById(500L)).thenReturn(Optional.of(existing));

        assertThrows(EvidenceValidationException.class, () -> evidenceService.uploadFile(request, 10L));
    }

    @Test
    @DisplayName("Cập nhật lại minh chứng: Chặn khi không phải chủ sở hữu (403)")
    void uploadFile_UpdateNotOwner_ThrowsPermissionDeniedException() {
        User otherUser = new User();
        otherUser.setId(99L);

        UploadEvidenceRequest request = new UploadEvidenceRequest();
        request.setCriteriaId(101L);
        request.setEvidenceId(500L);
        request.setFile(mockFile);

        Evidence existing = Evidence.builder()
                .id(500L)
                .criteria(childCriteria)
                .user(otherUser) // Không phải student id 10
                .status(EvidenceStatus.NEEDS_MODIFICATION)
                .build();

        when(criteriaRepository.findById(101L)).thenReturn(Optional.of(childCriteria));
        when(userRepository.findById(10L)).thenReturn(Optional.of(student));
        when(applicationRepository.existsByStudentIdAndCampaignIdAndStatusIn(eq(10L), eq(1L), any())).thenReturn(false);
        when(evidenceRepository.findById(500L)).thenReturn(Optional.of(existing));

        assertThrows(EvidencePermissionDeniedException.class, () -> evidenceService.uploadFile(request, 10L));
    }

    @Test
    @DisplayName("Xóa minh chứng thành công khi chưa nộp hồ sơ")
    void deleteEvidence_Success() {
        Evidence existing = Evidence.builder()
                .id(500L)
                .criteria(childCriteria)
                .user(student)
                .evidenceUrl("http://localhost:8080/evidence/uploads/test.jpg")
                .build();

        when(evidenceRepository.findById(500L)).thenReturn(Optional.of(existing));
        when(applicationRepository.existsByStudentIdAndCampaignIdAndStatusIn(eq(10L), eq(1L), any())).thenReturn(false);

        evidenceService.deleteEvidence(500L, 10L);

        verify(fileStorageService).deleteFile("http://localhost:8080/evidence/uploads/test.jpg");
        verify(evidenceRepository).delete(existing);
    }

    @Test
    @DisplayName("Xóa minh chứng: Chặn khi hồ sơ đợt đã nộp (LockedException)")
    void deleteEvidence_ApplicationSubmitted_ThrowsLockedException() {
        Evidence existing = Evidence.builder()
                .id(500L)
                .criteria(childCriteria)
                .user(student)
                .build();

        when(evidenceRepository.findById(500L)).thenReturn(Optional.of(existing));
        when(applicationRepository.existsByStudentIdAndCampaignIdAndStatusIn(eq(10L), eq(1L), any())).thenReturn(true);

        assertThrows(EvidenceLockedException.class, () -> evidenceService.deleteEvidence(500L, 10L));
        verify(evidenceRepository, never()).delete(any(Evidence.class));
    }
}
