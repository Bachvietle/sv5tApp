package com.example.SinhVien5T.application.dto.response;

import com.example.SinhVien5T.application.entity.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationPreviewResponse {

    private Long campaignId;

    private String campaignName;

    private Integer passedStandardsCount;

    private Integer totalStandardsCount;

    private Boolean isEligibleToSubmit;

    private Boolean isProfileCompleted;

    private Boolean alreadySubmitted;

    private ApplicationStatus currentApplicationStatus;

    @Builder.Default
    private List<StandardEvaluationResult> standards = new ArrayList<>();
}
