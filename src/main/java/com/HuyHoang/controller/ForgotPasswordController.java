package com.HuyHoang.controller;

import com.HuyHoang.DTO.MailBody;
import com.HuyHoang.DTO.request.ChangePasswordRequest;
import com.HuyHoang.DTO.request.VerifyEmailRequest;
import com.HuyHoang.DTO.request.VerifyOtpRequest;
import com.HuyHoang.DTO.response.ApiResponse;
import com.HuyHoang.Entity.ForgotPassword;
import com.HuyHoang.Entity.User;
import com.HuyHoang.repository.ForgotPasswordRepository;
import com.HuyHoang.repository.UserRepository;
import com.HuyHoang.service.EmailService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Date;
import java.util.Random;

@RestController
@RequestMapping("/forgotPassword")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ForgotPasswordController {



    EmailService emailService;
    ForgotPasswordRepository forgotPasswordRepository;

    @PostMapping("/verifyMail/{email}")
    public ApiResponse<String> verifyEmail(@PathVariable String email){
        return ApiResponse.<String>builder().result(
                emailService.verifyEmail(email)
        ).build();
    }

    @PostMapping("/verifyOtp/{email}")
    public ApiResponse<String> verifyOtp(@RequestBody VerifyOtpRequest request, @PathVariable String email){
        return ApiResponse.<String>builder()
                .result(emailService.verifyOtpAndUser(request.getOtp(), email))
                .build();

    }


    @PostMapping("/changePassword/{email}")
    public ApiResponse<String> changePasswordHandler(@RequestBody ChangePasswordRequest request,
                                                     @PathVariable String email){
        return ApiResponse.<String>builder()
                .result(emailService.changePasswordHandler(request,email))
                .build();
    }


}
