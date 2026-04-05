package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collection;


@RestController
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequestMapping("/users")
public class UserController {

    UserService userService;

    @PostMapping
    public UserDto createUser(@Valid @RequestBody UserDto userDto) {
        return this.userService.createUser(userDto);
    }

    @GetMapping
    public Collection<UserDto> getUsers() {
        return this.userService.getUsers();
    }

    @PatchMapping("/{userId}")
    public UserDto updateUser(@PathVariable long userId, @Valid @RequestBody UserDto userDto) {
        return this.userService.updateUser(userId, userDto);
    }

    @GetMapping("/{userId}")
    public UserDto getUserById(@PathVariable long userId) {
        return this.userService.getUserById(userId);
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable long userId) {
        this.userService.deleteUserById(userId);
    }

}
