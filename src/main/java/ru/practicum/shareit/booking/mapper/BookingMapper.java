package ru.practicum.shareit.booking.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.dto.BookerDto;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.ItemForBookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

@UtilityClass
public class BookingMapper {

    public BookingDto toDto(Booking booking) {
        BookingDto dto = new BookingDto();
        dto.setId(booking.getId());
        dto.setStart(booking.getStart());
        dto.setEnd(booking.getEnd());
        dto.setStatus(booking.getStatus());

        if (booking.getItem() != null) {
            ItemForBookingDto itemDto = new ItemForBookingDto();
            itemDto.setId(booking.getItem().getId());
            itemDto.setName(booking.getItem().getName());
            dto.setItem(itemDto);
        }

        if (booking.getBooker() != null) {
            BookerDto bookerDto = new BookerDto();
            bookerDto.setId(booking.getBooker().getId());
            bookerDto.setName(booking.getBooker().getName());
            dto.setBooker(bookerDto);
        }

        return dto;
    }

    public Booking fromDto(BookingCreateDto createRequest, User booker, Item item) {
        Booking booking = new Booking();
        booking.setStart(createRequest.getStart());
        booking.setEnd(createRequest.getEnd());
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStatus(Booking.BookingStatus.WAITING);
        return booking;
    }

}

