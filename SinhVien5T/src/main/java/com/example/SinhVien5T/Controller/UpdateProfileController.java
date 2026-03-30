package com.example.SinhVien5T.Controller;

import com.example.SinhVien5T.Dto.Request.UpdateProfileRequest;
import com.example.SinhVien5T.Dto.Response.ApiResponse;
import com.example.SinhVien5T.Dto.Response.ProfileResponse;
import com.example.SinhVien5T.Service.AuthService;
import com.example.SinhVien5T.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.channels.MulticastChannel;

@RestController
@RequestMapping("/user/profile")
@RequiredArgsConstructor
public class UpdateProfileController {
    private final UserService userService;

    @GetMapping

    public ResponseEntity<ApiResponse<ProfileResponse>> getProfile() {
        ProfileResponse data = userService.getProfile();
        return new ResponseEntity<>(ApiResponse.success("Đã lấy Profile", data), HttpStatus.OK);
    }

    @PutMapping (consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProfileResponse>> updateProfile(
            @RequestPart("data") UpdateProfileRequest request,
            @RequestPart(value = "avatar", required = false) MultipartFile avatarFile
            ) {
        ProfileResponse data = userService.updateProfile(request,avatarFile);
        return new ResponseEntity<>(ApiResponse.success("Cập nhật thông tin thành công", data),  HttpStatus.OK);
    }

}
