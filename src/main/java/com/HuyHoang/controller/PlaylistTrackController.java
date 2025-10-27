package com.HuyHoang.controller;

import com.HuyHoang.DTO.request.PlaylistTrackRequest;
import com.HuyHoang.DTO.response.ApiResponse;
import com.HuyHoang.DTO.response.PlaylistTrackResponse;
import com.HuyHoang.service.PlaylistTracksService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/playlist-track")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PlaylistTrackController {
    PlaylistTracksService playlistTracksService;

    @PostMapping
    ApiResponse<PlaylistTrackResponse> AddTrack(@RequestBody PlaylistTrackRequest request){
        return ApiResponse.<PlaylistTrackResponse>builder()
                .result(playlistTracksService.addTrackToPlaylist(request))
                .build();
    }

    @DeleteMapping("/{trackId}")
    ApiResponse<Void> removeTrack(@PathVariable String trackId){
        playlistTracksService.deletedTrackFromPlaylist(trackId);
        return ApiResponse.<Void>builder().build();
    }

    @GetMapping("/{playlistId}")
    ApiResponse<List<PlaylistTrackResponse>> getAllTrack(@PathVariable String playlistId){
        return ApiResponse.<List<PlaylistTrackResponse>>builder()
                .result(playlistTracksService.getAllTrackFromPlaylist(playlistId))
                .build();
    }

    @GetMapping("/admin/{playlistId}")
    ApiResponse<Page<PlaylistTrackResponse>> getAllPlaylistTrackPaged(
            @PathVariable String playlistId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "asc") String direction
    ){
        return ApiResponse.<Page<PlaylistTrackResponse>>builder()
                .result(
                        playlistTracksService.getAllPlaylistTracksPaged(playlistId,page,size,direction)
                )
                .build();
    }
}
