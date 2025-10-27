package com.HuyHoang.controller;

import com.HuyHoang.DTO.request.FavoriteSongRequest;
import com.HuyHoang.DTO.response.ApiResponse;
import com.HuyHoang.DTO.response.FavoriteSongResponse;
import com.HuyHoang.DTO.response.PlaylistResponse;
import com.HuyHoang.service.FavoriteSongsService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/favorite-songs")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FavoriteSongsController {
    FavoriteSongsService favoriteSongsService;

    @PostMapping()
    public ApiResponse<FavoriteSongResponse> addFavSong( @RequestBody FavoriteSongRequest request){
        return ApiResponse.<FavoriteSongResponse>builder()
                .result(favoriteSongsService.addFavoriteSong(request))
                .build();
    }

    @GetMapping()
    public ApiResponse<List<FavoriteSongResponse>> getFavoritesList(){
        return ApiResponse.<List<FavoriteSongResponse>>builder()
                .result(favoriteSongsService.getFavoriteSongs())
                .build();
    }

    @DeleteMapping("/{favoriteId}")
    public ApiResponse<String> removeFavorSong(@PathVariable String favoriteId){
        favoriteSongsService.removeFavoriteSong(favoriteId);
        return ApiResponse.<String>builder()
                .result("Song has been removed")
                .build();
    }

    @GetMapping("/pages")
    public ApiResponse<Page<FavoriteSongResponse>> getFavoriteSongPages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ){
        return ApiResponse.<Page<FavoriteSongResponse>>builder()
                .result(favoriteSongsService.getAllFavoriteSongPages(page,size,sortBy,direction))
                .build();
    }
}
