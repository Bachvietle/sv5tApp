package com.example.SinhVien5T.application.controller;

import com.example.SinhVien5T.application.dto.request.ApplicationEvaluateRequest;
import com.example.SinhVien5T.application.dto.request.ApplicationFilterRequest;
import com.example.SinhVien5T.application.dto.response.ApplicationDetailResponse;
import com.example.SinhVien5T.application.dto.response.ApplicationSummaryResponse;
import com.example.SinhVien5T.application.service.ApplicationManagementService;
import com.example.SinhVien5T.common.dto.response.ApiResponse;
import com.example.SinhVien5T.common.dto.response.PageResponse;
import com.example.SinhVien5T.user.entity.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/management/applications")
@RequiredArgsConstructor
public class ApplicationManagementController {

    private final ApplicationManagementService applicationManagementService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ApplicationSummaryResponse>>> getApplications(
            @ModelAttribute ApplicationFilterRequest request) {

        PageResponse<ApplicationSummaryResponse> data = applicationManagementService.getApplications(request);
        return new ResponseEntity<>(ApiResponse.success("Lấy danh sách hồ sơ thành công", data), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ApplicationDetailResponse>> getApplicationDetail(
            @PathVariable("id") Long id) {

        ApplicationDetailResponse data = applicationManagementService.getApplicationDetail(id);
        return new ResponseEntity<>(ApiResponse.success("Lấy chi tiết hồ sơ thành công", data), HttpStatus.OK);
    }

    @PatchMapping("/{id}/evaluate")
    public ResponseEntity<ApiResponse<ApplicationSummaryResponse>> evaluateApplication(
            @PathVariable("id") Long id,
            @Valid @RequestBody ApplicationEvaluateRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        ApplicationSummaryResponse data = applicationManagementService.evaluateApplication(id, request, currentUser.getId());
        return new ResponseEntity<>(ApiResponse.success("Thẩm định hồ sơ thành công", data), HttpStatus.OK);
    }
}
