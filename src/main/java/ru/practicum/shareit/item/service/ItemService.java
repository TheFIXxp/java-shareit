package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collection;

public interface ItemService {

    ItemDto createItem(ItemDto item, long userId);

    ItemDto updateItem(long itemId, ItemDto item, long userId);

    ItemDto getItemById(long itemId);

    Collection<ItemDto> getItemsByOwner(long userId);

    Collection<ItemDto> searchItems(String text);
}
