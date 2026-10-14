package com.example.SinhVien5T.application.mapper;

import com.example.SinhVien5T.application.dto.response.ApplicationSummaryResponse;
import com.example.SinhVien5T.application.dto.response.StudentProfileSnapshot;
import com.example.SinhVien5T.application.entity.Application;
import com.example.SinhVien5T.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ApplicationMapper {

    @Mapping(target = "studentId", source = "student.id")
    @Mapping(target = "studentName", expression = "java(buildFullName(application.getStudent()))")
    @Mapping(target = "studentCode", source = "student.studentCode")
    @Mapping(target = "studentEmail", source = "student.email")
    @Mapping(target = "faculty", source = "student.faculty")
    @Mapping(target = "courseYear", source = "student.courseYear")
    @Mapping(target = "campaignId", source = "campaign.id")
    @Mapping(target = "campaignName", source = "campaign.name")
    @Mapping(target = "reviewerId", source = "reviewer.id")
    @Mapping(target = "reviewerName", expression = "java(buildFullName(application.getReviewer()))")
    ApplicationSummaryResponse toSummaryResponse(Application application);

    @Mapping(target = "studentId", source = "id")
    @Mapping(target = "fullName", expression = "java(buildFullName(user))")
    StudentProfileSnapshot toProfileSnapshot(User user);

    default String buildFullName(User user) {
        if (user == null) return null;
        String lastName = user.getLastName() != null ? user.getLastName().trim() : "";
        String firstName = user.getFirstName() != null ? user.getFirstName().trim() : "";
        String fullName = (lastName + " " + firstName).trim();
        return fullName.isEmpty() ? user.getUserName() : fullName;
    }
}
