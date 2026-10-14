package com.example.SinhVien5T.application.controller;

import com.example.SinhVien5T.application.dto.request.SubmitApplicationRequest;
import com.example.SinhVien5T.application.dto.response.ApplicationDetailResponse;
import com.example.SinhVien5T.application.dto.response.ApplicationPreviewResponse;
import com.example.SinhVien5T.application.service.ApplicationService;
import com.example.SinhVien5T.common.dto.response.ApiResponse;
import com.example.SinhVien5T.user.entity.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/application")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @GetMapping("/preview/{campaignId}")
    public ResponseEntity<ApiResponse<ApplicationPreviewResponse>> previewApplication(
            @PathVariable("campaignId") Long campaignId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        ApplicationPreviewResponse data = applicationService.previewApplication(campaignId, currentUser.getId());
        return new ResponseEntity<>(ApiResponse.success("Lấy thông tin tiến độ hồ sơ thành công", data), HttpStatus.OK);
    }

    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<ApplicationDetailResponse>> submitApplication(
            @Valid @RequestBody SubmitApplicationRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        ApplicationDetailResponse data = applicationService.submitApplication(request, currentUser.getId());
        return new ResponseEntity<>(ApiResponse.success("Nộp hồ sơ xét tuyển thành công", data), HttpStatus.CREATED);
    }

    @GetMapping("/my-application/{campaignId}")
    public ResponseEntity<ApiResponse<ApplicationDetailResponse>> getMyApplication(
            @PathVariable("campaignId") Long campaignId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        ApplicationDetailResponse data = applicationService.getMyApplication(campaignId, currentUser.getId());
        return new ResponseEntity<>(ApiResponse.success("Lấy thông tin hồ sơ thành công", data), HttpStatus.OK);
    }
}
