package com.example.SinhVien5T.application.dto.response;

import com.example.SinhVien5T.user.entity.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentProfileSnapshot {

    private Long studentId;

    private String studentCode;

    private String fullName;

    private String email;

    private String phoneNumber;

    private LocalDate dob;

    private Gender gender;

    private String ethnicity;

    private String idenNumber;

    private String university;

    private String faculty;

    private String classCode;

    private String courseYear;

    private String fieldOfStudy;

    private String organPosition;

    private String ydMember;

    private String currentPosition;

    private String specificAddress;

    private String commune;

    private String province;

    private String specificAddressTemp;

    private String communeTemp;

    private String provinceTemp;
}
