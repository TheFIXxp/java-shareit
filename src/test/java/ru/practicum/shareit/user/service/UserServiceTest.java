package ru.practicum.shareit.user.service;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.UserAlreadyExistsException;
import ru.practicum.shareit.testutil.TestConstants;
import ru.practicum.shareit.testutil.TestDataFactory;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    @DisplayName("createUser: valid user -> user created with id")
    void createUser_validUser_userCreatedWithId() {
        UserDto created = userService.createUser(TestDataFactory.userDto(TestConstants.USER_EMAIL_IVAN, TestConstants.USER_NAME_IVAN));

        assertNotNull(created);
        assertEquals(TestConstants.FIRST_ID, created.getId());
        assertEquals(TestConstants.USER_EMAIL_IVAN, created.getEmail());
        assertEquals(TestConstants.USER_NAME_IVAN, created.getName());
    }

    @Test
    @DisplayName("createUser: blank email -> throw ValidationException")
    void createUser_blankEmail_throwValidationException() {
        UserDto dto = TestDataFactory.userDto(TestConstants.BLANK, TestConstants.USER_NAME_IVAN);

        assertThrows(ValidationException.class, () -> userService.createUser(dto));
    }

    @Test
    @DisplayName("createUser: duplicate email -> throw UserAlreadyExistsException")
    void createUser_duplicateEmail_throwUserAlreadyExistsException() {
        userService.createUser(TestDataFactory.userDto(TestConstants.USER_EMAIL_SAME, TestConstants.USER_NAME_FIRST));

        assertThrows(UserAlreadyExistsException.class, () -> userService.createUser(TestDataFactory.userDto(TestConstants.USER_EMAIL_SAME, TestConstants.USER_NAME_SECOND)));
    }

    @Test
    @DisplayName("updateUser: user does not exist -> throw NotFoundException")
    void updateUser_userDoesNotExist_throwNotFoundException() {
        UserDto patch = TestDataFactory.userDto(TestConstants.USER_EMAIL_NEW, TestConstants.USER_NAME_NEW);

        assertThrows(NotFoundException.class, () -> userService.updateUser(TestConstants.NOT_FOUND_ID, patch));
    }

    @Test
    @DisplayName("updateUser: partial patch -> updates only provided fields")
    void updateUser_partialPatch_updatesOnlyProvidedFields() {
        UserDto created = userService.createUser(TestDataFactory.userDto(TestConstants.USER_EMAIL_MARIA, TestConstants.USER_NAME_MARIA));

        UserDto updated = userService.updateUser(created.getId(), TestDataFactory.userDto(null, TestConstants.USER_NAME_MARIA_UPDATED));

        assertEquals(created.getId(), updated.getId());
        assertEquals(TestConstants.USER_EMAIL_MARIA, updated.getEmail());
        assertEquals(TestConstants.USER_NAME_MARIA_UPDATED, updated.getName());
    }

    @Test
    @DisplayName("getUserById: user does not exist -> throw NotFoundException")
    void getUserById_userDoesNotExist_throwNotFoundException() {
        assertThrows(NotFoundException.class, () -> userService.getUserById(TestConstants.NOT_FOUND_ID));
    }

    @Test
    @DisplayName("deleteUserById: existing user -> removed from storage")
    void deleteUserById_existingUser_removedFromStorage() {
        UserDto created = userService.createUser(TestDataFactory.userDto(TestConstants.USER_EMAIL_DELETE_ME, TestConstants.USER_NAME_DELETE_ME));

        userService.deleteUserById(created.getId());

        assertThrows(NotFoundException.class, () -> userService.getUserById(created.getId()));
    }

    @Test
    @DisplayName("getUsers: return all created users")
    void getUsers_returnAllCreatedUsers() {
        userService.createUser(TestDataFactory.userDto(TestConstants.USER_EMAIL_ONE, TestConstants.USER_NAME_ONE));
        userService.createUser(TestDataFactory.userDto(TestConstants.USER_EMAIL_TWO, TestConstants.USER_NAME_TWO));

        Collection<UserDto> users = userService.getUsers();

        assertEquals(TestConstants.SIZE_TWO, users.size());
    }
}


