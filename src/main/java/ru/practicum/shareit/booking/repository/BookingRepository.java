package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.Collection;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Collection<Booking> findByBooker_IdAndEndIsBeforeOrderByEndDesc(Long bookerId, LocalDateTime end);

    @Query("SELECT b FROM Booking b WHERE b.item.id IN " +
            "(SELECT i.id FROM Item i WHERE i.owner.id = ?1) " +
            "ORDER BY b.start DESC")
    Collection<Booking> findByOwner(Long ownerId);

    Collection<Booking> findByBookerIdOrderByStartDesc(Long bookerId);

    @Query("SELECT b FROM Booking b WHERE b.item.id = ?1 " +
            "AND b.end < ?2 AND b.status = 'APPROVED' " +
            "ORDER BY b.end DESC")
    Collection<Booking> findLastBookingByItem(Long itemId, LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.item.id = ?1 " +
            "AND b.start > ?2 AND b.status = 'APPROVED' " +
            "ORDER BY b.start ASC")
    Collection<Booking> findNextBookingByItem(Long itemId, LocalDateTime now);
}

