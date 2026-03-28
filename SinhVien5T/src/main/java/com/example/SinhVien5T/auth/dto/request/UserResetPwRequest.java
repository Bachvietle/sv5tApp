package com.example.SinhVien5T.auth.dto.request;

import lombok.Data;

@Data
public class UserResetPwRequest {

    private String token;

    private  String newPw;
}


