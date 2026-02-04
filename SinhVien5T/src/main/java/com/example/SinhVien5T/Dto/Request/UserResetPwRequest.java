package com.example.SinhVien5T.Dto.Request;

import lombok.Data;

@Data
public class UserResetPwRequest {

    private String token;

    private  String newPw;
}
