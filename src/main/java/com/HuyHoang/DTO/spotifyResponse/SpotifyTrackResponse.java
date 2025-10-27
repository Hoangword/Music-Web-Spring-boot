package com.HuyHoang.DTO.spotifyResponse;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SpotifyTrackResponse {
    private String trackName;
    private String artistName;
    private String albumImageUrl;
    private String previewUrl;
}
