package com.example.SinhVien5T.evidence.dto;

import lombok.Builder;
import lombok.Data;

@Builder
public class UploadEvidenceResponse {

    Long evidenceId;

    Long criteriaId;

    String evidenceUrl;

    Boolean status;

}
