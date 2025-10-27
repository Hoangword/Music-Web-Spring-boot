package com.HuyHoang.DTO;

import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtInfo implements Serializable {
    private String jwtId;
    private Date issueTime;
    private Date expiredTime;
}
