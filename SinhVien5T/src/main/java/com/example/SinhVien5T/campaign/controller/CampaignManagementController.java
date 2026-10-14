package com.example.SinhVien5T.campaign.controller;

import com.example.SinhVien5T.campaign.dto.CampaignFilterRequest;
import com.example.SinhVien5T.campaign.dto.CampaignResponse;
import com.example.SinhVien5T.campaign.dto.CampaignStatusUpdateRequest;
import com.example.SinhVien5T.campaign.entity.Campaign;
import com.example.SinhVien5T.campaign.service.CampaignService;
import com.example.SinhVien5T.common.dto.response.ApiResponse;
import com.example.SinhVien5T.common.dto.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/management/campaigns")
@RequiredArgsConstructor
public class CampaignManagementController {

    private final CampaignService campaignService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CampaignResponse>>> getCampaignsPage(
            @ModelAttribute CampaignFilterRequest request) {

        PageResponse<CampaignResponse> data = campaignService.getCampaignsPage(request);
        return new ResponseEntity<>(ApiResponse.success("Lấy danh sách đợt xét tuyển thành công", data), HttpStatus.OK);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Campaign>> updateCampaignStatus(
            @PathVariable("id") Long id,
            @Valid @RequestBody CampaignStatusUpdateRequest request) {

        Campaign updated = campaignService.updateCampaignStatus(id, request);
        return new ResponseEntity<>(ApiResponse.success("Cập nhật trạng thái đợt xét tuyển thành công", updated), HttpStatus.OK);
    }
}
