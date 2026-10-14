package com.example.SinhVien5T.application.dto.request;

import com.example.SinhVien5T.application.entity.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationFilterRequest {

    private Long campaignId;

    private ApplicationStatus status;

    private String keyword;

    private String faculty;

    private String courseYear;

    private Integer passedStandardsCount;

    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 10;

    private String sortBy;

    private String sortDirection;
}
