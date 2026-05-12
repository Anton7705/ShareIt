package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemStorage {
    Item save(Item item);

    Optional<Item> get(Long id);

    List<Item> findAll();

    void delete(Long id);

    List<Item> findAllContainsText(String searchText);

    List<Item> findAllByOwnerId(Long userId);
}
