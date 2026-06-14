package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserClient userClient;

    @Test
    @DisplayName("POST /users: valid -> forwards to client")
    void createUser_valid_forwards() throws Exception {
        when(this.userClient.createUser(any())).thenReturn(ResponseEntity.ok(Map.of("id", 1)));

        this.mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(new UserCreateDto("ivan@example.com", "Ivan"))))
                .andExpect(status().isOk());

        verify(this.userClient).createUser(any());
    }

    @Test
    @DisplayName("POST /users: invalid email -> 400, client not called")
    void createUser_invalidEmail_returns400() throws Exception {
        this.mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(new UserCreateDto("not-an-email", "Ivan"))))
                .andExpect(status().isBadRequest());

        verify(this.userClient, never()).createUser(any());
    }

    @Test
    @DisplayName("POST /users: blank name -> 400, client not called")
    void createUser_blankName_returns400() throws Exception {
        this.mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(new UserCreateDto("ivan@example.com", " "))))
                .andExpect(status().isBadRequest());

        verify(this.userClient, never()).createUser(any());
    }

    @Test
    @DisplayName("PATCH /users/{id}: partial update with only name -> forwards")
    void updateUser_partialName_forwards() throws Exception {
        when(this.userClient.updateUser(anyLong(), any())).thenReturn(ResponseEntity.ok(Map.of("id", 1)));

        this.mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(new UserUpdateDto(null, "Ivan Updated"))))
                .andExpect(status().isOk());

        verify(this.userClient).updateUser(anyLong(), any());
    }
}
