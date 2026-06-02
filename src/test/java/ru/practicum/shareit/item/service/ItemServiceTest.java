package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.testutil.TestConstants;
import ru.practicum.shareit.testutil.TestDataFactory;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ItemServiceTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Test
    @DisplayName("createItem: owner does not exist -> throw NotFoundException")
    void createItem_ownerDoesNotExist_throwNotFoundException() {
        var createRequest = TestDataFactory.itemCreateRequest(
                TestConstants.ITEM_NAME_DRILL,
                TestConstants.ITEM_DESC_POWER_DRILL,
                TestConstants.AVAILABLE_TRUE
        );

        assertThrows(NotFoundException.class,
                     () -> this.itemService.createItem(createRequest, TestConstants.NOT_FOUND_ID));
    }

    @Test
    @DisplayName("createItem: valid item -> item created")
    void createItem_validItem_itemCreated() {
        long ownerId = this.createUser(TestConstants.OWNER_EMAIL_2, TestConstants.OWNER_NAME_2).getId();
        var createRequest = TestDataFactory.itemCreateRequest(
                TestConstants.ITEM_NAME_DRILL,
                TestConstants.ITEM_DESC_POWER_DRILL,
                TestConstants.AVAILABLE_TRUE
        );

        ItemDto created = this.itemService.createItem(createRequest, ownerId);

        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals(TestConstants.ITEM_NAME_DRILL, created.getName());
        assertEquals(TestConstants.ITEM_DESC_POWER_DRILL, created.getDescription());
        assertTrue(created.getAvailable());
        assertNull(created.getLastBooking());
        assertNull(created.getNextBooking());
        assertNotNull(created.getComments());
    }

    @Test
    @DisplayName("updateItem: requester is not owner -> throw NotFoundException")
    void updateItem_requesterIsNotOwner_throwNotFoundException() {
        long ownerId = this.createUser(TestConstants.OWNER_EMAIL_3, TestConstants.OWNER_NAME_3).getId();
        long otherUserId = this.createUser(TestConstants.USER_EMAIL_OTHER, TestConstants.USER_NAME_OTHER).getId();
        var createRequest = TestDataFactory.itemCreateRequest(
                TestConstants.ITEM_NAME_BIKE,
                TestConstants.ITEM_DESC_CITY_BIKE,
                TestConstants.AVAILABLE_TRUE
        );
        ItemDto created = this.itemService.createItem(createRequest, ownerId);
        var updateRequest = TestDataFactory.itemUpdateRequest(TestConstants.ITEM_NAME_UPDATED, null, null);

        assertThrows(NotFoundException.class,
                     () -> this.itemService.updateItem(created.getId(), updateRequest, otherUserId));
    }

    @Test
    @DisplayName("updateItem: partial patch -> updates only provided fields")
    void updateItem_partialPatch_updatesOnlyProvidedFields() {
        long ownerId = this.createUser(TestConstants.OWNER_EMAIL_4, TestConstants.OWNER_NAME_4).getId();
        var createRequest = TestDataFactory.itemCreateRequest(
                TestConstants.ITEM_NAME_BIKE,
                TestConstants.ITEM_DESC_CITY_BIKE,
                TestConstants.AVAILABLE_TRUE
        );
        ItemDto created = this.itemService.createItem(createRequest, ownerId);
        var updateRequest = TestDataFactory.itemUpdateRequest(TestConstants.ITEM_NAME_ROAD_BIKE, null, null);

        ItemDto updated = this.itemService.updateItem(created.getId(), updateRequest, ownerId);

        assertEquals(TestConstants.ITEM_NAME_ROAD_BIKE, updated.getName());
        assertEquals(TestConstants.ITEM_DESC_CITY_BIKE, updated.getDescription());
        assertTrue(updated.getAvailable());
    }

    @Test
    @DisplayName("getItemById: item does not exist -> throw NotFoundException")
    void getItemById_itemDoesNotExist_throwNotFoundException() {
        assertThrows(NotFoundException.class, () -> this.itemService.getItemById(TestConstants.NOT_FOUND_ID));
    }

    @Test
    @DisplayName("getItemsByOwner: returns only owner items")
    void getItemsByOwner_returnsOnlyOwnerItems() {
        long owner1 = this.createUser(TestConstants.OWNER_EMAIL_5, TestConstants.OWNER_NAME_5).getId();
        long owner2 = this.createUser(TestConstants.OWNER_EMAIL_6, TestConstants.OWNER_NAME_6).getId();

        this.itemService.createItem(
                TestDataFactory.itemCreateRequest(
                        TestConstants.ITEM_NAME_BIKE,
                        TestConstants.ITEM_DESC_CITY_BIKE,
                        TestConstants.AVAILABLE_TRUE),
                owner1
        );
        this.itemService.createItem(
                TestDataFactory.itemCreateRequest(
                        TestConstants.ITEM_NAME_TENT,
                        TestConstants.ITEM_DESC_TWO_PERSON_TENT,
                        TestConstants.AVAILABLE_TRUE),
                owner1
        );
        this.itemService.createItem(
                TestDataFactory.itemCreateRequest(
                        TestConstants.ITEM_NAME_SKIS,
                        TestConstants.ITEM_DESC_CROSS_COUNTRY_SKIS,
                        TestConstants.AVAILABLE_TRUE),
                owner2
        );

        Collection<ItemDto> owner1Items = this.itemService.getItemsByOwner(owner1);

        assertEquals(TestConstants.SIZE_TWO, owner1Items.size());
    }

    @Test
    @DisplayName("searchItems: blank text -> empty result")
    void searchItems_blankText_emptyResult() {
        Collection<ItemDto> found = this.itemService.searchItems(TestConstants.BLANK);

        assertEquals(TestConstants.SIZE_ZERO, found.size());
    }

    @Test
    @DisplayName("searchItems: returns only available matching items")
    void searchItems_returnsOnlyAvailableMatchingItems() {
        long ownerId = this.createUser(TestConstants.OWNER_EMAIL_7, TestConstants.OWNER_NAME_7).getId();

        this.itemService.createItem(
                TestDataFactory.itemCreateRequest(
                        TestConstants.ITEM_NAME_DRILL,
                        TestConstants.ITEM_DESC_POWER_DRILL,
                        TestConstants.AVAILABLE_TRUE),
                ownerId
        );
        this.itemService.createItem(
                TestDataFactory.itemCreateRequest(
                        TestConstants.ITEM_NAME_DRILL_OLD,
                        TestConstants.ITEM_DESC_BROKEN_DRILL,
                        TestConstants.AVAILABLE_FALSE),
                ownerId
        );
        this.itemService.createItem(
                TestDataFactory.itemCreateRequest(
                        TestConstants.ITEM_NAME_HAMMER,
                        TestConstants.ITEM_DESC_STEEL_HAMMER,
                        TestConstants.AVAILABLE_TRUE),
                ownerId
        );

        Collection<ItemDto> found = this.itemService.searchItems(TestConstants.SEARCH_DRILL);

        assertEquals(TestConstants.SIZE_ONE, found.size());
        ItemDto only = found.iterator().next();
        assertEquals(TestConstants.ITEM_NAME_DRILL, only.getName());
    }

    private UserDto createUser(String email, String name) {
        return this.userService.createUser(TestDataFactory.userDto(email, name));
    }
}
