package com.HuyHoang.mapper;

import com.HuyHoang.DTO.request.PermissionRequest;
import com.HuyHoang.DTO.response.PermissionResponse;
import com.HuyHoang.Entity.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);
    PermissionResponse toPermissionResponse(Permission permission);

}
