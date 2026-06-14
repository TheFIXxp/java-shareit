package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingCreateDto;

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    private static final String SHARER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingClient bookingClient;

    @Test
    @DisplayName("POST /bookings: valid -> forwards to client")
    void createBooking_valid_forwards() throws Exception {
        when(this.bookingClient.bookItem(anyLong(), any())).thenReturn(ResponseEntity.ok(Map.of("id", 1)));
        LocalDateTime now = LocalDateTime.now();
        BookingCreateDto request = new BookingCreateDto(1L, now.plusDays(1), now.plusDays(2));

        this.mockMvc.perform(post("/bookings")
                        .header(SHARER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(this.bookingClient).bookItem(anyLong(), any());
    }

    @Test
    @DisplayName("POST /bookings: start after end -> 400, client not called")
    void createBooking_startAfterEnd_returns400() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        BookingCreateDto request = new BookingCreateDto(1L, now.plusDays(2), now.plusDays(1));

        this.mockMvc.perform(post("/bookings")
                        .header(SHARER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(this.bookingClient, never()).bookItem(anyLong(), any());
    }

    @Test
    @DisplayName("POST /bookings: missing fields -> 400, client not called")
    void createBooking_missingFields_returns400() throws Exception {
        this.mockMvc.perform(post("/bookings")
                        .header(SHARER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(this.bookingClient, never()).bookItem(anyLong(), any());
    }

    @Test
    @DisplayName("GET /bookings: unknown state -> 400, client not called")
    void getBookings_unknownState_returns400() throws Exception {
        this.mockMvc.perform(get("/bookings")
                        .header(SHARER_HEADER, 1L)
                        .param("state", "NONSENSE"))
                .andExpect(status().isBadRequest());

        verify(this.bookingClient, never()).getBookings(anyLong(), any());
    }
}
