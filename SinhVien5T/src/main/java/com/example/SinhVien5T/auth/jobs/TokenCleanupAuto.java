package com.example.SinhVien5T.auth.jobs;

import com.example.SinhVien5T.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@EnableScheduling
public class TokenCleanupAuto {
    private final RefreshTokenRepository refreshTokenRepository;

    @Scheduled(cron = "0 0 3 * * ?") // Chạy vào 3 giờ sáng mỗi ngày
    public void cleanExpiredTokens() {
        refreshTokenRepository.deleteByExpiredAtBefore(LocalDateTime.now());
    }
}


