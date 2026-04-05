package ru.practicum.shareit.testutil;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

@UtilityClass
public class TestDataFactory {

    public static UserDto userDto(String email, String name) {
        return new UserDto(0, email, name);
    }

    public static ItemDto itemDto(String name, String description, boolean available) {
        return new ItemDto(null, name, description, available, null, null);
    }

    public static ItemDto itemPatch(String name, String description, Boolean available, Long requestId) {
        return new ItemDto(null, name, description, available, null, requestId);
    }
}


