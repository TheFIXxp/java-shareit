package ru.practicum.shareit.user.service.impl;

import jakarta.validation.ValidationException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
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
        this.validateUserForCreate(userDto);

        User user = UserMapper.fromDto(userDto);
        try {
            return UserMapper.toDto(this.userRepository.save(user));
        } catch (DataIntegrityViolationException e) {
            throw new UserAlreadyExistsException("User with email already exists");
        }
    }

    @Override
    public UserDto updateUser(long userId, UserDto dto) {
        log.info("Updating user {} with {}", userId, dto);
        User existingUser = this.userRepository.findById(userId)
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
            existingUser.setEmail(dto.getEmail());
        }

        try {
            return UserMapper.toDto(this.userRepository.save(existingUser));
        } catch (DataIntegrityViolationException e) {
            throw new UserAlreadyExistsException("User with email already exists");
        }
    }

    @Override
    public Collection<UserDto> getUsers() {
        log.info("Getting all users");
        return this.userRepository.findAll()
                .stream().map(UserMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(long userId) {
        log.info("Getting user by id {}", userId);
        return this.userRepository.findById(userId)
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
}

