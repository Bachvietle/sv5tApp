package com.example.SinhVien5T.campaign.repository;

import com.example.SinhVien5T.campaign.entity.Campaign;
import com.example.SinhVien5T.campaign.entity.CampaignStatus;
import com.example.SinhVien5T.campaign.entity.Criteria;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CampaignRepository extends JpaRepository<Campaign, Long>, JpaSpecificationExecutor<Campaign> {


    // Lấy toàn bộ List criteria phẳng (ko phân biệt cha - con)
    @Query("SELECT c FROM Criteria c " +
            "JOIN c.standard st " +
            "JOIN st.campaign ca " +
            "WHERE ca.id = :campaignId ")
    List<Criteria> findAllCriteria(@Param("campaignId") Long campaignId);

    @Query("SELECT c FROM Campaign c WHERE (c.status IS NULL OR c.status <> :activeStatus) " +
            "AND c.startDate IS NOT NULL AND c.startDate <= :today " +
            "AND (c.end_date IS NULL OR c.end_date >= :today)")
    List<Campaign> findCampaignsToAutoOpen(@Param("today") LocalDate today, @Param("activeStatus") CampaignStatus activeStatus);

    @Query("SELECT c FROM Campaign c WHERE c.status = :activeStatus " +
            "AND c.end_date IS NOT NULL AND c.end_date < :today")
    List<Campaign> findCampaignsToAutoClose(@Param("today") LocalDate today, @Param("activeStatus") CampaignStatus activeStatus);

    Optional<Campaign> findFirstByStatus(CampaignStatus status);

    List<Campaign> findByStatus(CampaignStatus status);
}
