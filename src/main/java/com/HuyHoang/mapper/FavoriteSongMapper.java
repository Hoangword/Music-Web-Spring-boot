package com.HuyHoang.mapper;

import com.HuyHoang.DTO.request.FavoriteSongRequest;
import com.HuyHoang.DTO.response.FavoriteSongResponse;
import com.HuyHoang.Entity.FavoriteSongs;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FavoriteSongMapper {
    @Mapping(target = "user", ignore = true)
    FavoriteSongs toFavoriteSongs(FavoriteSongRequest request);
    FavoriteSongResponse toFavoriteSongResponse(FavoriteSongs favoriteSongs);
}
