package com.HuyHoang.mapper;

import com.HuyHoang.DTO.request.ListeningHistoryRequest;
import com.HuyHoang.DTO.response.ListeningHistoryResponse;
import com.HuyHoang.Entity.ListeningHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = {java.time.LocalDateTime.class})
public interface ListeningHistoryMapper {

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "playedAt",expression = "java(LocalDateTime.now())")
    ListeningHistory toListeningHistory(ListeningHistoryRequest request);
    ListeningHistoryResponse toListeningHistoryResponse(ListeningHistory listeningHistory);
}
