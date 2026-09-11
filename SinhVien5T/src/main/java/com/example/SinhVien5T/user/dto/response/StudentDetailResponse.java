package com.example.SinhVien5T.user.dto.response;

import com.example.SinhVien5T.user.entity.Gender;
import com.example.SinhVien5T.user.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDetailResponse {

    private Long id;

    private String userName;

    private String email;

    private String firstName;

    private String lastName;

    private String fullName;

    private Role role;

    private String avatar;

    private LocalDate dob;

    private Gender gender;

    private String ethnicity;

    private String idenNumber;

    private String university;

    private String fieldOfStudy;

    private String courseYear;

    private String studentCode;

    private String classCode;

    private String faculty;

    private String currentPosition;

    private String province;

    private String commune;

    private String specificAddress;

    private String provinceTemp;

    private String communeTemp;

    private String specificAddressTemp;

    private String phoneNumber;

    private String organPosition;

    private String ydMember;

    private boolean isProfileCompleted;

    private boolean isVerified;

    private boolean isActive;

    private LocalDateTime createdAt;

    private String createdBy;

    private LocalDateTime updatedAt;

    private String updatedBy;
}
