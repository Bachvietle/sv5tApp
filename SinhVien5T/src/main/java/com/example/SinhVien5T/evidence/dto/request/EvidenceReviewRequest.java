package com.example.SinhVien5T.evidence.dto.request;

import com.example.SinhVien5T.evidence.entity.EvidenceStatus;
import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class EvidenceReviewRequest {
    @NotNull(message = "Status cannot be null")
    private EvidenceStatus status;

    private String reviewerComment;
}
