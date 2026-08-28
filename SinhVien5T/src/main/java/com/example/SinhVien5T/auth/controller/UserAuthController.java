package com.example.SinhVien5T.auth.controller;

import com.example.SinhVien5T.auth.dto.request.UserLoginRequest;
import com.example.SinhVien5T.auth.dto.request.UserRegisterRequest;
import com.example.SinhVien5T.auth.dto.request.UserResetPwRequest;
import com.example.SinhVien5T.common.dto.response.ApiResponse;
import com.example.SinhVien5T.auth.service.AuthService;
import com.example.SinhVien5T.user.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
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
    public ResponseEntity<Void> verifyRegisterToken(@RequestParam String token) throws IOException {
        String redirectUrl = authService.verifyRegisterToken(token);
        
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(redirectUrl))
                .build();
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request, HttpServletResponse response) throws MessagingException {

        String ipAddress = getIpAddress(request);
        String userAgent = request.getHeader("User-Agent");

        Map<String, Object> data = authService.login(userLoginRequest, ipAddress, userAgent);

        String refreshToken = (String) data.remove("refreshToken");
        addRefreshCookie(refreshToken, 7 * 24 * 60 * 60, response);

        ApiResponse<Map<String, Object>> apiResponse = ApiResponse.success("Đăng nhập thành công", data);

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }


    @PostMapping("/log_out")
    public ResponseEntity<ApiResponse> logOut(@CookieValue(name = "refreshToken", required = false) String refreshToken, HttpServletResponse response){
        authService.logOut(refreshToken);

        addRefreshCookie(null, 0, response);

        ApiResponse apiResponse = ApiResponse.success("", null);

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PostMapping("/refresh_access_token")
    public ResponseEntity<ApiResponse<Map<String, Object>>> refreshAccessToken(@CookieValue(name = "refreshToken", required = false) String refreshToken){
        Map<String, Object> body = authService.refreshAccessToken(refreshToken);

        ApiResponse<Map<String, Object>> apiResponse = ApiResponse.success("Refresh access token thành công", body);

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    private void addRefreshCookie(String refreshToken, int maxAge, HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken != null ? refreshToken : "")
                .httpOnly(true)
                .secure(false) // dev
                .path("/")
                .maxAge(maxAge)
                .sameSite("Strict") // dev
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private String getIpAddress(HttpServletRequest request) {
        String remoteAddress = request.getHeader("X-Forwarded-For");
        return (remoteAddress != null && !remoteAddress.isEmpty()) ? remoteAddress : request.getRemoteAddr();
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


