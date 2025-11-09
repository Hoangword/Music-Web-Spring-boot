package com.HuyHoang.DTO.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TokenResponse {
     String accessToken;
     String refreshToken;
}
