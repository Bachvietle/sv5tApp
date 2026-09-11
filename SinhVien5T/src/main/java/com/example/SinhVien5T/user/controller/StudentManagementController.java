package com.example.SinhVien5T.user.controller;

import com.example.SinhVien5T.common.dto.response.ApiResponse;
import com.example.SinhVien5T.user.dto.request.StudentFilterRequest;
import com.example.SinhVien5T.user.dto.request.UpdateStudentStatusRequest;
import com.example.SinhVien5T.user.dto.response.PageResponse;
import com.example.SinhVien5T.user.dto.response.StudentDetailResponse;
import com.example.SinhVien5T.user.dto.response.StudentSummaryResponse;
import com.example.SinhVien5T.user.service.StudentManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/management/students")
@RequiredArgsConstructor
public class StudentManagementController {

    private final StudentManagementService studentManagementService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<StudentSummaryResponse>>> getStudents(
            @ModelAttribute StudentFilterRequest filterRequest) {
        PageResponse<StudentSummaryResponse> result = studentManagementService.getStudents(filterRequest);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách sinh viên thành công", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentDetailResponse>> getStudentDetail(@PathVariable Long id) {
        StudentDetailResponse result = studentManagementService.getStudentDetail(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin chi tiết sinh viên thành công", result));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateStudentStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStudentStatusRequest request) {
        studentManagementService.updateStudentStatus(id, request);
        String message = Boolean.TRUE.equals(request.getIsActive())
                ? "Mở khóa tài khoản sinh viên thành công"
                : "Khóa tài khoản sinh viên thành công";
        return ResponseEntity.ok(ApiResponse.success(message, null));
    }
}
