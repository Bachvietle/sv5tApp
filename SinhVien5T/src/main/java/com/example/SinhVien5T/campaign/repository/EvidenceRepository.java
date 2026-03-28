package com.example.SinhVien5T.campaign.repository;

import com.example.SinhVien5T.campaign.entity.Evidence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvidenceRepository extends JpaRepository<Evidence, Long> {

    @Query("SELECT e FROM Evidence e " +
            "JOIN  e.criteria cr " +
            "JOIN  cr.standard st " +
            "JOIN  st.campaign ca " +
            "WHERE e.user.id = :userId AND ca.id = :campaignId ")
    List<Evidence> findByUserAndCampaign(@Param("userId") Long userId, @Param("campaignId") Long campaignId);
}
