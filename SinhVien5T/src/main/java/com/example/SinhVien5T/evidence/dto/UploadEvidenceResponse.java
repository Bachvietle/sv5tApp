package com.example.SinhVien5T.evidence.dto;

import com.example.SinhVien5T.evidence.entity.EvidenceStatus;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UploadEvidenceResponse {

    private Long evidenceId;

    private Long criteriaId;

    private String evidenceUrl;

    private EvidenceStatus status;

    private String description;
}
