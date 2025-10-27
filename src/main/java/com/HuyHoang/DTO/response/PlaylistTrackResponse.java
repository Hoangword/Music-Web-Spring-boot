package com.HuyHoang.DTO.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PlaylistTrackResponse {
    String id;
    String playlistId;
    String trackId;

    String trackName;
    String artistName;
    String albumImageUrl;
    String previewUrl;
}
