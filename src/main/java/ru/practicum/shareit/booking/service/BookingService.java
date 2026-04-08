package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.Collection;

public interface BookingService {
    BookingDto createBooking(BookingCreateDto createRequest, long userId);

    BookingDto approveBooking(long bookingId, boolean approved, long userId);

    BookingDto getBookingById(long bookingId, long userId);

    Collection<BookingDto> getBookingsByBooker(long userId, String state);

    Collection<BookingDto> getBookingsByOwner(long userId, String state);
}