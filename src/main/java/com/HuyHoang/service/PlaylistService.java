package com.HuyHoang.service;

import com.HuyHoang.DTO.request.PlaylistCreationRequest;
import com.HuyHoang.DTO.request.PlaylistUpdateRequest;
import com.HuyHoang.DTO.response.PlaylistResponse;
import com.HuyHoang.Entity.Playlist;
import com.HuyHoang.Entity.User;
import com.HuyHoang.exception.AppException;
import com.HuyHoang.exception.ErrorCode;
import com.HuyHoang.mapper.PlaylistMapper;
import com.HuyHoang.repository.PlaylistRepository;
import com.HuyHoang.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PlaylistService {
    PlaylistRepository playlistRepository;
    UserRepository userRepository;
    PlaylistMapper playlistMapper;

    @PreAuthorize("hasRole('ADMIN')")
    public Page<PlaylistResponse> getAllPlaylistsPaged(int page, int size, String sortBy, String direction){
        Sort sort = direction.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page,size,sort);
        return playlistRepository.findAll(pageable).map(playlistMapper::toPlaylistResponse);

    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<PlaylistResponse> getUserPlaylistsPaged(String userId, int page, int size, String sortBy, String direction){
        User user = userRepository.findById(userId).orElseThrow(()-> new AppException(ErrorCode.USER_NOT_EXISTED));

        Sort sort = direction.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return playlistRepository.findAllByUser(user,pageable).map(playlistMapper::toPlaylistResponse);
    }

    @PreAuthorize("hasRole('USER')")
    public PlaylistResponse createPlaylist( PlaylistCreationRequest request){

        var context = SecurityContextHolder.getContext();
        var name = context.getAuthentication().getName();

        var user = userRepository.findByUsername(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if(playlistRepository.existsByName(request.getName())){
            throw new AppException(ErrorCode.PLAYLIST_EXISTED);
        }

        Playlist playlist = playlistMapper.toPlaylist(request);
        playlist.setUser(user);




        return playlistMapper.toPlaylistResponse(playlistRepository.save(playlist));

    }
    @PreAuthorize("hasRole('USER')")
    public PlaylistResponse updatePlaylist(String playlistId, PlaylistUpdateRequest request){
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new AppException(ErrorCode.PLAYLIST_NOT_EXISTED));

        playlistMapper.updatePlaylist(playlist,request);

        return playlistMapper.toPlaylistResponse(playlistRepository.save(playlist));

    }

    @PreAuthorize("hasRole('USER')")
    public void deletePlaylist(String playlistId){
        playlistRepository.deleteById(playlistId);
    }

    @PreAuthorize("hasRole('USER')")
    public List<PlaylistResponse> getAllPlaylists(){
        var context = SecurityContextHolder.getContext();
        var name = context.getAuthentication().getName();
        User user = userRepository.findByUsername(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return playlistRepository.findAllByUser(user).stream().map(playlistMapper::toPlaylistResponse).toList();
    }

    @PreAuthorize("hasRole('USER')")
    public PlaylistResponse getPlaylist(String playlistId){
        return playlistMapper.toPlaylistResponse(playlistRepository.findById(playlistId)
                .orElseThrow(() -> new AppException(ErrorCode.PLAYLIST_NOT_EXISTED)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<PlaylistResponse> getAllUserPlaylists(){
        return playlistRepository.findAll().stream().map(playlistMapper::toPlaylistResponse).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<PlaylistResponse> getPlaylistOfUser(String userId){
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return playlistRepository.findAllByUser(user).stream().map(
                playlistMapper::toPlaylistResponse
        ).toList();
    }

    public List<PlaylistResponse> getMyPlaylist(){
        var context = SecurityContextHolder.getContext();

        var name = context.getAuthentication().getName();

        User user = userRepository.findByUsername(name).orElseThrow(()-> new AppException(ErrorCode.USER_NOT_EXISTED));

        return playlistRepository.findAllByUser(user).stream().map(playlistMapper::toPlaylistResponse).toList();
    }
}
