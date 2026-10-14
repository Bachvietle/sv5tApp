package com.example.SinhVien5T.application.dto.request;

import com.example.SinhVien5T.application.entity.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationEvaluateRequest {

    @NotNull(message = "Trạng thái thẩm định không được để trống")
    private ApplicationStatus status;

    private String reviewerComment;
}
