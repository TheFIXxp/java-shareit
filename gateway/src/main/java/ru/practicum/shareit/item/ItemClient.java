package ru.practicum.shareit.item;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.client.ShareItGateWayProperties;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

import java.util.Map;

@Service
public class ItemClient extends BaseClient {

    private static final String API_PREFIX = "/items";

    public ItemClient(ShareItGateWayProperties properties, RestTemplateBuilder builder) {
        super(builder, properties.getUrl() + API_PREFIX);
    }

    public ResponseEntity<Object> getItems(long userId) {
        return this.get("", userId);
    }

    public ResponseEntity<Object> addItem(long userId, ItemCreateDto itemDto) {
        return this.post("", userId, itemDto);
    }

    public ResponseEntity<Object> updateItem(long userId, long itemId, ItemUpdateDto itemDto) {
        return this.patch("/" + itemId, userId, itemDto);
    }

    public ResponseEntity<Object> getItem(long itemId) {
        return this.get("/" + itemId);
    }

    public ResponseEntity<Object> search(String text) {
        return this.get("/search?text={text}", null, Map.of("text", text));
    }

    public ResponseEntity<Object> addComment(long userId, long itemId, CommentCreateDto commentDto) {
        return this.post("/" + itemId + "/comment", userId, commentDto);
    }
}
