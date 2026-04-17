package com.example.SinhVien5T.user.controller;


import com.example.SinhVien5T.auth.dto.request.UserUpdateProfileRequest;
import com.example.SinhVien5T.common.dto.response.ApiResponse;
import com.example.SinhVien5T.user.dto.request.UpdateProfileRequest;
import com.example.SinhVien5T.user.entity.User;
import com.example.SinhVien5T.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/user/profile")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<User>> getProfile(){
        User data = userService.getProfile();
        return new  ResponseEntity<>(ApiResponse.success("Lấy thông tin thành công", data), HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<ApiResponse<User>> updateProfile(@RequestBody UpdateProfileRequest request){
        userService.updateProfile(request);
        return new ResponseEntity<>(ApiResponse.success("Cập nhật thành công", null ), HttpStatus.OK);
    }
}
