package com.example.SinhVien5T.campaign.jobs;

import com.example.SinhVien5T.campaign.service.CampaignService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CampaignSchedulerJob {

    private final CampaignService campaignService;

    // Chạy vào 00:00 mỗi ngày để tự động mở đợt đến ngày và đóng đợt quá hạn
    @Scheduled(cron = "0 0 0 * * ?")
    public void scheduleCampaignStatusCheck() {
        campaignService.autoCheckCampaignStatus();
    }
}
