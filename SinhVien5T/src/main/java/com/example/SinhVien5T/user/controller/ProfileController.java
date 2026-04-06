package com.example.SinhVien5T.user.controller;


import com.example.SinhVien5T.common.dto.response.ApiResponse;
import com.example.SinhVien5T.user.dto.request.UpdateProfileRequest;
import com.example.SinhVien5T.user.entity.User;
import com.example.SinhVien5T.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("user/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<User>> getProfile(){
        User data = userService.getProfile();
        return new  ResponseEntity<>(ApiResponse.success("Lấy thông tin thành công", data), HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<ApiResponse<User>> updateProfile(@RequestBody UpdateProfileRequest request){
        User data = userService.updateProfile(request);
        return new ResponseEntity<>(ApiResponse.success("Cập nhật thành công", data), HttpStatus.OK);
    }
}
