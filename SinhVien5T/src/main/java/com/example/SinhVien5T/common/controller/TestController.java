package com.example.SinhVien5T.common.controller;

import com.example.SinhVien5T.common.dto.response.ApiResponse;
import com.example.SinhVien5T.campaign.entity.Campaign;
import com.example.SinhVien5T.campaign.repository.CampaignRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    private final CampaignRepository campaignRepository;

    @GetMapping("/get_campaign")
    public ResponseEntity<ApiResponse<Campaign>> getCampaign(@RequestParam Long id){
        Campaign campaign = campaignRepository.findById(id).orElseThrow(
                () ->  new RuntimeException("")
        );

        ApiResponse<Campaign> response = ApiResponse.success("Thanh cong", campaign);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}


