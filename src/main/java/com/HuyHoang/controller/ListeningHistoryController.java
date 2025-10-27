package com.HuyHoang.controller;

import com.HuyHoang.DTO.request.ListeningHistoryRequest;
import com.HuyHoang.DTO.response.ApiResponse;
import com.HuyHoang.DTO.response.ListeningHistoryResponse;
import com.HuyHoang.service.ListeningHistoryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/listening-history")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ListeningHistoryController {
    ListeningHistoryService listeningHistoryService;

    @PostMapping()
    ApiResponse<ListeningHistoryResponse> addSongToHistory(@RequestBody ListeningHistoryRequest request){
        return ApiResponse.<ListeningHistoryResponse>builder()
                .result(listeningHistoryService.addSongListening(request))
                .build();
    }

    @GetMapping()
    ApiResponse<List<ListeningHistoryResponse>> getHistory(){
        return ApiResponse.<List<ListeningHistoryResponse>>builder()
                .result(listeningHistoryService.getListeningHistory())
                .build();
    }

    @GetMapping("/pages")
    ApiResponse<Page<ListeningHistoryResponse>> getListeningHistoryPages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "playedAt") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ){

        return  ApiResponse.<Page<ListeningHistoryResponse>>builder()
                .result(
                        listeningHistoryService.getListeningHistoryPages(page,size,sortBy,direction)
                )
                .build();
    }
}
