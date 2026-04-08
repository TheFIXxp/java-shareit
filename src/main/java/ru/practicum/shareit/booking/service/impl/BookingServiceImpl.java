package ru.practicum.shareit.booking.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingServiceImpl implements BookingService {

    BookingRepository bookingRepository;
    ItemRepository itemRepository;
    UserRepository userRepository;

    @Override
    public BookingDto createBooking(BookingCreateDto createRequest, long userId) {
        log.info("Creating booking {} for user {}", createRequest, userId);

        User booker = this.userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id %s not found".formatted(userId)));

        Item item = this.itemRepository.findById(createRequest.getItemId())
                .orElseThrow(() -> new NotFoundException(
                        "Item with id %s not found".formatted(createRequest.getItemId())));

        if (!item.getAvailable()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item is not available for booking");
        }

        Booking booking = new Booking();
        booking.setStart(createRequest.getStart());
        booking.setEnd(createRequest.getEnd());
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStatus(Booking.BookingStatus.WAITING);

        Booking saved = this.bookingRepository.save(booking);
        return BookingMapper.toDto(saved);
    }

    @Override
    public BookingDto approveBooking(long bookingId, boolean approved, long userId) {
        log.info("Approving booking {} by user {}", bookingId, userId);

        Booking booking = this.bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking with id %s not found".formatted(bookingId)));

        if (booking.getItem().getOwner().getId() != userId) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only owner can approve booking");

        }

        if (approved) {
            booking.setStatus(Booking.BookingStatus.APPROVED);
        } else {
            booking.setStatus(Booking.BookingStatus.REJECTED);
        }

        Booking updated = this.bookingRepository.save(booking);
        return BookingMapper.toDto(updated);
    }

    @Override
    public BookingDto getBookingById(long bookingId, long userId) {
        log.info("Getting booking {} for user {}", bookingId, userId);

        Booking booking = this.bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking with id %s not found".formatted(bookingId)));

        if (booking.getBooker().getId() != userId && booking.getItem().getOwner().getId() != userId) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only owner or booker can get booking");
        }

        return BookingMapper.toDto(booking);
    }

    @Override
    public Collection<BookingDto> getBookingsByBooker(long userId, String state) {
        log.info("Getting bookings for booker {} with state {}", userId, state);

        this.userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id %s not found".formatted(userId)));

        LocalDateTime now = LocalDateTime.now();
        Collection<Booking> bookings = this.bookingRepository.findByBookerIdOrderByStartDesc(userId);

        return bookings.stream()
                .filter(b -> this.matchesState(b, state, now))
                .map(BookingMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<BookingDto> getBookingsByOwner(long userId, String state) {
        log.info("Getting bookings for owner {} with state {}", userId, state);

        this.userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id %s not found".formatted(userId)));

        LocalDateTime now = LocalDateTime.now();
        Collection<Booking> bookings = this.bookingRepository.findByOwner(userId);

        return bookings.stream()
                .filter(b -> this.matchesState(b, state, now))
                .map(BookingMapper::toDto)
                .collect(Collectors.toList());
    }

    private boolean matchesState(Booking booking, String state, LocalDateTime now) {
        return switch (state.toUpperCase()) {
            case "ALL" -> true;
            case "CURRENT" -> booking.getStart().isBefore(now) && booking.getEnd().isAfter(now);
            case "PAST" -> booking.getEnd().isBefore(now);
            case "FUTURE" -> booking.getStart().isAfter(now);
            case "WAITING" -> booking.getStatus() == Booking.BookingStatus.WAITING;
            case "REJECTED" -> booking.getStatus() == Booking.BookingStatus.REJECTED;
            default -> false;
        };
    }
}