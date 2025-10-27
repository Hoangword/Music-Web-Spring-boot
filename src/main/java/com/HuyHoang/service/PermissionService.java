package com.HuyHoang.service;

import com.HuyHoang.DTO.request.PermissionRequest;
import com.HuyHoang.DTO.response.PermissionResponse;
import com.HuyHoang.Entity.Permission;
import com.HuyHoang.mapper.PermissionMapper;
import com.HuyHoang.repository.PermissionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionService {
    PermissionRepository permissionRepository;
    PermissionMapper permissionMapper;

     public PermissionResponse createPermission(PermissionRequest request){
        Permission permission = permissionMapper.toPermission(request);

        return permissionMapper.toPermissionResponse(permissionRepository.save(permission));
    }

    public List<PermissionResponse> getAllPermissions(){
        return permissionRepository.findAll().stream().map(permissionMapper::toPermissionResponse).toList();
    }

    public void deletePermission(String permission){
        permissionRepository.deleteById(permission);
    }
}
