package com.HuyHoang.Controller;


import com.HuyHoang.DTO.request.EmailOtpVerifyRequest;
import com.HuyHoang.DTO.request.LoginRequest;
import com.HuyHoang.DTO.request.RegisterRequest;
import com.HuyHoang.DTO.response.LoginResponse;
import com.HuyHoang.DTO.response.RegisterEmailVeirifyResponse;
import com.HuyHoang.DTO.response.VerifyEmailResponse;
import com.HuyHoang.Entity.Permission;
import com.HuyHoang.Entity.Role;
import com.HuyHoang.Entity.User;
import com.HuyHoang.exception.AppException;
import com.HuyHoang.exception.ErrorCode;
import com.HuyHoang.mapper.UserMapper;
import com.HuyHoang.repository.RoleRepository;
import com.HuyHoang.service.AuthenticationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
public class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationService authenticationService;

    private User user;

    private RegisterRequest registerRequest;


    private RegisterEmailVeirifyResponse registerEmailVeirifyResponse;

    private VerifyEmailResponse verifyEmailResponse;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private EmailOtpVerifyRequest emailOtpVerifyRequest;
    @BeforeEach
    void initData(){
        registerRequest = RegisterRequest.builder()
                .username("NguyenHuyen5")
                .email("nguyendu2004.anhuu@gmail.com")
                .lastName("Du")
                .firstName("Nguyen")
                .password("12345678")
                .dob(LocalDate.of(2004,9,23))
                .build();


        Role role = Role.builder()
                .name("USER")
                .permissions(Set.of(
                        Permission.builder().name("CREATE_PLAYLIST").build(),
                        Permission.builder().name("CREATE_POST").build(),
                        Permission.builder().name("SEARCH_TRACK").build()
                ))
                .build();

        Set<Role> roles = new HashSet<>();
        roles.add(role);

        String otp = "143257";
        String hashedOtp = passwordEncoder.encode(otp);
        user = User.builder()
                .username("NguyenHuyen5")
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .lastName("Du")
                .firstName("Nguyen")
                .emailVerified(false)
                .emailVerificationToken(hashedOtp)
                .emailVerificationTokenExpiryDate(LocalDateTime.now().plusMinutes(10))
                .roles(roles)
                .build();

        registerEmailVeirifyResponse = RegisterEmailVeirifyResponse.builder()
                .userResponse(userMapper.toUserRespone(user))
                .message("User registered. Please verify your email.")
                .build();
        emailOtpVerifyRequest = EmailOtpVerifyRequest.builder()
                .otp(otp)
                .email("nguyendu2004.anhuu@gmail.com")
                .build();

        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationTokenExpiryDate(null);
        verifyEmailResponse = VerifyEmailResponse.builder()
                .userResponse(userMapper.toUserRespone(user))
                .message("Email verified successfully.")
                .build();
    }



    @Test
    void register_validRequest_success() throws Exception {
        //GIVEN
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String content = objectMapper.writeValueAsString(registerRequest);
        Mockito.when(authenticationService.registerWithEmailVerify(ArgumentMatchers.any()))
                        .thenReturn(registerEmailVeirifyResponse);

        //WHEN, THEN
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(content))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(1000)
        );
        //THEN
    }

    @Test
    void register_emailInvalid_fail() throws Exception {
        //GIVEN
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        registerRequest.setEmail("");
        String content = objectMapper.writeValueAsString(registerRequest);



        //WHEN, THEN
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(1025)
                ).andExpect(MockMvcResultMatchers.jsonPath("message").value("Email have not be blank")

                );
        //THEN
    }


    @Test
    void verify_validRequest_success() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String content = objectMapper.writeValueAsString(emailOtpVerifyRequest);
        Mockito.when(authenticationService.verifyRegisterEmail(ArgumentMatchers.any(),ArgumentMatchers.any()))
                .thenReturn(verifyEmailResponse);
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/verify-register-email")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(1000)
                );
    }
}
