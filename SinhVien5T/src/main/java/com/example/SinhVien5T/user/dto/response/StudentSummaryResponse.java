package com.example.SinhVien5T.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentSummaryResponse {

    private Long id;

    private String studentCode;

    private String fullName;

    private String email;

    private String faculty;

    private String classCode;

    private String courseYear;

    private String avatar;

    private boolean isActive;

    private boolean isProfileCompleted;

    private LocalDateTime createdAt;
}
