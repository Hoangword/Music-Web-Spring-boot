package com.HuyHoang.DTO.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TokenVerificationResponse {
    boolean isValid;
    String username;
    String jwtId;
    Set<String> permissions;
    String errorMessage;
}
