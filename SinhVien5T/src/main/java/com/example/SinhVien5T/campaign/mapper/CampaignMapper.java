package com.example.SinhVien5T.campaign.mapper;

import com.example.SinhVien5T.campaign.dto.CampaignResponse;
import com.example.SinhVien5T.campaign.entity.Campaign;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CampaignMapper {

    @Mapping(target = "endDate", source = "end_date")
    @Mapping(target = "isOpen", ignore = true)
    CampaignResponse toCampaignResponse(Campaign campaign);
}
