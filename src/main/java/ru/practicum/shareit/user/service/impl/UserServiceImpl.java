package ru.practicum.shareit.user.service.impl;

import jakarta.validation.ValidationException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.UserAlreadyExistsException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {

    UserRepository userRepository;

    @Override
    public UserDto createUser(UserDto userDto) {
        log.info("Creating user {}", userDto);
        validateUserForCreate(userDto);
        validateEmailUniqueness(userDto.getEmail());

        User stored = this.userRepository.create(UserMapper.fromDto(userDto));
        return UserMapper.toDto(stored);
    }

    @Override
    public UserDto updateUser(long userId, UserDto dto) {
        log.info("Updating user {} with {}", userId, dto);
        User existingUser = userRepository.getById(userId)
                .orElseThrow(() -> new NotFoundException("User with id %s not found".formatted(userId)));

        if (dto.getName() != null) {
            if (dto.getName().isBlank()) {
                throw new ValidationException("Name must not be blank");
            }
            existingUser.setName(dto.getName());
        }

        if (dto.getEmail() != null) {
            if (dto.getEmail().isBlank()) {
                throw new ValidationException("Email must not be blank");
            }
            validateEmailUniqueness(dto.getEmail());
            existingUser.setEmail(dto.getEmail());
        }

        return UserMapper.toDto(userRepository.update(existingUser));
    }

    @Override
    public Collection<UserDto> getUsers() {
        log.info("Getting all users");
        return userRepository.getAll()
                .stream().map(UserMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(long userId) {
        log.info("Getting user by id {}", userId);
        return this.userRepository.getById(userId)
                .map(UserMapper::toDto)
                .orElseThrow(() -> new NotFoundException("User with id %s not found".formatted(userId)));
    }

    @Override
    public void deleteUserById(long userId) {
        log.info("Deleting user by id {}", userId);
        this.userRepository.deleteById(userId);
    }

    private void validateUserForCreate(UserDto userDto) {
        if (userDto.getEmail() == null || userDto.getEmail().isBlank()) {
            throw new ValidationException("Email must not be blank");
        }
        if (userDto.getName() == null || userDto.getName().isBlank()) {
            throw new ValidationException("Name must not be blank");
        }
    }

    private void validateEmailUniqueness(String email) {
        userRepository.getByEmail(email).ifPresent(user -> {
            throw new UserAlreadyExistsException("User with email %s already exists".formatted(email));
        });
    }

}

