package com.example.SinhVien5T.application.dto.response;

import com.example.SinhVien5T.evidence.dto.response.EvidenceItemDTO;
import com.example.SinhVien5T.evidence.entity.EvidenceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationCriteriaDetailResponse {

    private Long id;

    private String name;

    private String description;

    private Boolean isMandatory;

    private Integer requiredChildrenCount;

    private String evidenceType;

    private Long evidenceId;

    private String evidenceUrl;

    private String evidenceDescription;

    private EvidenceStatus evidenceStatus;

    private String reviewerComment;

    private Boolean isPassed;

    @Builder.Default
    private List<EvidenceItemDTO> evidences = new ArrayList<>();

    @Builder.Default
    private List<ApplicationCriteriaDetailResponse> subCriteriaList = new ArrayList<>();
}
