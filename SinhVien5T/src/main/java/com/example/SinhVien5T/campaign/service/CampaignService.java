package com.example.SinhVien5T.campaign.service;

import com.example.SinhVien5T.campaign.dto.CriteriaDTO;
import com.example.SinhVien5T.campaign.dto.StandardDTO;
import com.example.SinhVien5T.campaign.entity.Campaign;
import com.example.SinhVien5T.campaign.entity.Criteria;
import com.example.SinhVien5T.campaign.entity.Evidence;
import com.example.SinhVien5T.campaign.entity.Standard;
import com.example.SinhVien5T.campaign.repository.CampaignRepository;
import com.example.SinhVien5T.campaign.repository.EvidenceRepository;
import com.example.SinhVien5T.user.entity.CustomUserDetails;
import com.example.SinhVien5T.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CampaignService {

        private final CampaignRepository campaignRepository;
        private final EvidenceRepository evidenceRepository;


        public List<StandardDTO> getResultTree(Long campaignId) {

                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

                CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

                Campaign campaign = campaignRepository.findById(campaignId)
                                .orElseThrow(() -> new RuntimeException(""));

                List<Evidence> evidenceList = evidenceRepository.findByUserAndCampaign(user.getId(), campaign.getId());

                // Chuyển sang map (criteria_id + Evi) để dễ tra cứu
                Map<Long, Evidence> evidenceMap = evidenceList.stream()
                                .collect(Collectors.toMap(
                                                evidence -> evidence.getCriteria().getId(),
                                                evidence -> evidence));


                // 1. Lấy toàn bộ criteria phẳng
                List<Criteria> criteriaList = campaignRepository.findAllCriteria(campaignId);

                // 2. Map sang DTO  (chưa cần quan tâm đến cha - con)
                Map<Long, CriteriaDTO> criteriaDTOMap = criteriaList.stream()
                                .collect(Collectors.toMap(
                                                c -> c.getId(),
                                                c -> convertToCriteriaDTO(c, evidenceMap)));


                // 3. Ktra lại, nếu criteria nào có cha, thêm con vào subList của cha DTO
                for (Criteria c : criteriaList) {
                    if (c.getParent() != null) {
                        CriteriaDTO parent = criteriaDTOMap.get(c.getParent().getId());
                        CriteriaDTO child = criteriaDTOMap.get(c.getId());

                        parent.getSubCriteriaList().add(child);
                    }
                }

                // 4. Chia thành các List<CriteriaDTO> theo standardId (lát nữa setCriteriaDTOList của từng standard
                // chỉ cần tra cứu trong map này)
                Map<Long, List<CriteriaDTO>> rootCriteriaMap  = criteriaList.stream()
                        .filter(c -> c.getParent() == null)
                        .collect(Collectors.groupingBy(
                                c -> c.getStandard().getId(),

                                // đóng thành List<CriteriaDTO>
                                Collectors.mapping(c -> criteriaDTOMap.get(c.getId()), Collectors.toList())
                        ));

                // 5. Bắt đầu Build cây
                List<StandardDTO> resultTree = new ArrayList<>();

                for (Standard standard : campaign.getStandards()) {
                        StandardDTO standardDTO = new StandardDTO();

                        standardDTO.setId(standard.getId());
                        standardDTO.setName(standard.getName());

                        List<CriteriaDTO> criteriaDTOListForStandard = rootCriteriaMap.getOrDefault(standard.getId(), new ArrayList<>());

                        standardDTO.setCriteriaDTOList(criteriaDTOListForStandard);

                        resultTree.add(standardDTO);
                }

                return resultTree;

        }

        public CriteriaDTO convertToCriteriaDTO(Criteria criteria, Map<Long, Evidence> evidenceMap) {

                if (criteria == null) {
                        return null;
                }

                Evidence evidence = evidenceMap.get(criteria.getId());

                String url = evidence != null ? evidence.getEvidenceUrl() : null;
                Boolean status = evidence != null ? evidence.getStatus() : null;
                String comment = evidence != null ? evidence.getReviewerComment() : null;

                return CriteriaDTO.builder()
                                .id(criteria.getId())
                                .name(criteria.getName())
                                .description(criteria.getDescription())
                                .isMandatory(criteria.getIsMandatory())
                                .requiredChildrenCount(criteria.getRequiredChildrenCount())
                                .evidenceType(criteria.getEvidenceType().toString())

                                .evidenceUrl(url)
                                .evidenceStatus(status)
                                .reviewerComment(comment)

                                .subCriteriaList(new ArrayList<>())
                                .build();
        }

                      // -----------------DON'T CARE---------------------//
     /*
                                       Plan lấy cây 2

    public List<StandardDTO> getResultTree2(Long campaignId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        User user = (User) authentication.getPrincipal();

        Campaign campaign = campaignRepository.findCampaignWithDetails(campaignId)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        List<Evidence> evidenceList = evidenceRepository.findByUserAndCampaign(user.getId(), campaign.getId());

        // Chuyển sang map (criteria_id + Evi) để dễ tra cứu
        Map<Long, Evidence> evidenceMap = evidenceList.stream()
                .collect(Collectors.toMap(
                        evidence -> evidence.getCriteria().getId(),
                        evidence -> evidence));

        // Bắt đầu build cây DTO
        List<StandardDTO> resultTree = new ArrayList<>();

        for (Standard standard : campaign.getStandards()) {
            StandardDTO standardDTO = new StandardDTO();

            standardDTO.setId(standard.getId());
            standardDTO.setName(standard.getName());

            List<CriteriaDTO> criteriaDTOList = standard.getCriteriaList().stream()
                    .map(c -> convertToCriteriaDTO(c, evidenceMap))
                    .collect(Collectors.toList());

            standardDTO.setCriteriaDTOList(criteriaDTOList);

            resultTree.add(standardDTO);
        }

        return resultTree;

    }

    public CriteriaDTO convertToCriteriaDTO2(Criteria criteria, Map<Long, Evidence> evidenceMap) {

        if (criteria == null) {
            return null;
        }

        List<CriteriaDTO> subListDto = new ArrayList<>();

        if (criteria.getCriteriaList() != null && !criteria.getCriteriaList().isEmpty()) {
            subListDto = criteria.getCriteriaList().stream()
                    .map(c -> convertToCriteriaDTO(c, evidenceMap))
                    .toList();
        }

        Evidence evidence = evidenceMap.get(criteria.getId());

        String url = evidence != null ? evidence.getEvidenceUrl() : null;
        String status = evidence != null ? evidence.getStatus() : null;
        String comment = evidence != null ? evidence.getReviewerComment() : null;

        return CriteriaDTO.builder()
                .id(criteria.getId())
                .name(criteria.getName())
                .description(criteria.getDescription())
                .isMandatory(criteria.getIsMandatory())
                .requiredChildrenCount(criteria.getRequiredChildrenCount())
                .evidenceType(criteria.getEvidenceType().toString())

                .evidenceUrl(url)
                .evidenceStatus(status)
                .reviewerComment(comment)

                .subCriteriaList(subListDto)
                .build();
    }

     */
}
