package com.example.SinhVien5T.Dto.Request;

import lombok.Data;

@Data public class VerifyOtpAndResetRequest { private String email; private String otp; private String newPassword; }
