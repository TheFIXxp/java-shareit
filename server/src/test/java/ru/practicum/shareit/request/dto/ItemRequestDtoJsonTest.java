package ru.practicum.shareit.request.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @Test
    @DisplayName("serialize: writes created date and item answers")
    void serialize_writesCreatedDateAndItems() throws Exception {
        ItemShortDto item = new ItemShortDto(5L, "Drill", "Power drill", true, 2L, 1L);
        ItemRequestDto dto = new ItemRequestDto(1L, "Need a drill",
                LocalDateTime.of(2025, 1, 1, 12, 30, 45), List.of(item));

        JsonContent<ItemRequestDto> result = this.json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Need a drill");
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo("2025-01-01T12:30:45");
        assertThat(result).extractingJsonPathNumberValue("$.items[0].id").isEqualTo(5);
        assertThat(result).extractingJsonPathNumberValue("$.items[0].ownerId").isEqualTo(2);
        assertThat(result).extractingJsonPathNumberValue("$.items[0].requestId").isEqualTo(1);
    }
}
