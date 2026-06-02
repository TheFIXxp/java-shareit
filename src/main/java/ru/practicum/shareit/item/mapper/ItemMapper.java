package ru.practicum.shareit.item.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@UtilityClass
public class ItemMapper {

    public ItemDto toDto(Item item, Booking lastBooking, Booking nextBooking, List<CommentDto> comments) {
        ItemDto dto = new ItemDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setAvailable(item.getAvailable());
        if (lastBooking != null) {
            BookingShortDto lastBookingDto = new BookingShortDto();
            lastBookingDto.setId(lastBooking.getId());
            lastBookingDto.setBookerId(lastBooking.getBooker() != null ? lastBooking.getBooker().getId() : null);
            dto.setLastBooking(lastBookingDto);
        }

        if (nextBooking != null) {
            BookingShortDto nextBookingDto = new BookingShortDto();
            nextBookingDto.setId(nextBooking.getId());
            nextBookingDto.setBookerId(nextBooking.getBooker() != null ? nextBooking.getBooker().getId() : null);
            dto.setNextBooking(nextBookingDto);
        }

        dto.setComments(comments);

        return dto;
    }

    public ItemDto toDto(Item item, List<CommentDto> comments) {
        ItemDto dto = new ItemDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setAvailable(item.getAvailable());
        dto.setLastBooking(null);
        dto.setNextBooking(null);

        dto.setComments(comments);

        return dto;
    }

    public ItemDto toDto(Item item) {
        ItemDto dto = new ItemDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setAvailable(item.getAvailable());
        dto.setLastBooking(null);
        dto.setNextBooking(null);
        dto.setComments(List.of());
        return dto;
    }

    public Item fromDto(ItemCreateDto createRequest) {
        Item item = new Item();
        item.setName(createRequest.getName());
        item.setDescription(createRequest.getDescription());
        item.setAvailable(createRequest.getAvailable());
        return item;
    }
}


