package ru.practicum.shareit.item.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.mapper.CommentMapper;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.comment.repository.CommentRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestsRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ItemServiceImpl implements ItemService {

    UserRepository userRepository;
    ItemRepository itemRepository;
    BookingRepository bookingRepository;
    CommentRepository commentRepository;
    ItemRequestsRepository itemRequestRepository;

    @Override
    public ItemDto createItem(ItemCreateDto createRequest, long userId) {
        log.info("Creating item {} for user {}", createRequest, userId);
        User owner = this.userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id %s not found".formatted(userId)));

        Item item = ItemMapper.fromDto(createRequest);
        item.setOwner(owner);

        if (createRequest.getRequestId() != null) {
            ItemRequest request = this.itemRequestRepository.findById(createRequest.getRequestId())
                    .orElseThrow(() -> new NotFoundException(
                            "Request with id %s not found".formatted(createRequest.getRequestId())));
            item.setRequest(request);
        }

        Item saved = this.itemRepository.save(item);
        return ItemMapper.toDto(saved);
    }

    @Override
    public ItemDto updateItem(long itemId, ItemUpdateDto itemDto, long userId) {
        log.info("Updating item {} for user {}", itemDto, userId);
        Item existedItem = this.itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item with id %s not found".formatted(itemId)));

        User owner = existedItem.getOwner();
        if (owner == null || owner.getId() != userId) {
            throw new NotFoundException("Item with id %s not found".formatted(itemId));
        }

        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            existedItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            existedItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existedItem.setAvailable(itemDto.getAvailable());
        }

        Item updated = this.itemRepository.save(existedItem);
        return ItemMapper.toDto(updated);
    }

    @Override
    public ItemDto getItemById(long itemId) {
        log.info("Getting item by id {}", itemId);
        Item item = this.itemRepository.findByIdWithComments(itemId)
                .orElseThrow(() -> new NotFoundException("Item with id %s not found".formatted(itemId)));

        return this.buildItemDtoWithComments(item);
    }

    @Override
    public Collection<ItemDto> getItemsByOwner(long userId) {
        log.info("Getting items by owner {}", userId);

        List<Item> items = this.itemRepository.findByOwner_Id(userId).stream()
                .toList();

        if (items.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .collect(Collectors.toList());

        LocalDateTime now = LocalDateTime.now();

        List<Booking> allBookings = this.bookingRepository.findApprovedByItemIds(itemIds);
        Map<Long, List<Booking>> bookingsByItemId = allBookings.stream()
                .collect(Collectors.groupingBy(b -> b.getItem().getId(), Collectors.toList()));

        List<Comment> allComments = this.commentRepository.findByItemIds(itemIds);
        Map<Long, List<CommentDto>> commentsByItemId = allComments.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getItem().getId(),
                        Collectors.mapping(CommentMapper::toDto, Collectors.toList())
                ));

        return items.stream()
                .map(item -> this.buildItemDtoWithBookings(
                        item,
                        bookingsByItemId.getOrDefault(item.getId(), Collections.emptyList()),
                        commentsByItemId.getOrDefault(item.getId(), Collections.emptyList()),
                        now
                ))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<ItemDto> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        log.info("Searching items by text {}", text);

        return this.itemRepository.searchAvailableByText(text)
                .stream()
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto createComment(long itemId, String text, long userId) {
        log.info("Creating comment for item {} by user {}", itemId, userId);

        User author = this.userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id %s not found".formatted(userId)));

        Item item = this.itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item with id %s not found".formatted(itemId)));

        LocalDateTime now = LocalDateTime.now();
        Collection<Booking> completedBookings =
                this.bookingRepository.findByBooker_IdAndEndIsBeforeOrderByEndDesc(userId, now);

        boolean hasCompletedBooking = completedBookings.stream()
                .anyMatch(b -> b.getItem().getId().equals(itemId) &&
                        b.getStatus() == Booking.BookingStatus.APPROVED);

        if (!hasCompletedBooking) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User has no completed booking for this item");
        }

        Comment comment = CommentMapper.from(text, item, author);


        Comment saved = this.commentRepository.save(comment);
        return CommentMapper.toDto(saved);
    }

    private ItemDto buildItemDtoWithComments(Item item) {
        return ItemMapper.toDto(item, collectComments(item));
    }

    private ItemDto buildItemDtoWithBookings(Item item, List<Booking> bookings,
                                             List<CommentDto> comments, LocalDateTime now) {
        Booking lastBooking = bookings.stream()
                .filter(b -> b.getEnd().isBefore(now))
                .max(Comparator.comparing(Booking::getEnd))
                .orElse(null);

        Booking nextBooking = bookings.stream()
                .filter(b -> b.getStart().isAfter(now))
                .min(Comparator.comparing(Booking::getStart))
                .orElse(null);

        return ItemMapper.toDto(item, lastBooking, nextBooking, comments);
    }

    private static List<CommentDto> collectComments(Item item) {
        return (item.getComments() != null ? item.getComments() : List.<Comment>of())
                .stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.toList());
    }
}
