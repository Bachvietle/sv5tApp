package com.example.SinhVien5T.evidence.dto.response;

import com.example.SinhVien5T.evidence.entity.EvidenceStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class EvidenceAdminDTO {
    private Long id;
    private String evidenceUrl;
    private EvidenceStatus status;
    private String description;
    private Integer submissionCount;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private String reviewerComment;

    // Student Info
    private Long studentId;
    private String studentName;
    private String studentMssv;
    private String studentFaculty;

    // Criteria Info
    private Long criteriaId;
    private String criteriaName;
    private String standardName;
}
