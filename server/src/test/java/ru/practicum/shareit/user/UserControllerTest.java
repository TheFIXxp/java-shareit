package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("POST /users: creates user")
    void createUser_returnsCreatedUser() throws Exception {
        UserDto response = new UserDto(1L, "ivan@example.com", "Ivan");
        when(this.userService.createUser(any())).thenReturn(response);

        this.mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(new UserDto(0L, "ivan@example.com", "Ivan"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("ivan@example.com"))
                .andExpect(jsonPath("$.name").value("Ivan"));
    }

    @Test
    @DisplayName("POST /users: invalid email -> 400")
    void createUser_invalidEmail_returns400() throws Exception {
        this.mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(new UserDto(0L, "not-an-email", "Ivan"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /users: returns all users")
    void getUsers_returnsList() throws Exception {
        when(this.userService.getUsers()).thenReturn(List.of(new UserDto(1L, "ivan@example.com", "Ivan")));

        this.mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @DisplayName("GET /users/{id}: not found -> 404")
    void getUserById_notFound_returns404() throws Exception {
        when(this.userService.getUserById(anyLong()))
                .thenThrow(new NotFoundException("User with id 999 not found"));

        this.mockMvc.perform(get("/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /users/{id}: blank name -> 400")
    void updateUser_blankName_returns400() throws Exception {
        when(this.userService.updateUser(anyLong(), any()))
                .thenThrow(new ValidationException("Name must not be blank"));

        this.mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(new UserDto(0L, "ivan@example.com", " "))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /users/{id}: invokes service")
    void deleteUser_invokesService() throws Exception {
        this.mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());

        verify(this.userService).deleteUserById(1L);
    }
}
