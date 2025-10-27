package com.HuyHoang.controller;

import com.HuyHoang.DTO.request.PlaylistCreationRequest;
import com.HuyHoang.DTO.request.PlaylistUpdateRequest;
import com.HuyHoang.DTO.response.ApiResponse;
import com.HuyHoang.DTO.response.PlaylistResponse;
import com.HuyHoang.Entity.User;
import com.HuyHoang.service.PlaylistService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/playlist")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PlaylistController {
    PlaylistService playlistService;

    @PostMapping()
    ApiResponse<PlaylistResponse> createPlaylist(@RequestBody PlaylistCreationRequest request){
        return ApiResponse.<PlaylistResponse>builder().result(
                playlistService.createPlaylist( request)
        ).build();
    }

    @PutMapping("/{playlistId}")
    ApiResponse<PlaylistResponse> updatePlayist(@PathVariable String playlistId,@RequestBody PlaylistUpdateRequest request){
        return ApiResponse.<PlaylistResponse>builder().result(
                playlistService.updatePlaylist(playlistId,request)
        ).build();
    }

    @DeleteMapping("/{playlistId}")
    ApiResponse<String> deletePlaylist(@PathVariable String playlistId){
        playlistService.deletePlaylist(playlistId);
        return ApiResponse.<String>builder()
                .result("Playlist has been deleted"
        ).build();
    }

    @GetMapping("/{playlistId}")
    ApiResponse<PlaylistResponse> getPlaylist(@PathVariable String playlistId){
        return ApiResponse.<PlaylistResponse>builder().result(
                playlistService.getPlaylist(playlistId)
        ).build();
    }

    @GetMapping()
    ApiResponse<List<PlaylistResponse>> getAllPlaylist(){
        return ApiResponse.<List<PlaylistResponse>>builder().result(
                playlistService.getAllPlaylists()
        ).build();
    }

    @GetMapping("/myPlaylist")
    ApiResponse<List<PlaylistResponse>> getMyPlaylist(){

        return ApiResponse.<List<PlaylistResponse>>builder().result(
                playlistService.getMyPlaylist()
        ).build();
    }

    @GetMapping("/admin/All")
    ApiResponse<List<PlaylistResponse>> getAllUserPlaylists(){
        return ApiResponse.<List<PlaylistResponse>>builder()
                .result(playlistService.getAllUserPlaylists())
                .build();
    }

    @GetMapping("/admin/user/{userId}")
    ApiResponse<List<PlaylistResponse>> getPlaylistOfUser(@PathVariable String userId){
        return ApiResponse.<List<PlaylistResponse>>builder()
                .result(playlistService.getPlaylistOfUser(userId))
                .build();
    }

    @GetMapping("/admin/All-Paged")
    ApiResponse<Page<PlaylistResponse>> getAllplaylistsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ){
        return ApiResponse.<Page<PlaylistResponse>>builder()
                .result(
                        playlistService.getAllPlaylistsPaged(page,size,sortBy,direction)
                )
                .build();

    }

    @GetMapping("/admin/user/paged/{userId}")
    ApiResponse<Page<PlaylistResponse>> getUserplaylistsPaged(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ){
        return ApiResponse.<Page<PlaylistResponse>>builder()
                .result(
                        playlistService.getUserPlaylistsPaged(userId,page,size,sortBy,direction)
                )
                .build();

    }

}
