package com.example.SinhVien5T.campaign.dto;

import com.example.SinhVien5T.campaign.entity.CampaignStatus;
import com.example.SinhVien5T.campaign.entity.Level;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignFilterRequest {

    private String keyword;

    private Long academicYear;

    private Level level;

    private CampaignStatus status;

    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 10;

    private String sortBy;

    private String sortDirection;
}
