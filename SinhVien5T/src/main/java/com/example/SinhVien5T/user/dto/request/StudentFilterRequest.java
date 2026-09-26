package com.example.SinhVien5T.user.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentFilterRequest {

    private String keyword;

    private String faculty;

    private String courseYear;

    private Boolean isActive;

    private Boolean isProfileCompleted;

    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 10;

    private String sortBy;

    @Builder.Default
    private String sortDirection = "ASC";
}
