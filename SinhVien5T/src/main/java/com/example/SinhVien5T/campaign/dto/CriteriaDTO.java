package com.example.SinhVien5T.campaign.dto;

import lombok.*;

import java.util.List;

/*
      Bao gồm:
      Ttin criteria    +     Ttin evidence (của riêng User)
      (Dữ liệu tĩnh)         (Dữ liệu động)
 */

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CriteriaDTO {

    // Ttin criteria (lấy từ bảng Criteria)
    private Long id;
    private String name;
    private String description;
    private Boolean isMandatory;
    private Integer requiredChildrenCount;
    private String evidenceType;


    // Ttin evidence (lấy từ bảng Evidence)
    private String evidenceUrl;
    private Boolean evidenceStatus;
    private String reviewerComment;

    private List<CriteriaDTO> subCriteriaList;

}
