package ru.practicum.shareit.user;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.client.ShareItGateWayProperties;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

@Service
public class UserClient extends BaseClient {

    private static final String API_PREFIX = "/users";

    public UserClient(ShareItGateWayProperties properties, RestTemplateBuilder builder) {
        super(builder, properties.getUrl() + API_PREFIX);
    }

    public ResponseEntity<Object> createUser(UserCreateDto userDto) {
        return this.post("", userDto);
    }

    public ResponseEntity<Object> getUsers() {
        return this.get("");
    }

    public ResponseEntity<Object> getUser(long userId) {
        return this.get("/" + userId);
    }

    public ResponseEntity<Object> updateUser(long userId, UserUpdateDto userDto) {
        return this.patch("/" + userId, userDto);
    }

    public ResponseEntity<Object> deleteUser(long userId) {
        return this.delete("/" + userId);
    }
}
