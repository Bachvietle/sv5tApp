package com.example.SinhVien5T.campaign.dto;

import com.example.SinhVien5T.campaign.entity.CampaignStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignStatusUpdateRequest {

    @NotNull(message = "Trạng thái không được để trống")
    private CampaignStatus status;

    @Builder.Default
    private Boolean sendNotification = true;
}
