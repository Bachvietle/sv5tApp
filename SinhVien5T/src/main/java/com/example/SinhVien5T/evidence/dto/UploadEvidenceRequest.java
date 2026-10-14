package com.example.SinhVien5T.evidence.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UploadEvidenceRequest {

    @NotNull(message = "Criteria ID không được để trống")
    private Long criteriaId;

    @NotNull(message = "File minh chứng không được để trống")
    private MultipartFile file;

    private Long evidenceId;

    private String description;
}
