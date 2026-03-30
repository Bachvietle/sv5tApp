package com.example.SinhVien5T.Dto.Response;

import com.example.SinhVien5T.Entity.Enum.Gender;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Data
public class ProfileResponse {
    //Auth
    private Long id;
    private String username;
    private String email;

    //Thông tin cá nhân
    private String firstName;
    private String lastName;
    private String avatar;
    private LocalDate DoB;
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
    private boolean isProfileUpdate;

    //Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
