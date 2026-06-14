package ru.practicum.shareit.testutil;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

@UtilityClass
public class TestDataFactory {

    public static UserDto userDto(String email, String name) {
        return new UserDto(0, email, name);
    }

    public static ItemRequestCreateDto itemRequestCreate(String description) {
        return new ItemRequestCreateDto(description);
    }

    public static BookingCreateDto bookingCreateRequest(Long itemId, LocalDateTime start, LocalDateTime end) {
        return new BookingCreateDto(itemId, start, end);
    }

    public static ItemCreateDto itemCreateRequest(String name, String description, boolean available) {
        return new ItemCreateDto(null, name, description, available, null);
    }

    public static ItemCreateDto itemCreateRequest(String name, String description, boolean available, Long requestId) {
        return new ItemCreateDto(null, name, description, available, requestId);
    }

    public static ItemUpdateDto itemUpdateRequest(String name, String description, Boolean available) {
        return new ItemUpdateDto(null, name, description, available);
    }
}