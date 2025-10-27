package com.HuyHoang.mapper;

import com.HuyHoang.DTO.request.RoleRequest;
import com.HuyHoang.DTO.response.RoleResponse;
import com.HuyHoang.Entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);
    RoleResponse toRoleResponse(Role role);
}
