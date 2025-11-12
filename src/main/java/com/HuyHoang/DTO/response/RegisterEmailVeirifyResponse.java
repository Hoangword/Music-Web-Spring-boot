package com.HuyHoang.DTO.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegisterEmailVeirifyResponse {
    String message;
    UserResponse userResponse;
}
