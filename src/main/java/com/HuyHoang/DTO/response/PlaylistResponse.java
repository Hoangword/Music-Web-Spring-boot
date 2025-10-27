package com.HuyHoang.DTO.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PlaylistResponse {
    private String id;
    private String name;
    private String description;
    private String userId; // ID user sở hữu playlist
    private List<String> trackIds;
}
