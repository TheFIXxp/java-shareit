package ru.practicum.shareit.request.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequest {
    long id;
    String description;
    long requestorId;
    long created;
}