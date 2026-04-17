package com.example.SinhVien5T.campaign.repository;

import com.example.SinhVien5T.campaign.entity.Criteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CriteriaRepository extends JpaRepository<Criteria, Long> {

}
