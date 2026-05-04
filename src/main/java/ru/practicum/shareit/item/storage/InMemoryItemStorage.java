package ru.practicum.shareit.item.storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;

import java.util.*;

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

    public Optional<Item> get(Long id) {
        return Optional.ofNullable(itemMap.get(id));
    }

    public List<Item> findAll() {
        return new ArrayList<>(itemMap.values());
    }

    public void delete(Long id) {
        itemMap.remove(id);
    }
}
