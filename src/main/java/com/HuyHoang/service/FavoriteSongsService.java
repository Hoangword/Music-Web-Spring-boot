package com.HuyHoang.service;

import com.HuyHoang.DTO.request.FavoriteSongRequest;
import com.HuyHoang.DTO.response.FavoriteSongResponse;
import com.HuyHoang.Entity.FavoriteSongs;
import com.HuyHoang.Entity.User;
import com.HuyHoang.SpotifyService.SpotifyApiService;
import com.HuyHoang.exception.AppException;
import com.HuyHoang.exception.ErrorCode;
import com.HuyHoang.mapper.FavoriteSongMapper;
import com.HuyHoang.repository.FavoriteSongsRepository;
import com.HuyHoang.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FavoriteSongsService {

    FavoriteSongsRepository favoriteSongsRepository;
    FavoriteSongMapper favoriteSongMapper;
    UserRepository userRepository;
    SpotifyApiService spotifyApiService;
    public FavoriteSongResponse addFavoriteSong(FavoriteSongRequest request){
        var context = SecurityContextHolder.getContext();
        var name = context.getAuthentication().getName();
        User user = userRepository.findByUsername(name)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        FavoriteSongs favoriteSongs = favoriteSongMapper.toFavoriteSongs(request);
        favoriteSongs.setUser(user);

        return favoriteSongMapper.toFavoriteSongResponse(favoriteSongsRepository.save(favoriteSongs));
    }

    public List<FavoriteSongResponse> getFavoriteSongs(){

        var context = SecurityContextHolder.getContext();
        var name = context.getAuthentication().getName();

        return favoriteSongsRepository.findByUserId(
                userRepository.findByUsername(name).orElseThrow(()-> new AppException(ErrorCode.USER_NOT_EXISTED)).getId()
        ).stream().map( Songs -> {
                var trackInfo = spotifyApiService.getTrackInfo(Songs.getSpotifyTrackId());
                return FavoriteSongResponse.builder()
                        .id(Songs.getId())
                        .title(trackInfo.getTrackName())
                        .spotifyTrackId(Songs.getSpotifyTrackId())
                        .artist(trackInfo.getArtistName())
                        .albumImageUrl(trackInfo.getAlbumImageUrl())
                        .build();
                }
        ).toList();
    }

    public Page<FavoriteSongResponse> getAllFavoriteSongPages(int page, int size, String sortBy, String direction){

        Sort sort = direction.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page,size,sort);

        SecurityContext context = SecurityContextHolder.getContext();
        String username = context.getAuthentication().getName();

        User user = userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return favoriteSongsRepository.findByUser(user,pageable).map( Songs ->
                {
                    var trackInfo = spotifyApiService.getTrackInfo(Songs.getSpotifyTrackId());
                    return FavoriteSongResponse.builder()
                            .id(Songs.getId())
                            .title(trackInfo.getTrackName())
                            .spotifyTrackId(Songs.getSpotifyTrackId())
                            .artist(trackInfo.getArtistName())
                            .albumImageUrl(trackInfo.getAlbumImageUrl())
                            .build();
                }
                );
    }

    public void removeFavoriteSong(String id){
        favoriteSongsRepository.deleteById(id);
    }
}
