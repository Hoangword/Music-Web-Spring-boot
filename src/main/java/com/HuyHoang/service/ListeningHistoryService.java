package com.HuyHoang.service;

import com.HuyHoang.DTO.request.ListeningHistoryRequest;
import com.HuyHoang.DTO.response.ListeningHistoryResponse;
import com.HuyHoang.Entity.ListeningHistory;
import com.HuyHoang.Entity.User;
import com.HuyHoang.SpotifyService.SpotifyApiService;
import com.HuyHoang.exception.AppException;
import com.HuyHoang.exception.ErrorCode;
import com.HuyHoang.mapper.ListeningHistoryMapper;
import com.HuyHoang.repository.ListeningHistoryRepository;
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

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ListeningHistoryService {
    ListeningHistoryRepository listeningHistoryRepository;
    UserRepository userRepository;
    ListeningHistoryMapper listeningHistoryMapper;
    SpotifyApiService spotifyApiService;



    public ListeningHistoryResponse addSongListening(ListeningHistoryRequest request){
        var context = SecurityContextHolder.getContext();
        var username = context.getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        ListeningHistory listeningHistory = listeningHistoryMapper.toListeningHistory(request);
        listeningHistory.setPlayedAt(LocalDateTime.now());
        listeningHistory.setUser(user);
        //listeningHistory.setPlayedAt(LocalDateTime.now());
        listeningHistoryRepository.save(listeningHistory);
        var trackInfo = spotifyApiService.getTrackInfo(listeningHistory.getSpotifyTrackId());
        return ListeningHistoryResponse.builder()
                .id(listeningHistory.getId())
                .userId(user.getId())
                .title(trackInfo.getTrackName())
                .playedAt(listeningHistory.getPlayedAt())
                .spotifyTrackId(listeningHistory.getSpotifyTrackId())
                .albumImageUrl(trackInfo.getAlbumImageUrl())
                .artist(trackInfo.getArtistName())
                .previewUrl(trackInfo.getPreviewUrl())
                .build();
    }

    public List<ListeningHistoryResponse> getListeningHistory(){
        var context = SecurityContextHolder.getContext();
        var username = context.getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return listeningHistoryRepository.findByUser(user).stream().map(
                Songs -> {
                    var trackInfo = spotifyApiService.getTrackInfo(Songs.getSpotifyTrackId());
                    return ListeningHistoryResponse.builder()
                            .id(Songs.getId())
                            .userId(user.getId())
                            .title(trackInfo.getTrackName())
                            .spotifyTrackId(Songs.getSpotifyTrackId())
                            .albumImageUrl(trackInfo.getAlbumImageUrl())
                            .artist(trackInfo.getArtistName())
                            .previewUrl(trackInfo.getPreviewUrl())
                            .build();
                }

        ).toList();
    }
    public Page<ListeningHistoryResponse> getListeningHistoryPages(int page, int size, String sortBy, String direction){
        SecurityContext context = SecurityContextHolder.getContext();
        String username = context.getAuthentication().getName();

        User user = userRepository.findByUsername(username).orElseThrow(()-> new AppException(ErrorCode.USER_NOT_EXISTED));

        Sort sort = direction.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page,size,sort);
        return listeningHistoryRepository.findByUser(user, pageable).map(
                Songs -> {
                    var trackInfo = spotifyApiService.getTrackInfo(Songs.getSpotifyTrackId());

                    return ListeningHistoryResponse.builder()
                            .id(Songs.getId())
                            .userId(user.getId())
                            .title(trackInfo.getTrackName())
                            .spotifyTrackId(Songs.getSpotifyTrackId())
                            .albumImageUrl(trackInfo.getAlbumImageUrl())
                            .artist(trackInfo.getArtistName())
                            .previewUrl(trackInfo.getPreviewUrl())
                            .build();
                }
        );


    }
}
