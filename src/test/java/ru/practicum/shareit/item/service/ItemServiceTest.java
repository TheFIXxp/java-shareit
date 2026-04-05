package ru.practicum.shareit.item.service;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ItemServiceTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Test
    @DisplayName("createItem: owner does not exist -> throw NotFoundException")
    void createItem_ownerDoesNotExist_throwNotFoundException() {
        ItemDto dto = TestDataFactory.itemDto(TestConstants.ITEM_NAME_DRILL, TestConstants.ITEM_DESC_POWER_DRILL, TestConstants.AVAILABLE_TRUE);

        assertThrows(NotFoundException.class, () -> itemService.createItem(dto, TestConstants.NOT_FOUND_ID));
    }

    @Test
    @DisplayName("createItem: blank name -> throw ValidationException")
    void createItem_blankName_throwValidationException() {
        long ownerId = createUser(TestConstants.OWNER_EMAIL_1, TestConstants.OWNER_NAME_1).getId();
        ItemDto dto = TestDataFactory.itemDto(TestConstants.BLANK, TestConstants.ITEM_DESC_POWER_DRILL, TestConstants.AVAILABLE_TRUE);

        assertThrows(ValidationException.class, () -> itemService.createItem(dto, ownerId));
    }

    @Test
    @DisplayName("createItem: valid item -> item created")
    void createItem_validItem_itemCreated() {
        long ownerId = createUser(TestConstants.OWNER_EMAIL_2, TestConstants.OWNER_NAME_2).getId();

        ItemDto created = itemService.createItem(TestDataFactory.itemDto(TestConstants.ITEM_NAME_DRILL, TestConstants.ITEM_DESC_POWER_DRILL, TestConstants.AVAILABLE_TRUE), ownerId);

        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals(TestConstants.ITEM_NAME_DRILL, created.getName());
        assertEquals(ownerId, created.getOwnerId());
    }

    @Test
    @DisplayName("updateItem: requester is not owner -> throw NotFoundException")
    void updateItem_requesterIsNotOwner_throwNotFoundException() {
        long ownerId = createUser(TestConstants.OWNER_EMAIL_3, TestConstants.OWNER_NAME_3).getId();
        long otherUserId = createUser(TestConstants.USER_EMAIL_OTHER, TestConstants.USER_NAME_OTHER).getId();
        ItemDto created = itemService.createItem(TestDataFactory.itemDto(TestConstants.ITEM_NAME_BIKE, TestConstants.ITEM_DESC_CITY_BIKE, TestConstants.AVAILABLE_TRUE), ownerId);

        assertThrows(NotFoundException.class,
                     () -> itemService.updateItem(created.getId(), TestDataFactory.itemPatch(TestConstants.ITEM_NAME_UPDATED, null, null, null), otherUserId));
    }

    @Test
    @DisplayName("updateItem: partial patch -> updates only provided fields")
    void updateItem_partialPatch_updatesOnlyProvidedFields() {
        long ownerId = createUser(TestConstants.OWNER_EMAIL_4, TestConstants.OWNER_NAME_4).getId();
        ItemDto created = itemService.createItem(TestDataFactory.itemDto(TestConstants.ITEM_NAME_BIKE, TestConstants.ITEM_DESC_CITY_BIKE, TestConstants.AVAILABLE_TRUE), ownerId);

        ItemDto updated = itemService.updateItem(created.getId(), TestDataFactory.itemPatch(TestConstants.ITEM_NAME_ROAD_BIKE, null, null, null), ownerId);

        assertEquals(TestConstants.ITEM_NAME_ROAD_BIKE, updated.getName());
        assertEquals(TestConstants.ITEM_DESC_CITY_BIKE, updated.getDescription());
        assertEquals(TestConstants.AVAILABLE_TRUE, updated.getAvailable());
    }

    @Test
    @DisplayName("getItemById: item does not exist -> throw NotFoundException")
    void getItemById_itemDoesNotExist_throwNotFoundException() {
        assertThrows(NotFoundException.class, () -> itemService.getItemById(TestConstants.NOT_FOUND_ID));
    }

    @Test
    @DisplayName("getItemsByOwner: returns only owner items")
    void getItemsByOwner_returnsOnlyOwnerItems() {
        long owner1 = createUser(TestConstants.OWNER_EMAIL_5, TestConstants.OWNER_NAME_5).getId();
        long owner2 = createUser(TestConstants.OWNER_EMAIL_6, TestConstants.OWNER_NAME_6).getId();
        itemService.createItem(TestDataFactory.itemDto(TestConstants.ITEM_NAME_BIKE, TestConstants.ITEM_DESC_CITY_BIKE, TestConstants.AVAILABLE_TRUE), owner1);
        itemService.createItem(TestDataFactory.itemDto(TestConstants.ITEM_NAME_TENT, TestConstants.ITEM_DESC_TWO_PERSON_TENT, TestConstants.AVAILABLE_TRUE), owner1);
        itemService.createItem(TestDataFactory.itemDto(TestConstants.ITEM_NAME_SKIS, TestConstants.ITEM_DESC_CROSS_COUNTRY_SKIS, TestConstants.AVAILABLE_TRUE), owner2);

        Collection<ItemDto> owner1Items = itemService.getItemsByOwner(owner1);

        assertEquals(TestConstants.SIZE_TWO, owner1Items.size());
    }

    @Test
    @DisplayName("searchItems: blank text -> empty result")
    void searchItems_blankText_emptyResult() {
        Collection<ItemDto> found = itemService.searchItems(TestConstants.BLANK);

        assertEquals(TestConstants.SIZE_ZERO, found.size());
    }

    @Test
    @DisplayName("searchItems: returns only available matching items")
    void searchItems_returnsOnlyAvailableMatchingItems() {
        long ownerId = createUser(TestConstants.OWNER_EMAIL_7, TestConstants.OWNER_NAME_7).getId();
        itemService.createItem(TestDataFactory.itemDto(TestConstants.ITEM_NAME_DRILL, TestConstants.ITEM_DESC_POWER_DRILL, TestConstants.AVAILABLE_TRUE), ownerId);
        itemService.createItem(TestDataFactory.itemDto(TestConstants.ITEM_NAME_DRILL_OLD, TestConstants.ITEM_DESC_BROKEN_DRILL, TestConstants.AVAILABLE_FALSE), ownerId);
        itemService.createItem(TestDataFactory.itemDto(TestConstants.ITEM_NAME_HAMMER, TestConstants.ITEM_DESC_STEEL_HAMMER, TestConstants.AVAILABLE_TRUE), ownerId);

        Collection<ItemDto> found = itemService.searchItems(TestConstants.SEARCH_DRILL);

        assertEquals(TestConstants.SIZE_ONE, found.size());
        ItemDto only = found.iterator().next();
        assertEquals(TestConstants.ITEM_NAME_DRILL, only.getName());
    }

    private UserDto createUser(String email, String name) {
        return userService.createUser(TestDataFactory.userDto(email, name));
    }
}




