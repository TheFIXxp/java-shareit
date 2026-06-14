package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingDtoJsonTest {

    @Autowired
    private JacksonTester<BookingDto> json;

    @Test
    @DisplayName("serialize: writes ISO dates and nested objects")
    void serialize_writesIsoDatesAndNestedObjects() throws Exception {
        BookingDto dto = new BookingDto();
        dto.setId(1L);
        dto.setStart(LocalDateTime.of(2025, 1, 1, 12, 30, 45));
        dto.setEnd(LocalDateTime.of(2025, 1, 2, 12, 30, 45));
        dto.setItem(new ItemForBookingDto(3L, "Drill"));
        dto.setBooker(new BookerDto(2L, "Ivan"));
        dto.setStatus(Booking.BookingStatus.WAITING);

        JsonContent<BookingDto> result = this.json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2025-01-01T12:30:45");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2025-01-02T12:30:45");
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("WAITING");
        assertThat(result).extractingJsonPathNumberValue("$.item.id").isEqualTo(3);
        assertThat(result).extractingJsonPathStringValue("$.booker.name").isEqualTo("Ivan");
    }

    @Test
    @DisplayName("deserialize: parses ISO dates and status")
    void deserialize_parsesIsoDatesAndStatus() throws Exception {
        String content = "{\"id\":1,\"start\":\"2025-01-01T12:30:45\","
                + "\"end\":\"2025-01-02T12:30:45\",\"status\":\"APPROVED\"}";

        BookingDto dto = this.json.parseObject(content);

        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2025, 1, 1, 12, 30, 45));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2025, 1, 2, 12, 30, 45));
        assertThat(dto.getStatus()).isEqualTo(Booking.BookingStatus.APPROVED);
    }
}
