package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.testutil.TestConstants;
import ru.practicum.shareit.testutil.TestDataFactory;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class BookingServiceTest {

    private static final String STATE_ALL = "ALL";

    @Autowired
    private BookingService bookingService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Test
    @DisplayName("createBooking: valid request -> booking created with WAITING status")
    void createBooking_validRequest_bookingCreatedWithWaitingStatus() {
        long ownerId = this.createUser(TestConstants.OWNER_EMAIL_1, TestConstants.OWNER_NAME_1);
        long bookerId = this.createUser(TestConstants.BOOKER_EMAIL, TestConstants.BOOKER_NAME);
        long itemId = this.createItem(ownerId, TestConstants.AVAILABLE_TRUE);

        BookingDto created = this.bookingService.createBooking(this.futureBooking(itemId), bookerId);

        assertNotNull(created.getId());
        assertEquals(Booking.BookingStatus.WAITING, created.getStatus());
        assertEquals(itemId, created.getItem().getId());
        assertEquals(bookerId, created.getBooker().getId());
    }

    @Test
    @DisplayName("createBooking: item not available -> throw 400")
    void createBooking_itemNotAvailable_throwBadRequest() {
        long ownerId = this.createUser(TestConstants.OWNER_EMAIL_1, TestConstants.OWNER_NAME_1);
        long bookerId = this.createUser(TestConstants.BOOKER_EMAIL, TestConstants.BOOKER_NAME);
        long itemId = this.createItem(ownerId, TestConstants.AVAILABLE_FALSE);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> this.bookingService.createBooking(this.futureBooking(itemId), bookerId));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    @DisplayName("createBooking: start after end -> throw 400")
    void createBooking_startAfterEnd_throwBadRequest() {
        long ownerId = this.createUser(TestConstants.OWNER_EMAIL_1, TestConstants.OWNER_NAME_1);
        long bookerId = this.createUser(TestConstants.BOOKER_EMAIL, TestConstants.BOOKER_NAME);
        long itemId = this.createItem(ownerId, TestConstants.AVAILABLE_TRUE);

        LocalDateTime now = LocalDateTime.now();
        BookingCreateDto request =
                TestDataFactory.bookingCreateRequest(itemId, now.plusDays(2), now.plusDays(1));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> this.bookingService.createBooking(request, bookerId));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    @DisplayName("createBooking: item does not exist -> throw NotFoundException")
    void createBooking_itemDoesNotExist_throwNotFoundException() {
        long bookerId = this.createUser(TestConstants.BOOKER_EMAIL, TestConstants.BOOKER_NAME);

        assertThrows(NotFoundException.class,
                () -> this.bookingService.createBooking(this.futureBooking(TestConstants.NOT_FOUND_ID), bookerId));
    }

    @Test
    @DisplayName("approveBooking: by owner -> status APPROVED")
    void approveBooking_byOwner_statusApproved() {
        long ownerId = this.createUser(TestConstants.OWNER_EMAIL_1, TestConstants.OWNER_NAME_1);
        long bookerId = this.createUser(TestConstants.BOOKER_EMAIL, TestConstants.BOOKER_NAME);
        long itemId = this.createItem(ownerId, TestConstants.AVAILABLE_TRUE);
        BookingDto created = this.bookingService.createBooking(this.futureBooking(itemId), bookerId);

        BookingDto approved = this.bookingService.approveBooking(created.getId(), true, ownerId);

        assertEquals(Booking.BookingStatus.APPROVED, approved.getStatus());
    }

    @Test
    @DisplayName("approveBooking: by non-owner -> throw 403")
    void approveBooking_byNonOwner_throwForbidden() {
        long ownerId = this.createUser(TestConstants.OWNER_EMAIL_1, TestConstants.OWNER_NAME_1);
        long bookerId = this.createUser(TestConstants.BOOKER_EMAIL, TestConstants.BOOKER_NAME);
        long itemId = this.createItem(ownerId, TestConstants.AVAILABLE_TRUE);
        BookingDto created = this.bookingService.createBooking(this.futureBooking(itemId), bookerId);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> this.bookingService.approveBooking(created.getId(), true, bookerId));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    @DisplayName("getBookingById: by booker -> returns booking")
    void getBookingById_byBooker_returnsBooking() {
        long ownerId = this.createUser(TestConstants.OWNER_EMAIL_1, TestConstants.OWNER_NAME_1);
        long bookerId = this.createUser(TestConstants.BOOKER_EMAIL, TestConstants.BOOKER_NAME);
        long itemId = this.createItem(ownerId, TestConstants.AVAILABLE_TRUE);
        BookingDto created = this.bookingService.createBooking(this.futureBooking(itemId), bookerId);

        BookingDto found = this.bookingService.getBookingById(created.getId(), bookerId);

        assertEquals(created.getId(), found.getId());
    }

    @Test
    @DisplayName("getBookingById: by stranger -> throw 403")
    void getBookingById_byStranger_throwForbidden() {
        long ownerId = this.createUser(TestConstants.OWNER_EMAIL_1, TestConstants.OWNER_NAME_1);
        long bookerId = this.createUser(TestConstants.BOOKER_EMAIL, TestConstants.BOOKER_NAME);
        long strangerId = this.createUser(TestConstants.USER_EMAIL_OTHER, TestConstants.USER_NAME_OTHER);
        long itemId = this.createItem(ownerId, TestConstants.AVAILABLE_TRUE);
        BookingDto created = this.bookingService.createBooking(this.futureBooking(itemId), bookerId);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> this.bookingService.getBookingById(created.getId(), strangerId));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    @DisplayName("getBookingsByBooker: state ALL -> returns booker bookings")
    void getBookingsByBooker_stateAll_returnsBookings() {
        long ownerId = this.createUser(TestConstants.OWNER_EMAIL_1, TestConstants.OWNER_NAME_1);
        long bookerId = this.createUser(TestConstants.BOOKER_EMAIL, TestConstants.BOOKER_NAME);
        long itemId = this.createItem(ownerId, TestConstants.AVAILABLE_TRUE);
        this.bookingService.createBooking(this.futureBooking(itemId), bookerId);

        Collection<BookingDto> bookings = this.bookingService.getBookingsByBooker(bookerId, STATE_ALL);

        assertEquals(TestConstants.SIZE_ONE, bookings.size());
    }

    @Test
    @DisplayName("getBookingsByOwner: state ALL -> returns owner bookings")
    void getBookingsByOwner_stateAll_returnsBookings() {
        long ownerId = this.createUser(TestConstants.OWNER_EMAIL_1, TestConstants.OWNER_NAME_1);
        long bookerId = this.createUser(TestConstants.BOOKER_EMAIL, TestConstants.BOOKER_NAME);
        long itemId = this.createItem(ownerId, TestConstants.AVAILABLE_TRUE);
        this.bookingService.createBooking(this.futureBooking(itemId), bookerId);

        Collection<BookingDto> bookings = this.bookingService.getBookingsByOwner(ownerId, STATE_ALL);

        assertEquals(TestConstants.SIZE_ONE, bookings.size());
    }

    private BookingCreateDto futureBooking(long itemId) {
        LocalDateTime now = LocalDateTime.now();
        return TestDataFactory.bookingCreateRequest(itemId, now.plusDays(1), now.plusDays(2));
    }

    private long createItem(long ownerId, boolean available) {
        ItemDto created = this.itemService.createItem(
                TestDataFactory.itemCreateRequest(
                        TestConstants.ITEM_NAME_DRILL,
                        TestConstants.ITEM_DESC_POWER_DRILL,
                        available),
                ownerId);
        return created.getId();
    }

    private long createUser(String email, String name) {
        UserDto created = this.userService.createUser(TestDataFactory.userDto(email, name));
        return created.getId();
    }
}
