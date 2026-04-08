package ru.practicum.shareit.item.model;

import lombok.Data;

@Data
public class Item {
    Long id;
    String name;
    String description;
    Boolean available;
    Long ownerId;
    Long requestId;
}
