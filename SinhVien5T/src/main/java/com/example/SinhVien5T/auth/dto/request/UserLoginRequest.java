package com.example.SinhVien5T.auth.dto.request;

import lombok.Data;

@Data
public class UserLoginRequest {
    private String email;

    private String userPassword;
}


