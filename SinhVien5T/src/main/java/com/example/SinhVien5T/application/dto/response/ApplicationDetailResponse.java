package com.example.SinhVien5T.application.dto.response;

import com.example.SinhVien5T.application.entity.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationDetailResponse {

    private Long id;

    private Long campaignId;

    private String campaignName;

    private ApplicationStatus status;

    private LocalDateTime submittedAt;

    private Integer passedStandardsCount;

    private Integer totalStandardsCount;

    private Long reviewerId;

    private String reviewerName;

    private LocalDateTime reviewedAt;

    private String reviewerComment;

    private StudentProfileSnapshot studentProfile;

    @Builder.Default
    private List<ApplicationStandardDetailResponse> standards = new ArrayList<>();
}
