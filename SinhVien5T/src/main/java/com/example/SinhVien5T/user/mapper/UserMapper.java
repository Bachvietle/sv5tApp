package com.example.SinhVien5T.user.mapper;

import com.example.SinhVien5T.auth.dto.request.UserUpdateProfileRequest;
import com.example.SinhVien5T.user.entity.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring") //giúp nó trở thành 1 Bean như bth
public interface UserMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserFromRequest(UserUpdateProfileRequest request, @MappingTarget User user);

}
