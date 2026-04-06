package com.example.SinhVien5T.user.dto.request;


import com.example.SinhVien5T.user.entity.Gender;
import lombok.Data;


import java.time.LocalDate;

@Data
public class UpdateProfileRequest {
    private String firstName;
    private String lastName;
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
}