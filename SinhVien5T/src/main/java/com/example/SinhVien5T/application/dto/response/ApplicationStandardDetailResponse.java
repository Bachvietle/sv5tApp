package com.example.SinhVien5T.application.dto.response;

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
public class ApplicationStandardDetailResponse {

    private Long id;

    private String name;

    private String description;

    private Boolean isPassed;

    private Integer mandatoryPassedCount;

    private Integer mandatoryTotalCount;

    private Boolean isMandatoryPassed;

    private Integer optionalPassedCount;

    private Integer optionalTotalCount;

    private Boolean isOptionalPassed;

    @Builder.Default
    private List<ApplicationCriteriaDetailResponse> criteriaList = new ArrayList<>();
}
