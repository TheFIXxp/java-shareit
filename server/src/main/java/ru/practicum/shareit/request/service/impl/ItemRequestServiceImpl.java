package ru.practicum.shareit.request.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemShortDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestsRepository;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    ItemRequestsRepository requestRepository;
    UserRepository userRepository;
    ItemRepository itemRepository;

    @Override
    public ItemRequestDto create(ItemRequestCreateDto createRequest, long userId) {
        log.info("Creating item request {} for user {}", createRequest, userId);
        User requestor = this.getUserOrElseThrow(userId);

        ItemRequest request = ItemRequestMapper.toEntity(createRequest, requestor, LocalDateTime.now());
        ItemRequest saved = this.requestRepository.save(request);

        return ItemRequestMapper.toDto(saved, List.of());
    }

    @Override
    public List<ItemRequestDto> getOwnRequests(long userId) {
        log.info("Getting own requests for user {}", userId);
        this.getUserOrElseThrow(userId);

        List<ItemRequest> requests = this.requestRepository.findByRequestor_IdOrderByCreatedDesc(userId);
        return this.toDtosWithItems(requests);
    }

    @Override
    public List<ItemRequestDto> getAllRequests(long userId) {
        log.info("Getting all requests created by other users for user {}", userId);
        this.getUserOrElseThrow(userId);

        List<ItemRequest> requests = this.requestRepository.findByRequestor_IdNotOrderByCreatedDesc(userId);
        return this.toDtosWithItems(requests);
    }

    @Override
    public ItemRequestDto getById(long requestId, long userId) {
        log.info("Getting request {} for user {}", requestId, userId);
        this.getUserOrElseThrow(userId);

        ItemRequest request = this.requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request with id %s not found".formatted(requestId)));

        List<ItemShortDto> items = this.itemRepository.findByRequest_Id(requestId).stream()
                .map(ItemRequestMapper::toItemShortDto)
                .collect(Collectors.toList());

        return ItemRequestMapper.toDto(request, items);
    }

    private List<ItemRequestDto> toDtosWithItems(List<ItemRequest> requests) {
        if (requests.isEmpty()) {
            return List.of();
        }

        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());

        Map<Long, List<ItemShortDto>> itemsByRequestId = this.itemRepository.findByRequest_IdIn(requestIds).stream()
                .collect(Collectors.groupingBy(
                        item -> item.getRequest().getId(),
                        Collectors.mapping(ItemRequestMapper::toItemShortDto, Collectors.toList())
                ));

        return requests.stream()
                .map(request -> ItemRequestMapper.toDto(
                        request,
                        itemsByRequestId.getOrDefault(request.getId(), List.of())))
                .collect(Collectors.toList());
    }

    private User getUserOrElseThrow(long userId) {
        return this.userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id %s not found".formatted(userId)));
    }
}
