package ru.practicum.shareit.item.service;

import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

import java.util.Collection;

public interface ItemService {

    ItemDto createItem(ItemCreateDto item, long userId);

    ItemDto updateItem(long itemId, ItemUpdateDto item, long userId);

    ItemDto getItemById(long itemId);

    Collection<ItemDto> getItemsByOwner(long userId);

    Collection<ItemDto> searchItems(String text);

    CommentDto createComment(long itemId, String text, long userId);
}
