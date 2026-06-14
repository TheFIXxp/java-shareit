package ru.practicum.shareit.request;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.client.ShareItGateWayProperties;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;

@Service
public class RequestClient extends BaseClient {

    private static final String API_PREFIX = "/requests";

    public RequestClient(ShareItGateWayProperties properties, RestTemplateBuilder builder) {
        super(builder, properties.getUrl() + API_PREFIX);
    }

    public ResponseEntity<Object> addRequest(long userId, ItemRequestCreateDto createDto) {
        return this.post("", userId, createDto);
    }

    public ResponseEntity<Object> getOwnRequests(long userId) {
        return this.get("", userId);
    }

    public ResponseEntity<Object> getAllRequests(long userId) {
        return this.get("/all", userId);
    }

    public ResponseEntity<Object> getById(long userId, long requestId) {
        return this.get("/" + requestId, userId);
    }
}
