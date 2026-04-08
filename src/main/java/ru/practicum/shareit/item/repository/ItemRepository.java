package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.Optional;

public interface ItemRepository {

    Item create(Item item);

    Item update(Item item);

    Optional<Item> getById(long itemId);

    Collection<Item> getByOwnerId(long ownerId);

    Collection<Item> getAll();

    Collection<Item> searchAvailableByText(String text);
}


