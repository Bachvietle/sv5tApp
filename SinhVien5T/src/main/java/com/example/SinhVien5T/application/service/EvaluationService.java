package com.example.SinhVien5T.application.service;

import com.example.SinhVien5T.application.dto.response.ApplicationCriteriaDetailResponse;
import com.example.SinhVien5T.application.dto.response.ApplicationStandardDetailResponse;
import com.example.SinhVien5T.application.dto.response.StandardEvaluationResult;
import com.example.SinhVien5T.campaign.entity.Campaign;
import com.example.SinhVien5T.campaign.entity.Criteria;
import com.example.SinhVien5T.campaign.entity.Standard;
import com.example.SinhVien5T.campaign.repository.CampaignRepository;
import com.example.SinhVien5T.common.exception.ResourceNotFoundException;
import com.example.SinhVien5T.evidence.dto.response.EvidenceItemDTO;
import com.example.SinhVien5T.evidence.entity.Evidence;
import com.example.SinhVien5T.evidence.entity.EvidenceStatus;
import com.example.SinhVien5T.evidence.entity.EvidenceType;
import com.example.SinhVien5T.evidence.repository.EvidenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EvaluationService {

    private final CampaignRepository campaignRepository;
    private final EvidenceRepository evidenceRepository;

    @Transactional(readOnly = true)
    public List<StandardEvaluationResult> evaluateStandardsSummary(Long userId, Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt xét tuyển"));

        List<Evidence> evidenceList = evidenceRepository.findByUserAndCampaign(userId, campaignId);
        Map<Long, List<Evidence>> evidenceMap = evidenceList.stream()
                .collect(Collectors.groupingBy(e -> e.getCriteria().getId()));

        List<Criteria> allCriteria = campaignRepository.findAllCriteria(campaignId);
        Map<Long, List<Criteria>> childrenMap = allCriteria.stream()
                .filter(c -> c.getParent() != null)
                .collect(Collectors.groupingBy(c -> c.getParent().getId()));

        Map<Long, List<Criteria>> rootCriteriaByStandardMap = allCriteria.stream()
                .filter(c -> c.getParent() == null)
                .collect(Collectors.groupingBy(c -> c.getStandard().getId()));

        List<StandardEvaluationResult> results = new ArrayList<>();

        for (Standard standard : campaign.getStandards()) {
            List<Criteria> rootCriteria = rootCriteriaByStandardMap.getOrDefault(standard.getId(), Collections.emptyList());

            StandardEvaluationResult result = evaluateStandard(standard, rootCriteria, childrenMap, evidenceMap);
            results.add(result);
        }

        return results;
    }

    @Transactional(readOnly = true)
    public List<ApplicationStandardDetailResponse> evaluateStandardsDetail(Long userId, Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt xét tuyển"));

        List<Evidence> evidenceList = evidenceRepository.findByUserAndCampaign(userId, campaignId);
        Map<Long, List<Evidence>> evidenceMap = evidenceList.stream()
                .collect(Collectors.groupingBy(e -> e.getCriteria().getId()));

        List<Criteria> allCriteria = campaignRepository.findAllCriteria(campaignId);
        Map<Long, List<Criteria>> childrenMap = allCriteria.stream()
                .filter(c -> c.getParent() != null)
                .collect(Collectors.groupingBy(c -> c.getParent().getId()));

        Map<Long, List<Criteria>> rootCriteriaByStandardMap = allCriteria.stream()
                .filter(c -> c.getParent() == null)
                .collect(Collectors.groupingBy(c -> c.getStandard().getId()));

        List<ApplicationStandardDetailResponse> standardResponses = new ArrayList<>();

        for (Standard standard : campaign.getStandards()) {
            List<Criteria> rootCriteria = rootCriteriaByStandardMap.getOrDefault(standard.getId(), Collections.emptyList());

            StandardEvaluationResult eval = evaluateStandard(standard, rootCriteria, childrenMap, evidenceMap);

            List<ApplicationCriteriaDetailResponse> criteriaResponses = rootCriteria.stream()
                    .map(c -> toCriteriaDetailResponse(c, childrenMap, evidenceMap))
                    .collect(Collectors.toList());

            ApplicationStandardDetailResponse standardDetail = ApplicationStandardDetailResponse.builder()
                    .id(standard.getId())
                    .name(standard.getName())
                    .description(standard.getDescription())
                    .isPassed(eval.getIsPassed())
                    .mandatoryPassedCount(eval.getMandatoryPassedCount())
                    .mandatoryTotalCount(eval.getMandatoryTotalCount())
                    .isMandatoryPassed(eval.getIsMandatoryPassed())
                    .optionalPassedCount(eval.getOptionalPassedCount())
                    .optionalTotalCount(eval.getOptionalTotalCount())
                    .isOptionalPassed(eval.getIsOptionalPassed())
                    .criteriaList(criteriaResponses)
                    .build();

            standardResponses.add(standardDetail);
        }

        return standardResponses;
    }

    private StandardEvaluationResult evaluateStandard(
            Standard standard,
            List<Criteria> rootCriteria,
            Map<Long, List<Criteria>> childrenMap,
            Map<Long, List<Evidence>> evidenceMap) {

        List<Criteria> mandatoryList = new ArrayList<>();
        List<Criteria> optionalList = new ArrayList<>();

        for (Criteria c : rootCriteria) {
            if (Boolean.TRUE.equals(c.getIsMandatory())) {
                mandatoryList.add(c);
            } else {
                optionalList.add(c);
            }
        }

        int mandatoryTotal = mandatoryList.size();
        int mandatoryPassed = (int) mandatoryList.stream()
                .filter(c -> isCriteriaPassed(c, childrenMap, evidenceMap))
                .count();
        boolean isMandatoryPassed = (mandatoryPassed == mandatoryTotal);

        int optionalTotal = optionalList.size();
        int optionalPassed = (int) optionalList.stream()
                .filter(c -> isCriteriaPassed(c, childrenMap, evidenceMap))
                .count();
        // Có tiêu chí tự chọn: phải đạt ít nhất 1 cái. Không có tiêu chí tự chọn: tính là qua.
        boolean isOptionalPassed = (optionalTotal == 0 || optionalPassed >= 1);

        boolean isPassed = isMandatoryPassed && isOptionalPassed;

        return StandardEvaluationResult.builder()
                .standardId(standard.getId())
                .standardName(standard.getName())
                .isPassed(isPassed)
                .mandatoryPassedCount(mandatoryPassed)
                .mandatoryTotalCount(mandatoryTotal)
                .isMandatoryPassed(isMandatoryPassed)
                .optionalPassedCount(optionalPassed)
                .optionalTotalCount(optionalTotal)
                .isOptionalPassed(isOptionalPassed)
                .build();
    }

    public boolean isCriteriaPassed(
            Criteria criteria,
            Map<Long, List<Criteria>> childrenMap,
            Map<Long, List<Evidence>> evidenceMap) {

        if (criteria == null) {
            return false;
        }

        List<Criteria> children = childrenMap.getOrDefault(criteria.getId(), Collections.emptyList());

        // Nếu là tiêu chí lá (không có con)
        if (children.isEmpty()) {
            if (criteria.getEvidenceType() == EvidenceType.NONE) {
                return true;
            }
            List<Evidence> list = evidenceMap.getOrDefault(criteria.getId(), Collections.emptyList());
            // Có ít nhất 1 minh chứng được APPROVED
            return list.stream().anyMatch(e -> e.getStatus() == EvidenceStatus.APPROVED);
        }

        // Nếu có tiêu chí con
        List<Criteria> mandatoryChildren = children.stream()
                .filter(c -> Boolean.TRUE.equals(c.getIsMandatory()))
                .toList();

        List<Criteria> optionalChildren = children.stream()
                .filter(c -> !Boolean.TRUE.equals(c.getIsMandatory()))
                .toList();

        boolean mandatoryChildrenPassed = mandatoryChildren.stream()
                .allMatch(c -> isCriteriaPassed(c, childrenMap, evidenceMap));

        int totalPassedChildren = (int) children.stream()
                .filter(c -> isCriteriaPassed(c, childrenMap, evidenceMap))
                .count();

        int requiredCount = criteria.getRequiredChildrenCount() != null ? criteria.getRequiredChildrenCount() : 0;

        boolean countSatisfied;
        if (requiredCount > 0) {
            countSatisfied = totalPassedChildren >= requiredCount;
        } else if (!optionalChildren.isEmpty()) {
            countSatisfied = optionalChildren.stream().anyMatch(c -> isCriteriaPassed(c, childrenMap, evidenceMap));
        } else {
            countSatisfied = true;
        }

        return mandatoryChildrenPassed && countSatisfied;
    }

    private ApplicationCriteriaDetailResponse toCriteriaDetailResponse(
            Criteria criteria,
            Map<Long, List<Criteria>> childrenMap,
            Map<Long, List<Evidence>> evidenceMap) {

        List<Evidence> list = evidenceMap.getOrDefault(criteria.getId(), Collections.emptyList());

        List<EvidenceItemDTO> itemDTOs = list.stream()
                .map(e -> EvidenceItemDTO.builder()
                        .id(e.getId())
                        .evidenceUrl(e.getEvidenceUrl())
                        .description(e.getDescription())
                        .status(e.getStatus())
                        .reviewerComment(e.getReviewerComment())
                        .submittedAt(e.getSubmittedAt())
                        .submissionCount(e.getSubmissionCount())
                        .reviewedAt(e.getReviewedAt())
                        .build())
                .collect(Collectors.toList());

        Evidence primary = list.stream()
                .filter(e -> e.getStatus() == EvidenceStatus.APPROVED)
                .findFirst()
                .orElse(list.isEmpty() ? null : list.get(list.size() - 1));

        List<Criteria> children = childrenMap.getOrDefault(criteria.getId(), Collections.emptyList());
        List<ApplicationCriteriaDetailResponse> subList = children.stream()
                .map(child -> toCriteriaDetailResponse(child, childrenMap, evidenceMap))
                .collect(Collectors.toList());

        boolean passed = isCriteriaPassed(criteria, childrenMap, evidenceMap);

        return ApplicationCriteriaDetailResponse.builder()
                .id(criteria.getId())
                .name(criteria.getName())
                .description(criteria.getDescription())
                .isMandatory(criteria.getIsMandatory())
                .requiredChildrenCount(criteria.getRequiredChildrenCount())
                .evidenceType(criteria.getEvidenceType() != null ? criteria.getEvidenceType().name() : null)
                .evidenceId(primary != null ? primary.getId() : null)
                .evidenceUrl(primary != null ? primary.getEvidenceUrl() : null)
                .evidenceDescription(primary != null ? primary.getDescription() : null)
                .evidenceStatus(primary != null ? primary.getStatus() : null)
                .reviewerComment(primary != null ? primary.getReviewerComment() : null)
                .evidences(itemDTOs)
                .isPassed(passed)
                .subCriteriaList(subList)
                .build();
    }
}
