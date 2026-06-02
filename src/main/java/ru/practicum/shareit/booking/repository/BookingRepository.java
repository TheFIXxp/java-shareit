package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Collection<Booking> findByBooker_IdAndEndIsBeforeOrderByEndDesc(Long bookerId, LocalDateTime end);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.booker.id = ?1 " +
            "AND ( " +
            "(?2 = 'ALL') OR " +
            "(?2 = 'CURRENT' AND b.start < ?3 AND b.end > ?3) OR " +
            "(?2 = 'PAST' AND b.end < ?3) OR " +
            "(?2 = 'FUTURE' AND b.start > ?3) OR " +
            "(?2 = 'WAITING' AND b.status = 'WAITING') OR " +
            "(?2 = 'REJECTED' AND b.status = 'REJECTED') " +
            ") " +
            "ORDER BY b.start DESC")
    Collection<Booking> findByBookerIdAndState(Long bookerId, String state, LocalDateTime now);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.owner.id = ?1 " +
            "AND ( " +
            "(?2 = 'ALL') OR " +
            "(?2 = 'CURRENT' AND b.start < ?3 AND b.end > ?3) OR " +
            "(?2 = 'PAST' AND b.end < ?3) OR " +
            "(?2 = 'FUTURE' AND b.start > ?3) OR " +
            "(?2 = 'WAITING' AND b.status = 'WAITING') OR " +
            "(?2 = 'REJECTED' AND b.status = 'REJECTED') " +
            ") " +
            "ORDER BY b.start DESC")
    Collection<Booking> findByOwnerIdAndState(Long ownerId, String state, LocalDateTime now);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id IN (?1) " +
            "AND b.status = 'APPROVED' " +
            "ORDER BY b.start DESC")
    List<Booking> findApprovedByItemIds(List<Long> itemIds);
}
