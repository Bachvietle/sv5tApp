package com.example.SinhVien5T.user.mapper;

import com.example.SinhVien5T.user.dto.request.UpdateProfileRequest;
import com.example.SinhVien5T.user.entity.User;
import com.example.SinhVien5T.user.dto.response.StudentDetailResponse;
import com.example.SinhVien5T.user.dto.response.StudentSummaryResponse;
import com.example.SinhVien5T.user.dto.response.UserProfileResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    // Tự động bỏ qua các trường null trong request, chỉ map các trường có giá trị
    void updateProfileFromRequest(UpdateProfileRequest request, @MappingTarget User user);

    @Mapping(target = "fullName", expression = "java(buildFullName(user))")
    @Mapping(target = "isProfileCompleted", source = "profileCompleted")
    StudentSummaryResponse toStudentSummaryResponse(User user);

    @Mapping(target = "fullName", expression = "java(buildFullName(user))")
    @Mapping(target = "isProfileCompleted", source = "profileCompleted")
    StudentDetailResponse toStudentDetailResponse(User user);

    UserProfileResponse toUserProfileResponse(User user);

    default String buildFullName(User user) {
        if (user == null) return "";
        String lastName = user.getLastName() != null ? user.getLastName().trim() : "";
        String firstName = user.getFirstName() != null ? user.getFirstName().trim() : "";
        String fullName = (lastName + " " + firstName).trim();
        return fullName.isEmpty() ? user.getUserName() : fullName;
    }
}
