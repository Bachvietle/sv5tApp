package com.example.SinhVien5T.evidence.dto.request;

import com.example.SinhVien5T.evidence.entity.EvidenceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvidenceFilterRequest {

    private EvidenceStatus status;

    private String keyword;

    private String courseYear;

    private Long standardId;

    private Boolean isResubmitted;

    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 10;

    private String sortBy;

    @Builder.Default
    private String sortDirection = "DESC";
}
