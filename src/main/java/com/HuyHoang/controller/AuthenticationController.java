package com.HuyHoang.controller;

import com.HuyHoang.DTO.request.*;
import com.HuyHoang.DTO.response.ApiResponse;
import com.HuyHoang.DTO.response.AuthenticationResponse;
import com.HuyHoang.DTO.response.IntrospectResponse;
import com.HuyHoang.DTO.response.LoginResponse;
import com.HuyHoang.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticationController {
    AuthenticationService authenticationService;

    @PostMapping("/token")
    ApiResponse<AuthenticationResponse> authentication(@RequestBody AuthenticationRequest request){
        var result = authenticationService.authenticate(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }

//    @PostMapping("/login")
//    ApiResponse<LoginResponse> login(@RequestBody LoginRequest request){
//
//        return ApiResponse.<LoginResponse>builder()
//                .result(
//                        authenticationService.login(request)
//                )
//                .build();
//    }

    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> authentication(@RequestBody IntrospectRequest request) throws ParseException, JOSEException {
        var result = authenticationService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/refresh")
    ApiResponse<AuthenticationResponse> refreshToken(@RequestBody RefreshRequest request)
            throws ParseException, JOSEException {
        return ApiResponse.<AuthenticationResponse>builder()
                .result(authenticationService.refreshToken(request))
                .build();
    }
    @PostMapping("/logout")
    ApiResponse<Void> logout(@RequestBody LogoutRequest request) throws ParseException, JOSEException {
        authenticationService.logout(request);
        return ApiResponse.<Void>builder().build();
    }

//    @PostMapping("/logout")
//    ApiResponse<Void> logout(@RequestHeader("Authorization") LogoutRequest request) throws ParseException, JOSEException {
//        log.info("token:{}", request.getToken());
//        String token = request.getToken().replace("Bearer ", "");
//        authenticationService.logout(token);
//        return ApiResponse.<Void>builder()
//                .build();
//    }
}
