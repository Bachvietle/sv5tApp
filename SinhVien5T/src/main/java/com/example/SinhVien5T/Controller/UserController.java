package com.example.SinhVien5T.Controller;

import com.example.SinhVien5T.Dto.Request.UserRegisterRequest;
import com.example.SinhVien5T.Dto.Response.ApiResponse;
import com.example.SinhVien5T.Service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody UserRegisterRequest request) throws Exception {

        userService.register(request);

        ApiResponse apiResponse = ApiResponse.success("Đăng kí thành công", null);

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PostMapping("/verify_register_token")
    public void verifyRegisterToken(@RequestParam String token, HttpServletResponse response) throws IOException {
        userService.verifyRegisterToken(token, response);
    }
}
