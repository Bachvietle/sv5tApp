package com.example.SinhVien5T.evidence.controller;

import com.example.SinhVien5T.common.dto.response.ApiResponse;
import com.example.SinhVien5T.evidence.dto.UploadEvidenceRequest;
import com.example.SinhVien5T.evidence.dto.UploadEvidenceResponse;
import com.example.SinhVien5T.evidence.entity.Evidence;
import com.example.SinhVien5T.evidence.service.EvidenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/evidence")
@RequiredArgsConstructor
public class EvidenceController {

    private final EvidenceService evidenceService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<UploadEvidenceResponse>> uploadEvidence (@Valid @ModelAttribute UploadEvidenceRequest request) throws IOException {

        UploadEvidenceResponse data = evidenceService.uploadFile(request);

        ApiResponse<UploadEvidenceResponse> response = ApiResponse.success("Tải file thành công",data);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
