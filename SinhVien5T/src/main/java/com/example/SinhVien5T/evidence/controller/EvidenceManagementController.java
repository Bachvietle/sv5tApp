package com.example.SinhVien5T.evidence.controller;

import com.example.SinhVien5T.common.dto.response.ApiResponse;
import com.example.SinhVien5T.common.dto.response.PageResponse;
import com.example.SinhVien5T.evidence.dto.request.EvidenceFilterRequest;
import com.example.SinhVien5T.evidence.dto.request.EvidenceReviewRequest;
import com.example.SinhVien5T.evidence.dto.response.EvidenceAdminDTO;
import com.example.SinhVien5T.evidence.entity.EvidenceStatus;
import com.example.SinhVien5T.evidence.service.EvidenceManagementService;
import com.example.SinhVien5T.user.entity.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/management/evidences")
@RequiredArgsConstructor
public class EvidenceManagementController {

    private final EvidenceManagementService evidenceManagementService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<EvidenceAdminDTO>>> getEvidences(
            @ModelAttribute EvidenceFilterRequest filterRequest) {

        PageResponse<EvidenceAdminDTO> result = evidenceManagementService.getEvidences(filterRequest);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách minh chứng thành công", result));
    }

    @PatchMapping("/{id}/review")
    public ResponseEntity<ApiResponse<EvidenceAdminDTO>> reviewEvidence(
            @PathVariable Long id,
            @Valid @RequestBody EvidenceReviewRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        EvidenceAdminDTO result = evidenceManagementService.reviewEvidence(id, request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Thẩm định minh chứng thành công", result));
    }
}
