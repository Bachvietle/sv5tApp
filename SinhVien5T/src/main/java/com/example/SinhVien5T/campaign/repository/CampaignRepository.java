package com.example.SinhVien5T.campaign.repository;

import com.example.SinhVien5T.campaign.entity.Campaign;
import com.example.SinhVien5T.campaign.entity.Criteria;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {


    // Lấy toàn bộ List criteria phẳng (ko phân biệt cha - con)
    @Query("SELECT c FROM Criteria c " +
            "JOIN c.standard st " +
            "JOIN st.campaign ca " +
            "WHERE ca.id = :campaignId ")
    List<Criteria> findAllCriteria(@Param("campaignId") Long campaignId);
}
