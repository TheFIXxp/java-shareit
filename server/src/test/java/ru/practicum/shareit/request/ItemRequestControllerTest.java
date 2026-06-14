package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    private static final String SHARER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService itemRequestService;

    @Test
    @DisplayName("POST /requests: creates request")
    void addRequest_returnsCreatedRequest() throws Exception {
        ItemRequestDto response = new ItemRequestDto(1L, "Need a drill",
                LocalDateTime.of(2025, 1, 1, 12, 0), List.of());
        when(this.itemRequestService.create(any(), anyLong())).thenReturn(response);

        this.mockMvc.perform(post("/requests")
                        .header(SHARER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(new ItemRequestCreateDto("Need a drill"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Need a drill"));
    }

    @Test
    @DisplayName("GET /requests: returns own requests")
    void getOwnRequests_returnsList() throws Exception {
        ItemRequestDto response = new ItemRequestDto(1L, "Need a drill",
                LocalDateTime.of(2025, 1, 1, 12, 0), List.of());
        when(this.itemRequestService.getOwnRequests(anyLong())).thenReturn(List.of(response));

        this.mockMvc.perform(get("/requests").header(SHARER_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].description").value("Need a drill"));
    }

    @Test
    @DisplayName("GET /requests/all: returns other users requests")
    void getAllRequests_returnsList() throws Exception {
        ItemRequestDto response = new ItemRequestDto(2L, "Need a saw",
                LocalDateTime.of(2025, 1, 1, 12, 0), List.of());
        when(this.itemRequestService.getAllRequests(anyLong())).thenReturn(List.of(response));

        this.mockMvc.perform(get("/requests/all").header(SHARER_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2));
    }

    @Test
    @DisplayName("GET /requests/{id}: returns request")
    void getById_returnsRequest() throws Exception {
        ItemRequestDto response = new ItemRequestDto(1L, "Need a drill",
                LocalDateTime.of(2025, 1, 1, 12, 0), List.of());
        when(this.itemRequestService.getById(eq(1L), anyLong())).thenReturn(response);

        this.mockMvc.perform(get("/requests/1").header(SHARER_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("GET /requests/{id}: not found -> 404")
    void getById_notFound_returns404() throws Exception {
        when(this.itemRequestService.getById(anyLong(), anyLong()))
                .thenThrow(new NotFoundException("Request with id 999 not found"));

        this.mockMvc.perform(get("/requests/999").header(SHARER_HEADER, 1L))
                .andExpect(status().isNotFound());
    }
}
