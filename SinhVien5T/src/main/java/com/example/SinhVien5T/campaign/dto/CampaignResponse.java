package com.example.SinhVien5T.campaign.dto;

import com.example.SinhVien5T.campaign.entity.CampaignStatus;
import com.example.SinhVien5T.campaign.entity.Level;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignResponse {

    private Long id;

    private String name;

    private Long academicYear;

    private Level level;

    private CampaignStatus status;

    private LocalDate startDate;

    private LocalDate endDate;

    private Boolean isOpen;
}
