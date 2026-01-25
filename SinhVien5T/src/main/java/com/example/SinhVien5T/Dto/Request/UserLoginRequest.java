package com.example.SinhVien5T.Dto.Request;

import lombok.Data;

@Data
public class UserLoginRequest {
    private String email;

    private String userPassword;
}
