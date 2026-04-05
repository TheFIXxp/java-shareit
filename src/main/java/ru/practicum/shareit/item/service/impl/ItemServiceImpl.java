package ru.practicum.shareit.item.service.impl;

import jakarta.validation.ValidationException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ItemServiceImpl implements ItemService {

    UserService userService;
    ItemRepository itemRepository;

    @Override
    public ItemDto createItem(ItemDto itemDto, long userId) {
        validateItemForCreate(itemDto);
        userService.getUserById(userId);

        Item item = ItemMapper.fromDto(itemDto, userId);
        Item stored = this.itemRepository.create(item);
        return ItemMapper.toDto(stored);
    }

    @Override
    public ItemDto updateItem(long itemId, ItemDto itemDto, long userId) {
        Item existedItem = this.itemRepository.getById(itemId)
                .orElseThrow(() -> new NotFoundException("Item with id %s not found".formatted(itemId)));

        if (existedItem.getOwnerId() == null || existedItem.getOwnerId() != userId) {
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
        if (itemDto.getRequestId() != null) {
            existedItem.setRequestId(itemDto.getRequestId());
        }

        Item updated = this.itemRepository.update(existedItem);
        return ItemMapper.toDto(updated);
    }

    @Override
    public ItemDto getItemById(long itemId) {
        return this.itemRepository.getById(itemId).map(ItemMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Item with id %s not found".formatted(itemId)));
    }

    @Override
    public Collection<ItemDto> getItemsByOwner(long userId) {
        return this.itemRepository.getByOwnerId(userId).stream()
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<ItemDto> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        return this.itemRepository.searchAvailableByText(text).stream()
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
    }


    private void validateItemForCreate(ItemDto itemDto) {
        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new ValidationException("Item name must not be blank");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            throw new ValidationException("Item description must not be blank");
        }
        if (itemDto.getAvailable() == null) {
            throw new ValidationException("Item availability must be specified");
        }
    }
}
