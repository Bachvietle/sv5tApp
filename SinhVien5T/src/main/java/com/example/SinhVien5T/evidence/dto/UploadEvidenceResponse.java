package com.example.SinhVien5T.evidence.dto;

import com.example.SinhVien5T.evidence.entity.EvidenceStatus;
import lombok.Builder;
import lombok.Data;

@Builder
public class UploadEvidenceResponse {

    Long evidenceId;

    Long criteriaId;

    String evidenceUrl;

    EvidenceStatus status;

}
