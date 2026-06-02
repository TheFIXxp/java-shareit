package ru.practicum.shareit.testutil;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.user.dto.UserDto;

@UtilityClass
public class TestDataFactory {

    public static UserDto userDto(String email, String name) {
        return new UserDto(0, email, name);
    }

    public static ItemCreateDto itemCreateRequest(String name, String description, boolean available) {
        return new ItemCreateDto(null, name, description, available);
    }

    public static ItemUpdateDto itemUpdateRequest(String name, String description, Boolean available) {
        return new ItemUpdateDto(null, name, description, available);
    }
}