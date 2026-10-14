package com.example.SinhVien5T.evidence.dto.response;

import com.example.SinhVien5T.evidence.entity.EvidenceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvidenceItemDTO {

    private Long id;

    private String evidenceUrl;

    private String description;

    private EvidenceStatus status;

    private String reviewerComment;

    private LocalDateTime submittedAt;

    private Integer submissionCount;

    private LocalDateTime reviewedAt;
}
