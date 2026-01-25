package com.example.SinhVien5T.Controller;

import com.example.SinhVien5T.Dto.Request.UserLoginRequest;
import com.example.SinhVien5T.Dto.Request.UserRegisterRequest;
import com.example.SinhVien5T.Dto.Response.ApiResponse;
import com.example.SinhVien5T.Service.AuthService;
import com.example.SinhVien5T.Service.UserService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/user/auth")
@RequiredArgsConstructor
public class UserAuthController {

    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody UserRegisterRequest request) throws Exception {

        authService.register(request);

        ApiResponse apiResponse = ApiResponse.success("Đăng kí thành công", null);

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PostMapping("/verify_register_token")
    public void verifyRegisterToken(@RequestParam String token, HttpServletResponse response) throws IOException {
        authService.verifyRegisterToken(token, response);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody UserLoginRequest request) throws MessagingException {

        authService.login(request);

        ApiResponse apiResponse = ApiResponse.success("Otp xác minh đã được gửi đến email của bạn", null);

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PostMapping("/verify_otp_login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyOtpLogin(@RequestParam String otp, HttpServletRequest request, HttpServletResponse response){
        Map<String, Object> body = authService.verifyOtpLogin(otp, request, response);

        ApiResponse<Map<String, Object>> apiResponse = ApiResponse.success("Đăng nhập thành công", body);

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }


    @PostMapping("/log_out")
    public ResponseEntity<ApiResponse> logOut(HttpServletRequest request, HttpServletResponse response){
        authService.logOut(request, response);

        ApiResponse apiResponse = ApiResponse.success("", null);

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PostMapping("/refresh_access_token")
    public ResponseEntity<ApiResponse<Map<String, Object>>> refreshAccessToken(HttpServletRequest request){
        Map<String, Object> body = authService.refreshAccessToken(request);

        ApiResponse<Map<String, Object>> apiResponse = ApiResponse.success("Refresh access token thành công", body);

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
}
