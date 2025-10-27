package com.HuyHoang.DTO.response;

import com.HuyHoang.DTO.TokenPayload;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
}
