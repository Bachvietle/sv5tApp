package com.example.SinhVien5T.application.repository;

import com.example.SinhVien5T.application.entity.Application;
import com.example.SinhVien5T.application.entity.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long>, JpaSpecificationExecutor<Application> {

    Optional<Application> findByStudentIdAndCampaignId(Long studentId, Long campaignId);

    boolean existsByStudentIdAndCampaignId(Long studentId, Long campaignId);

    boolean existsByStudentIdAndCampaignIdAndStatus(Long studentId, Long campaignId, ApplicationStatus status);

    boolean existsByStudentIdAndCampaignIdAndStatusIn(Long studentId, Long campaignId, Collection<ApplicationStatus> statuses);
}
