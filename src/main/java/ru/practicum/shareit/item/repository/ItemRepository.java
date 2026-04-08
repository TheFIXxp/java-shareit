package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {


    @Query("SELECT i FROM Item i " +
            "LEFT JOIN FETCH i.comments " +
            "WHERE i.owner.id = ?1 " +
            "ORDER BY i.id")
    Collection<Item> findByOwner_Id(Long ownerId);


    @Query("SELECT i FROM Item i " +
            "LEFT JOIN FETCH i.comments " +
            "WHERE i.id = ?1")
    Optional<Item> findByIdWithComments(Long id);

    @Query("SELECT i FROM Item i " +
            "WHERE i.available = true AND (" +
            "LOWER(i.name) LIKE LOWER(CONCAT('%', ?1, '%')) OR " +
            "LOWER(i.description) LIKE LOWER(CONCAT('%', ?1, '%')))")
    Collection<Item> searchAvailableByText(String text);
}