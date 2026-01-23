package com.example.SinhVien5T.Controller;

import com.example.SinhVien5T.Dto.Request.UserRegisterRequest;
import com.example.SinhVien5T.Dto.Response.ApiResponse;
import com.example.SinhVien5T.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(UserRegisterRequest request) throws Exception {
        userService.register(request);

        ApiResponse response = ApiResponse.success("", null);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/verify_register_token")
    public void verifyRegisterToken(){

    }
}
