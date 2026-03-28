package com.example.SinhVien5T.campaign.dto;

import lombok.*;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StandardDTO {

    private Long id;

    private String name;

    private String description;

    private List<CriteriaDTO> criteriaDTOList;
}
