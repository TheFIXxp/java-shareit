package ru.practicum.shareit.user.repository.impl;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class InMemoryUserRepository implements UserRepository {

    private final Map<Long, User> users = new HashMap<>();
    private long nextId = 1;

    @Override
    public User create(User user) {
        user.setId(this.nextId++);
        this.users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        this.users.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> getById(long userId) {
        return Optional.ofNullable(this.users.get(userId));
    }

    @Override
    public Optional<User> getByEmail(String email) {
        return this.users.values().stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst();
    }

    @Override
    public Collection<User> getAll() {
        return this.users.values();
    }

    @Override
    public void deleteById(long userId) {
        this.users.remove(userId);
    }

}
