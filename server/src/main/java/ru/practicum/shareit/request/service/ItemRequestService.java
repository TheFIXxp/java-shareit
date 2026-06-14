package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {

    ItemRequestDto create(ItemRequestCreateDto createRequest, long userId);

    List<ItemRequestDto> getOwnRequests(long userId);

    List<ItemRequestDto> getAllRequests(long userId);

    ItemRequestDto getById(long requestId, long userId);
}
