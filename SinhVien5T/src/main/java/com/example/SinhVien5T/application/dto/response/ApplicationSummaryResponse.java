package com.example.SinhVien5T.application.dto.response;

import com.example.SinhVien5T.application.entity.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationSummaryResponse {

    private Long id;

    private Long studentId;

    private String studentName;

    private String studentCode;

    private String studentEmail;

    private String faculty;

    private String courseYear;

    private Long campaignId;

    private String campaignName;

    private ApplicationStatus status;

    private LocalDateTime submittedAt;

    private Integer passedStandardsCount;

    private Integer totalStandardsCount;

    private Long reviewerId;

    private String reviewerName;

    private LocalDateTime reviewedAt;
}
