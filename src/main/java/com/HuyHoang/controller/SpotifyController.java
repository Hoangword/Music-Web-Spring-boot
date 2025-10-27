package com.HuyHoang.controller;

import com.HuyHoang.DTO.response.ApiResponse;
import com.HuyHoang.DTO.spotifyResponse.SearchTrackResponse;
import com.HuyHoang.DTO.spotifyResponse.SpotifyTrackResponse;
import com.HuyHoang.SpotifyService.SpotifyApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/spotify")
@RequiredArgsConstructor
public class SpotifyController {
    private final SpotifyApiService spotifyApiService;

    @GetMapping("/track/{trackId}")
    public ApiResponse<SpotifyTrackResponse> getTrack(@PathVariable String trackId) {
        return ApiResponse.<SpotifyTrackResponse>builder()
                .result(spotifyApiService.getTrackInfo(trackId))
                .build();
    }

    @GetMapping("/search")
    public ApiResponse<List<SearchTrackResponse>> searchTracks(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "10") int limit) {

        return ApiResponse.<List<SearchTrackResponse>>builder()
                .result(spotifyApiService.searchTrack(keyword, limit))
                .build();
    }
}
