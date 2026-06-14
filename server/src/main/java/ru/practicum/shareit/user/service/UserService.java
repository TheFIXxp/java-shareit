package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;

public interface UserService {
    UserDto createUser(UserDto user);

    UserDto updateUser(long userId, UserDto user);

    Collection<UserDto> getUsers();

    UserDto getUserById(long userId);

    void deleteUserById(long userId);
}
