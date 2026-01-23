package com.example.SinhVien5T.Dto.Response;
import lombok.AllArgsConstructor;
import lombok.Data;
@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String refreshToken;
}
