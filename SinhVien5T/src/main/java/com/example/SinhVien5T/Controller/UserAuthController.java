package com.example.SinhVien5T.Controller;

import com.example.SinhVien5T.Dto.Request.UserLoginRequest;
import com.example.SinhVien5T.Dto.Request.UserRegisterRequest;
import com.example.SinhVien5T.Dto.Request.UserResetPwRequest;
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

    @GetMapping("/verify_register_token")
    public ResponseEntity<ApiResponse> verifyRegisterToken(@RequestParam String token, HttpServletResponse response) throws IOException {
        authService.verifyRegisterToken(token, response);

        ApiResponse apiResponse = ApiResponse.success("Xác minh thành công", null);

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request, HttpServletResponse response) throws MessagingException {

        Map<String, Object> data = authService.login(userLoginRequest, request, response);

        ApiResponse<Map<String, Object>> apiResponse = ApiResponse.success("Đăng nhập thaành công", data);

        System.out.println("Đăng nhập thành công");

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

    @PostMapping("/missing_password")
    public void missingPassWord(@RequestParam String email) throws MessagingException {
        authService.missingPassWord(email);
    }

    @GetMapping("/verify_reset_token")
    public ResponseEntity<ApiResponse> checkResetToken(@RequestParam String token) throws IOException {
        // Logic: Chỉ check xem token có tồn tại và còn hạn không
        boolean isValid = authService.verifyResetPwToken(token);

        if (isValid) {
            return ResponseEntity.ok(ApiResponse.success("Token hợp lệ", null));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Token hết hạn hoặc không tồn tại"));
        }
    }

    @PostMapping("/reset_password")
    public ResponseEntity<ApiResponse<String>> resetPassWord(@RequestBody UserResetPwRequest request) throws MessagingException {

        authService.resetPassWord(request.getToken(), request.getNewPw());

        ApiResponse apiResponse = ApiResponse.success("Đổi mật khẩu thành công", null);

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
}
