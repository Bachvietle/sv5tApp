package com.example.SinhVien5T.campaign.service;

import com.example.SinhVien5T.campaign.dto.CampaignFilterRequest;
import com.example.SinhVien5T.campaign.dto.CampaignResponse;
import com.example.SinhVien5T.campaign.dto.CampaignStatusUpdateRequest;
import com.example.SinhVien5T.campaign.dto.CampaignYearGroupResponse;
import com.example.SinhVien5T.campaign.dto.CriteriaDTO;
import com.example.SinhVien5T.campaign.dto.StandardDTO;
import com.example.SinhVien5T.campaign.entity.Campaign;
import com.example.SinhVien5T.campaign.entity.CampaignStatus;
import com.example.SinhVien5T.campaign.entity.Criteria;
import com.example.SinhVien5T.campaign.entity.Standard;
import com.example.SinhVien5T.campaign.mapper.CampaignMapper;
import com.example.SinhVien5T.campaign.repository.CampaignRepository;
import com.example.SinhVien5T.campaign.repository.specification.CampaignSpecification;
import com.example.SinhVien5T.common.dto.response.PageResponse;
import com.example.SinhVien5T.common.exception.ResourceNotFoundException;
import com.example.SinhVien5T.evidence.dto.response.EvidenceItemDTO;
import com.example.SinhVien5T.evidence.entity.Evidence;
import com.example.SinhVien5T.evidence.entity.EvidenceStatus;
import com.example.SinhVien5T.evidence.repository.EvidenceRepository;
import com.example.SinhVien5T.notification.service.EmailService;
import com.example.SinhVien5T.user.entity.CustomUserDetails;
import com.example.SinhVien5T.user.entity.Role;
import com.example.SinhVien5T.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CampaignService {

        private final CampaignRepository campaignRepository;
        private final EvidenceRepository evidenceRepository;
        private final UserRepository userRepository;
        private final EmailService emailService;
        private final CampaignMapper campaignMapper;


        public List<StandardDTO> getResultTree(Long campaignId) {

                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

                CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

                Campaign campaign = campaignRepository.findById(campaignId)
                                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt xét tuyển"));

                List<Evidence> evidenceList = evidenceRepository.findByUserAndCampaign(user.getId(), campaign.getId());

                // Chuyển sang map (criteria_id + List<Evidence>) để hỗ trợ nhiều minh chứng
                Map<Long, List<Evidence>> evidenceMap = evidenceList.stream()
                                .collect(Collectors.groupingBy(e -> e.getCriteria().getId()));


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

        public CriteriaDTO convertToCriteriaDTO(Criteria criteria, Map<Long, List<Evidence>> evidenceMap) {

                if (criteria == null) {
                        return null;
                }

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

                // Lấy minh chứng đại diện (ưu tiên APPROVED, nếu không thì lấy bản nộp mới nhất)
                Evidence primary = list.stream()
                                .filter(e -> e.getStatus() == EvidenceStatus.APPROVED)
                                .findFirst()
                                .orElse(list.isEmpty() ? null : list.get(list.size() - 1));

                String url = primary != null ? primary.getEvidenceUrl() : null;
                EvidenceStatus status = primary != null ? primary.getStatus() : null;
                String comment = primary != null ? primary.getReviewerComment() : null;

                return CriteriaDTO.builder()
                                .id(criteria.getId())
                                .name(criteria.getName())
                                .description(criteria.getDescription())
                                .isMandatory(criteria.getIsMandatory())
                                .requiredChildrenCount(criteria.getRequiredChildrenCount())
                                .evidenceType(criteria.getEvidenceType() != null ? criteria.getEvidenceType().name() : null)

                                .evidenceUrl(url)
                                .evidenceStatus(status)
                                .reviewerComment(comment)
                                .evidences(itemDTOs)

                                .subCriteriaList(new ArrayList<>())
                                .build();
        }

        public boolean isCampaignOpen(Campaign campaign) {
                if (campaign == null || campaign.getStatus() != CampaignStatus.ACTIVE) {
                        return false;
                }
                LocalDate today = LocalDate.now();
                if (campaign.getStartDate() != null && today.isBefore(campaign.getStartDate())) {
                        return false;
                }
                if (campaign.getEnd_date() != null && today.isAfter(campaign.getEnd_date())) {
                        return false;
                }
                return true;
        }

        public Campaign getActiveCampaign() {
                List<Campaign> activeCampaigns = campaignRepository.findByStatus(CampaignStatus.ACTIVE);
                for (Campaign c : activeCampaigns) {
                        if (isCampaignOpen(c)) {
                                return c;
                        }
                }
                return activeCampaigns.stream().findFirst()
                                .orElseThrow(() -> new ResourceNotFoundException("Hiện không có đợt xét tuyển nào đang mở"));
        }

        @Transactional
        public Campaign updateCampaignStatus(Long campaignId, CampaignStatusUpdateRequest request) {
                Campaign campaign = campaignRepository.findById(campaignId)
                                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt xét tuyển"));

                CampaignStatus oldStatus = campaign.getStatus();
                CampaignStatus newStatus = request.getStatus();
                campaign.setStatus(newStatus);
                Campaign saved = campaignRepository.save(campaign);

                if (newStatus == CampaignStatus.ACTIVE && oldStatus != CampaignStatus.ACTIVE
                                && Boolean.TRUE.equals(request.getSendNotification())) {
                        broadcastCampaignOpenedEmail(saved);
                }

                return saved;
        }

        @Transactional
        public void autoCheckCampaignStatus() {
                LocalDate today = LocalDate.now();

                // 1. Tự động mở các đợt đến ngày mở
                List<Campaign> toOpen = campaignRepository.findCampaignsToAutoOpen(today, CampaignStatus.ACTIVE);
                for (Campaign campaign : toOpen) {
                        campaign.setStatus(CampaignStatus.ACTIVE);
                        campaignRepository.save(campaign);
                        broadcastCampaignOpenedEmail(campaign);
                }

                // 2. Tự động đóng các đợt quá hạn
                List<Campaign> toClose = campaignRepository.findCampaignsToAutoClose(today, CampaignStatus.ACTIVE);
                for (Campaign campaign : toClose) {
                        campaign.setStatus(CampaignStatus.CLOSED);
                        campaignRepository.save(campaign);
                }
        }

        private void broadcastCampaignOpenedEmail(Campaign campaign) {
                List<String> studentEmails = userRepository.findEmailsByRoleAndIsActiveTrue(Role.USER);
                if (!studentEmails.isEmpty()) {
                        emailService.sendCampaignAnnouncementToStudents(
                                        campaign.getName(),
                                        campaign.getLevel() != null ? campaign.getLevel().name() : "Toàn trường",
                                        campaign.getAcademicYear(),
                                        campaign.getStartDate(),
                                        campaign.getEnd_date(),
                                        studentEmails
                        );
                }
        }

        public CampaignResponse toCampaignResponse(Campaign campaign) {
                if (campaign == null) {
                        return null;
                }
                CampaignResponse response = campaignMapper.toCampaignResponse(campaign);
                response.setIsOpen(isCampaignOpen(campaign));
                return response;
        }

        private int getStatusPriority(CampaignStatus status) {
                if (status == null) return 5;
                return switch (status) {
                        case ACTIVE -> 1;
                        case UPCOMING -> 2;
                        case PAUSED -> 3;
                        case CLOSED -> 4;
                };
        }

        @Transactional(readOnly = true)
        public List<CampaignYearGroupResponse> getCampaignsGroupedByYear(CampaignFilterRequest request) {
                Specification<Campaign> spec = CampaignSpecification.filterCampaigns(request);
                List<Campaign> list = campaignRepository.findAll(spec);

                Comparator<CampaignResponse> campaignComparator = Comparator
                                .comparingInt((CampaignResponse c) -> getStatusPriority(c.getStatus()))
                                .thenComparing(c -> c.getStartDate() != null ? c.getStartDate() : LocalDate.MIN, Comparator.reverseOrder())
                                .thenComparing(CampaignResponse::getId, Comparator.reverseOrder());

                Map<Long, List<CampaignResponse>> groupedMap = list.stream()
                                .map(this::toCampaignResponse)
                                .collect(Collectors.groupingBy(
                                                c -> c.getAcademicYear() != null ? c.getAcademicYear() : 0L,
                                                () -> new TreeMap<Long, List<CampaignResponse>>(Comparator.reverseOrder()),
                                                Collectors.toList()
                                ));

                List<CampaignYearGroupResponse> result = new ArrayList<>();
                for (Map.Entry<Long, List<CampaignResponse>> entry : groupedMap.entrySet()) {
                        List<CampaignResponse> sortedList = entry.getValue().stream()
                                        .sorted(campaignComparator)
                                        .collect(Collectors.toList());
                        result.add(new CampaignYearGroupResponse(entry.getKey(), sortedList));
                }

                return result;
        }

        @Transactional(readOnly = true)
        public PageResponse<CampaignResponse> getCampaignsPage(CampaignFilterRequest request) {
                String sortBy = request.getSortBy() != null ? request.getSortBy() : "academicYear";
                Sort.Direction direction = "ASC".equalsIgnoreCase(request.getSortDirection()) ? Sort.Direction.ASC : Sort.Direction.DESC;

                Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), Sort.by(direction, sortBy));
                Specification<Campaign> spec = CampaignSpecification.filterCampaigns(request);

                Page<Campaign> page = campaignRepository.findAll(spec, pageable);
                Page<CampaignResponse> dtoPage = page.map(this::toCampaignResponse);

                return new PageResponse<>(
                                dtoPage.getContent(),
                                dtoPage.getNumber(),
                                dtoPage.getSize(),
                                dtoPage.getTotalElements(),
                                dtoPage.getTotalPages(),
                                dtoPage.isLast()
                );
        }

                      // -----------------DON'T CARE---------------------//
     /*
                                       Plan lấy cây 2

    public List<StandardDTO> getResultTree2(Long campaignId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

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
        EvidenceStatus status = evidence != null ? evidence.getStatus() : null;
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
