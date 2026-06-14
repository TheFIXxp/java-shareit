package ru.practicum.shareit.comment.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class CommentDtoJsonTest {

    @Autowired
    private JacksonTester<CommentDto> json;

    @Test
    @DisplayName("serialize: writes author name and ISO instant")
    void serialize_writesAuthorNameAndInstant() throws Exception {
        CommentDto dto = new CommentDto(1L, "Great drill", "Ivan", Instant.parse("2025-01-01T12:30:45Z"));

        JsonContent<CommentDto> result = this.json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.text").isEqualTo("Great drill");
        assertThat(result).extractingJsonPathStringValue("$.authorName").isEqualTo("Ivan");
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo("2025-01-01T12:30:45Z");
    }
}
