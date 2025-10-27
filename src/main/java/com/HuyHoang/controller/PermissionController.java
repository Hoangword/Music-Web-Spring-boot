package com.HuyHoang.controller;

import com.HuyHoang.DTO.request.PermissionRequest;
import com.HuyHoang.DTO.response.ApiResponse;
import com.HuyHoang.DTO.response.PermissionResponse;
import com.HuyHoang.service.PermissionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PermissionController {
    PermissionService permissionService;

    @PostMapping
     ApiResponse<PermissionResponse> create(@RequestBody PermissionRequest request){
        return ApiResponse.<PermissionResponse>builder()
                .result(permissionService.createPermission(request))
                .build();
    }

    @GetMapping
     ApiResponse<List<PermissionResponse>> getPermissions(){
        return ApiResponse.<List<PermissionResponse>>builder()
                .result(permissionService.getAllPermissions())
                .build();
    }

    @DeleteMapping("/{permission}")
     ApiResponse<Void> deletePermissions(@PathVariable String permission){
        permissionService.deletePermission(permission);
        return ApiResponse.<Void>builder()
                .build();
    }

}
