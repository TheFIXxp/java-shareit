package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    private static final String SHARER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    @Test
    @DisplayName("POST /items: creates item")
    void addItem_returnsCreatedItem() throws Exception {
        ItemDto response = this.itemDto(1L, "Drill", "Power drill");
        when(this.itemService.createItem(any(), anyLong())).thenReturn(response);

        this.mockMvc.perform(post("/items")
                        .header(SHARER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(
                                new ItemCreateDto(null, "Drill", "Power drill", true, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Drill"));
    }

    @Test
    @DisplayName("POST /items: blank name -> 400")
    void addItem_blankName_returns400() throws Exception {
        this.mockMvc.perform(post("/items")
                        .header(SHARER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(
                                new ItemCreateDto(null, " ", "Power drill", true, null))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /items: returns owner items")
    void getItems_returnsList() throws Exception {
        when(this.itemService.getItemsByOwner(anyLong()))
                .thenReturn(List.of(this.itemDto(1L, "Drill", "Power drill")));

        this.mockMvc.perform(get("/items").header(SHARER_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @DisplayName("GET /items/{id}: returns item")
    void getItem_returnsItem() throws Exception {
        when(this.itemService.getItemById(anyLong())).thenReturn(this.itemDto(1L, "Drill", "Power drill"));

        this.mockMvc.perform(get("/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("PATCH /items/{id}: returns updated item")
    void updateItem_returnsUpdated() throws Exception {
        when(this.itemService.updateItem(anyLong(), any(), anyLong()))
                .thenReturn(this.itemDto(1L, "Road bike", "Power drill"));

        this.mockMvc.perform(patch("/items/1")
                        .header(SHARER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Road bike\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Road bike"));
    }

    @Test
    @DisplayName("GET /items/search: returns matches")
    void searchItems_returnsList() throws Exception {
        when(this.itemService.searchItems(anyString()))
                .thenReturn(List.of(this.itemDto(1L, "Drill", "Power drill")));

        this.mockMvc.perform(get("/items/search").param("text", "drill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Drill"));
    }

    @Test
    @DisplayName("POST /items/{id}/comment: returns comment")
    void addComment_returnsComment() throws Exception {
        CommentDto response = new CommentDto(1L, "Great drill", "Ivan", Instant.parse("2025-01-01T12:00:00Z"));
        when(this.itemService.createComment(anyLong(), anyString(), anyLong())).thenReturn(response);

        this.mockMvc.perform(post("/items/1/comment")
                        .header(SHARER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Great drill\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Great drill"))
                .andExpect(jsonPath("$.authorName").value("Ivan"));
    }

    @Test
    @DisplayName("POST /items/{id}/comment: no completed booking -> 400")
    void addComment_noCompletedBooking_returns400() throws Exception {
        when(this.itemService.createComment(anyLong(), anyString(), anyLong()))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "No completed booking"));

        this.mockMvc.perform(post("/items/1/comment")
                        .header(SHARER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Great drill\"}"))
                .andExpect(status().isBadRequest());
    }

    private ItemDto itemDto(Long id, String name, String description) {
        ItemDto dto = new ItemDto();
        dto.setId(id);
        dto.setName(name);
        dto.setDescription(description);
        dto.setAvailable(true);
        dto.setComments(List.of());
        return dto;
    }
}
