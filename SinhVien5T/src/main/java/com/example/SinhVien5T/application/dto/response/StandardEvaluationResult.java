package com.example.SinhVien5T.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StandardEvaluationResult {

    private Long standardId;

    private String standardName;

    private Boolean isPassed;

    private Integer mandatoryPassedCount;

    private Integer mandatoryTotalCount;

    private Boolean isMandatoryPassed;

    private Integer optionalPassedCount;

    private Integer optionalTotalCount;

    private Boolean isOptionalPassed;
}
