package com.HuyHoang.configuration;

import com.HuyHoang.Entity.Role;
import com.HuyHoang.Entity.User;
import com.HuyHoang.exception.AppException;
import com.HuyHoang.exception.ErrorCode;
import com.HuyHoang.repository.RoleRepository;
import com.HuyHoang.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {

    PasswordEncoder passwordEncoder;
    RoleRepository roleRepository;
    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository){
        return args -> {
            if(userRepository.findByUsername("admin").isEmpty()){
                //var roles = new HashSet<com.HuyHoang.Entity.Role>();
                Role role = roleRepository.findByName("ADMIN")
                        .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));
                Set<Role> roles = new HashSet<>();
                roles.add(role);

                userRepository.save(User.builder()
                                .username("admin")
                                .password(passwordEncoder.encode("admin"))
                                .email("nguyenhuyhoangqn2017@gmail.com")
                                .emailVerified(true)
                                .roles(roles)
                        .build());
                log.warn("admin user has been created with default password: admin, please change it");
            }
        };
    }
}
