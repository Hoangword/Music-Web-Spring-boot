package com.HuyHoang.controller;

import com.HuyHoang.DTO.request.UserCreationRequest;
import com.HuyHoang.DTO.request.UserUpdateRequest;
import com.HuyHoang.DTO.response.ApiResponse;
import com.HuyHoang.DTO.response.UserResponse;
import com.HuyHoang.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    UserService userService;

    @PostMapping
     ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreationRequest request){
        return ApiResponse.<UserResponse>builder()
                .result(userService.createUser(request))
                .build();

    }

    @PutMapping("/{userId}")
     ApiResponse<UserResponse> updateUser(@PathVariable String userId, @RequestBody UserUpdateRequest request){
        return ApiResponse.<UserResponse>builder().result(userService.updateUser(userId,request)).build();
    }

    @DeleteMapping("/{userId}")
     ApiResponse<String> deleteUser(@PathVariable String userId){
        userService.deletedUser(userId);
        return ApiResponse.<String>builder().result("User has been deleted").build();
    }

    @GetMapping("/admin/user")
    ApiResponse<Page<UserResponse>> getAllUserPages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "username") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ){
        return ApiResponse.<Page<UserResponse>>builder()
                .result(
                        userService.getAllUserPages(page,size,sortBy,direction)
                )
                .build();
    }

    @GetMapping
    ApiResponse<List<UserResponse>> getAllUsers(){

        var authentication = SecurityContextHolder.getContext().getAuthentication();

        log.info("Username: {}", authentication.getName());
        authentication.getAuthorities().forEach(grantedAuthority -> log.info(grantedAuthority.getAuthority()));


        return ApiResponse.<List<UserResponse>>builder().result(userService.getUsers()).build();
    }

    @GetMapping("/{userId}")
    ApiResponse<UserResponse> getUser(@PathVariable String userId){
        return ApiResponse.<UserResponse>builder().result(userService.getUser(userId)).build();
    }


    @GetMapping("/myInfo")
    ApiResponse<UserResponse> getMyInfo(){
        return ApiResponse.<UserResponse>builder().result(userService.getMyInfo()).build();
    }

    @GetMapping("admin/search")
    ApiResponse<Page<UserResponse>> searchUser(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String email
    ){
        return ApiResponse.<Page<UserResponse>>builder()
                .result(
                        userService.searchUser(keyword,role,email)
                )
                .build();
    }

}
