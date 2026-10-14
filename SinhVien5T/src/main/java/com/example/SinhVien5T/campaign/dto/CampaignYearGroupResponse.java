package com.example.SinhVien5T.campaign.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignYearGroupResponse {

    private Long academicYear;

    @Builder.Default
    private List<CampaignResponse> campaigns = new ArrayList<>();
}
