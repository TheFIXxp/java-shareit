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
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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

    @Override
    public ItemDto createItem(ItemCreateDto createRequest, long userId) {
        log.info("Creating item {} for user {}", createRequest, userId);
        User owner = this.userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id %s not found".formatted(userId)));

        Item item = ItemMapper.fromDto(createRequest);
        item.setOwner(owner);

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
        return this.itemRepository.findByOwner_Id(userId)
                .stream()
                .map(this::buildItemDtoWithCommentsAndBookings)
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

        Comment comment = new Comment();
        comment.setText(text);
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(Instant.now());

        Comment saved = this.commentRepository.save(comment);
        return CommentMapper.toDto(saved);
    }

    private ItemDto buildItemDtoWithComments(Item item) {
        return ItemMapper.toDto(item, collectComments(item));
    }

    private ItemDto buildItemDtoWithCommentsAndBookings(Item item) {
        LocalDateTime now = LocalDateTime.now();

        Booking lastBooking = this.bookingRepository.findLastBookingByItem(item.getId(), now)
                .stream()
                .findFirst()
                .orElse(null);

        Booking nextBooking = this.bookingRepository.findNextBookingByItem(item.getId(), now)
                .stream()
                .findFirst()
                .orElse(null);

        List<CommentDto> comments = collectComments(item);

        return ItemMapper.toDto(item, lastBooking, nextBooking, comments);
    }

    private static List<CommentDto> collectComments(Item item) {
        return (item.getComments() != null ? item.getComments() : List.<Comment>of())
                .stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.toList());
    }


}
