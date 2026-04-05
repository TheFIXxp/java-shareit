package ru.practicum.shareit.item.repository.impl;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class InMemoryItemRepository implements ItemRepository {

    private final Map<Long, Item> items = new LinkedHashMap<>();
    private long nextId = 1;

    @Override
    public Item create(Item item) {
        item.setId(this.nextId++);
        this.items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item update(Item item) {
        this.items.put(item.getId(), item);
        return item;
    }

    @Override
    public Optional<Item> getById(long itemId) {
        return Optional.ofNullable(this.items.get(itemId));
    }

    @Override
    public Collection<Item> getByOwnerId(long ownerId) {
        return this.items.values().stream().filter(item -> item.getOwnerId() != null && item.getOwnerId().equals(ownerId)).collect(Collectors.toList());
    }

    @Override
    public Collection<Item> getAll() {
        return this.items.values();
    }

    @Override
    public Collection<Item> searchAvailableByText(String text) {
        String searchText = text.toLowerCase();
        return this.items.values().stream().filter(Item::getAvailable).filter(item -> containsIgnoreCase(item.getName(), searchText) || containsIgnoreCase(item.getDescription(), searchText)).collect(Collectors.toList());
    }

    private boolean containsIgnoreCase(String value, String searchText) {
        return value != null && value.toLowerCase().contains(searchText);
    }
}

