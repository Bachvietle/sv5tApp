package com.example.SinhVien5T.auth.dto.request;


import com.example.SinhVien5T.user.entity.Gender;
import com.example.SinhVien5T.user.entity.Role;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserUpdateProfileRequest {

    private String userName;

    private String email;

    private String avatar;

    private String phoneNumber;

    private String ethnicity;

    private Gender gender;

    private LocalDate birthDay;

    private String address;

    private String faculty;

    private String classId;

    private String studentCode;
}
