package com.example.SinhVien5T.evidence.mapper;

import com.example.SinhVien5T.evidence.dto.response.EvidenceAdminDTO;
import com.example.SinhVien5T.evidence.entity.Evidence;
import com.example.SinhVien5T.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EvidenceMapper {

    @Mapping(target = "studentId", source = "user.id")
    @Mapping(target = "studentName", expression = "java(buildFullName(evidence.getUser()))")
    @Mapping(target = "studentMssv", source = "user.studentCode")
    @Mapping(target = "studentFaculty", source = "user.faculty")
    @Mapping(target = "criteriaId", source = "criteria.id")
    @Mapping(target = "criteriaName", source = "criteria.name")
    @Mapping(target = "standardName", source = "criteria.standard.name")
    EvidenceAdminDTO toEvidenceAdminDTO(Evidence evidence);

    default String buildFullName(User user) {
        if (user == null) return "";
        String lastName = user.getLastName() != null ? user.getLastName().trim() : "";
        String firstName = user.getFirstName() != null ? user.getFirstName().trim() : "";
        String fullName = (lastName + " " + firstName).trim();
        return fullName.isEmpty() ? user.getUserName() : fullName;
    }
}
