package ru.practicum.shareit.item.storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class InMemoryItemStorage implements ItemStorage {
    private long currentId = 1;
    private final Map<Long, Item> itemMap = new HashMap<>();

    public Item save(Item item) {
        if (item.getId() == null) {
            item.setId(currentId++);
        }
        itemMap.put(item.getId(), item);
        return item;
    }

    @Override
    public Optional<Item> get(Long id) {
        return Optional.ofNullable(itemMap.get(id));
    }

    @Override
    public List<Item> findAll() {
        return new ArrayList<>(itemMap.values());
    }

    @Override
    public void delete(Long id) {
        itemMap.remove(id);
    }

    @Override
    public List<Item> findAllContainsText(String searchText) {
        return itemMap.values().stream()
                .filter(item -> item.getAvailable() != null && item.getAvailable())
                .filter(item -> (item.getName() != null && item.getName().toLowerCase().contains(searchText))
                        || (item.getDescription() != null && item.getDescription().toLowerCase().contains(searchText)))
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> findAllByOwnerId(Long userId) {
        return itemMap.values().stream()
                .filter(item -> item.getOwner().getId().equals(userId))
                .collect(Collectors.toList());
    }
}
