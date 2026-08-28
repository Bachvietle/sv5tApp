package com.example.SinhVien5T.user.mapper;

import com.example.SinhVien5T.user.dto.request.UpdateProfileRequest;
import com.example.SinhVien5T.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    // Tự động bỏ qua các trường null trong request, chỉ map các trường có giá trị
    void updateProfileFromRequest(UpdateProfileRequest request, @MappingTarget User user);

}
