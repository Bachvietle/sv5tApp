package com.example.SinhVien5T.campaign.controller;

import com.example.SinhVien5T.campaign.dto.StandardDTO;
import com.example.SinhVien5T.campaign.service.CampaignService;
import com.example.SinhVien5T.common.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user/campaign")
@RequiredArgsConstructor
public class CampaignController {

    private final CampaignService campaignService;

    @GetMapping("/get_campaign/{id}")
    public ResponseEntity<ApiResponse<List<StandardDTO>>> getCampaign (@PathVariable("id") Long campaignId){

        List<StandardDTO> data = campaignService.getResultTree(campaignId);

        ApiResponse<List<StandardDTO>> response = ApiResponse.success("Lấy dữ liệu thành công", data);

        return new ResponseEntity<>(response, HttpStatus.OK);

    }
}
