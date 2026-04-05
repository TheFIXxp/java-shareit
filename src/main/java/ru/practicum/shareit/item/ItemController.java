package ru.practicum.shareit.item;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.Collection;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/items")
public class ItemController {

    ItemService itemService;

    @GetMapping
    public Collection<ItemDto> getAllItems(@RequestHeader("X-Sharer-User-Id") long userId) {
        return this.itemService.getItemsByOwner(userId);
    }

    @PostMapping
    public ItemDto addItem(@RequestHeader("X-Sharer-User-Id") long userId, @RequestBody ItemDto itemDto) {
        return this.itemService.createItem(itemDto, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@PathVariable long itemId, @RequestHeader("X-Sharer-User-Id") long userId, @RequestBody ItemDto itemDto) {
        return this.itemService.updateItem(itemId, itemDto, userId);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItem(@PathVariable long itemId) {
        return this.itemService.getItemById(itemId);
    }

    @GetMapping("/search")
    public Collection<ItemDto> searchItems(@RequestParam String text) {
        return this.itemService.searchItems(text);
    }
}
