package com.HuyHoang.mapper;

import com.HuyHoang.DTO.request.RegisterRequest;
import com.HuyHoang.DTO.request.UserCreationRequest;
import com.HuyHoang.DTO.request.UserUpdateRequest;
import com.HuyHoang.DTO.response.RegisterResponse;
import com.HuyHoang.DTO.response.UserResponse;
import com.HuyHoang.Entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper{
    User toUser(UserCreationRequest request);
    UserResponse toUserRespone(User user);

    User toUser(RegisterRequest request);

    RegisterResponse tuRegisterResponse(User user);

    @Mapping(target = "roles", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);

}
