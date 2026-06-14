package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemShortDto;
import ru.practicum.shareit.testutil.TestConstants;
import ru.practicum.shareit.testutil.TestDataFactory;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ItemRequestServiceTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    @Test
    @DisplayName("create: valid user -> request created with id and created timestamp")
    void create_validUser_requestCreated() {
        long userId = this.createRequestor();

        ItemRequestDto created = this.itemRequestService.create(
                TestDataFactory.itemRequestCreate(TestConstants.REQUEST_DESC_NEED_DRILL), userId);

        assertNotNull(created.getId());
        assertEquals(TestConstants.REQUEST_DESC_NEED_DRILL, created.getDescription());
        assertNotNull(created.getCreated());
        assertNotNull(created.getItems());
        assertTrue(created.getItems().isEmpty());
    }

    @Test
    @DisplayName("create: user does not exist -> throw NotFoundException")
    void create_userDoesNotExist_throwNotFoundException() {
        assertThrows(NotFoundException.class, () -> this.itemRequestService.create(
                TestDataFactory.itemRequestCreate(TestConstants.REQUEST_DESC_NEED_DRILL),
                TestConstants.NOT_FOUND_ID));
    }

    @Test
    @DisplayName("getOwnRequests: returns own requests newest first")
    void getOwnRequests_returnsOwnRequestsNewestFirst() {
        long userId = this.createRequestor();
        this.itemRequestService.create(
                TestDataFactory.itemRequestCreate(TestConstants.REQUEST_DESC_NEED_DRILL), userId);
        this.itemRequestService.create(
                TestDataFactory.itemRequestCreate(TestConstants.REQUEST_DESC_NEED_LADDER), userId);

        List<ItemRequestDto> own = this.itemRequestService.getOwnRequests(userId);

        assertEquals(TestConstants.SIZE_TWO, own.size());
        for (int i = 0; i < own.size() - 1; i++) {
            assertTrue(!own.get(i).getCreated().isBefore(own.get(i + 1).getCreated()));
        }
    }

    @Test
    @DisplayName("getOwnRequests: includes item answers")
    void getOwnRequests_includesItemAnswers() {
        long requestorId = this.createRequestor();
        long responderId = this.createResponder();
        ItemRequestDto request = this.itemRequestService.create(
                TestDataFactory.itemRequestCreate(TestConstants.REQUEST_DESC_NEED_DRILL), requestorId);

        ItemDto answer = this.itemService.createItem(
                TestDataFactory.itemCreateRequest(
                        TestConstants.ITEM_NAME_DRILL,
                        TestConstants.ITEM_DESC_POWER_DRILL,
                        TestConstants.AVAILABLE_TRUE,
                        request.getId()),
                responderId);

        List<ItemRequestDto> own = this.itemRequestService.getOwnRequests(requestorId);

        assertEquals(TestConstants.SIZE_ONE, own.size());
        List<ItemShortDto> items = own.get(0).getItems();
        assertEquals(TestConstants.SIZE_ONE, items.size());
        assertEquals(answer.getId(), items.get(0).getId());
        assertEquals(TestConstants.ITEM_NAME_DRILL, items.get(0).getName());
        assertEquals(responderId, items.get(0).getOwnerId());
        assertEquals(request.getId(), items.get(0).getRequestId());
    }

    @Test
    @DisplayName("getAllRequests: returns requests created by other users only")
    void getAllRequests_returnsOthersRequestsOnly() {
        long requestorId = this.createRequestor();
        long responderId = this.createResponder();
        this.itemRequestService.create(
                TestDataFactory.itemRequestCreate(TestConstants.REQUEST_DESC_NEED_DRILL), requestorId);
        this.itemRequestService.create(
                TestDataFactory.itemRequestCreate(TestConstants.REQUEST_DESC_NEED_SAW), responderId);

        List<ItemRequestDto> all = this.itemRequestService.getAllRequests(responderId);

        assertEquals(TestConstants.SIZE_ONE, all.size());
        assertEquals(TestConstants.REQUEST_DESC_NEED_DRILL, all.get(0).getDescription());
    }

    @Test
    @DisplayName("getById: request does not exist -> throw NotFoundException")
    void getById_requestDoesNotExist_throwNotFoundException() {
        long userId = this.createRequestor();

        assertThrows(NotFoundException.class,
                () -> this.itemRequestService.getById(TestConstants.NOT_FOUND_ID, userId));
    }

    @Test
    @DisplayName("getById: returns request with item answers")
    void getById_returnsRequestWithItemAnswers() {
        long requestorId = this.createRequestor();
        long responderId = this.createResponder();
        ItemRequestDto request = this.itemRequestService.create(
                TestDataFactory.itemRequestCreate(TestConstants.REQUEST_DESC_NEED_DRILL), requestorId);
        this.itemService.createItem(
                TestDataFactory.itemCreateRequest(
                        TestConstants.ITEM_NAME_DRILL,
                        TestConstants.ITEM_DESC_POWER_DRILL,
                        TestConstants.AVAILABLE_TRUE,
                        request.getId()),
                responderId);

        ItemRequestDto found = this.itemRequestService.getById(request.getId(), responderId);

        assertEquals(request.getId(), found.getId());
        assertEquals(TestConstants.REQUEST_DESC_NEED_DRILL, found.getDescription());
        assertEquals(TestConstants.SIZE_ONE, found.getItems().size());
    }

    private long createRequestor() {
        return this.createUser(TestConstants.REQUESTOR_EMAIL, TestConstants.REQUESTOR_NAME);
    }

    private long createResponder() {
        return this.createUser(TestConstants.RESPONDER_EMAIL, TestConstants.RESPONDER_NAME);
    }

    private long createUser(String email, String name) {
        UserDto created = this.userService.createUser(TestDataFactory.userDto(email, name));
        return created.getId();
    }
}
