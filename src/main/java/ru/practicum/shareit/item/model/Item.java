package ru.practicum.shareit.item.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column
    String name;

    @Column
    String description;

    @Column(name = "is_available")
    Boolean available;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    User owner;

    @ManyToOne
    @JoinColumn(name = "request_id")
    ItemRequest request;

    @OneToMany(mappedBy = "item", fetch = FetchType.LAZY)
    Collection<Comment> comments;
}
