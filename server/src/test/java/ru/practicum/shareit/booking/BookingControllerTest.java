package ru.practicum.shareit.booking;

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
import ru.practicum.shareit.booking.dto.BookerDto;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.ItemForBookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    private static final String SHARER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @Test
    @DisplayName("POST /bookings: creates booking")
    void createBooking_returnsCreated() throws Exception {
        when(this.bookingService.createBooking(any(), anyLong()))
                .thenReturn(this.bookingDto(Booking.BookingStatus.WAITING));

        LocalDateTime now = LocalDateTime.now();
        BookingCreateDto request = new BookingCreateDto(1L, now.plusDays(1), now.plusDays(2));

        this.mockMvc.perform(post("/bookings")
                        .header(SHARER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    @DisplayName("POST /bookings: missing fields -> 400")
    void createBooking_missingFields_returns400() throws Exception {
        this.mockMvc.perform(post("/bookings")
                        .header(SHARER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /bookings/{id}: approves booking")
    void approveBooking_returnsApproved() throws Exception {
        when(this.bookingService.approveBooking(eq(1L), eq(true), anyLong()))
                .thenReturn(this.bookingDto(Booking.BookingStatus.APPROVED));

        this.mockMvc.perform(patch("/bookings/1")
                        .header(SHARER_HEADER, 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @DisplayName("PATCH /bookings/{id}: by non-owner -> 403")
    void approveBooking_byNonOwner_returns403() throws Exception {
        when(this.bookingService.approveBooking(anyLong(), eq(true), anyLong()))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Only owner can approve booking"));

        this.mockMvc.perform(patch("/bookings/1")
                        .header(SHARER_HEADER, 1L)
                        .param("approved", "true"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /bookings/{id}: returns booking")
    void getBooking_returnsBooking() throws Exception {
        when(this.bookingService.getBookingById(eq(1L), anyLong()))
                .thenReturn(this.bookingDto(Booking.BookingStatus.WAITING));

        this.mockMvc.perform(get("/bookings/1").header(SHARER_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("GET /bookings: returns booker bookings")
    void getBookings_returnsList() throws Exception {
        when(this.bookingService.getBookingsByBooker(anyLong(), anyString()))
                .thenReturn(List.of(this.bookingDto(Booking.BookingStatus.WAITING)));

        this.mockMvc.perform(get("/bookings").header(SHARER_HEADER, 1L).param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @DisplayName("GET /bookings/owner: returns owner bookings")
    void getOwnerBookings_returnsList() throws Exception {
        when(this.bookingService.getBookingsByOwner(anyLong(), anyString()))
                .thenReturn(List.of(this.bookingDto(Booking.BookingStatus.WAITING)));

        this.mockMvc.perform(get("/bookings/owner").header(SHARER_HEADER, 1L).param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    private BookingDto bookingDto(Booking.BookingStatus status) {
        BookingDto dto = new BookingDto();
        dto.setId(1L);
        dto.setStart(LocalDateTime.of(2025, 1, 1, 12, 0));
        dto.setEnd(LocalDateTime.of(2025, 1, 2, 12, 0));
        dto.setItem(new ItemForBookingDto(1L, "Drill"));
        dto.setBooker(new BookerDto(1L, "Ivan"));
        dto.setStatus(status);
        return dto;
    }
}
