package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final UserService userService;
    private final ItemStorage itemStorage;

    @Override
    public Item createItem(Item item, Long userId) {
        User user = userService.findUser(userId);
        item.setOwner(user);
        itemStorage.save(item);
        return item;
    }

    @Override
    public Item updateItem(Long itemId, ItemDto itemDto, Long userId) {
        userService.findUser(userId);

        Item oldItem = itemStorage.get(itemId)
                .orElseThrow(() -> new NoSuchElementException("Отсутствует элемент с id : " + itemId));

        if (!oldItem.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Пользователь не имеет права на доступ к этому элементу");
        }

        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            oldItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            oldItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            oldItem.setAvailable(itemDto.getAvailable());
        }

        itemStorage.save(oldItem);
        return oldItem;
    }

    @Override
    public Item getItem(Long itemId) {
        return itemStorage.get(itemId)
                .orElseThrow(() -> new NoSuchElementException("Отсутствует элемент с id : " + itemId));
    }

    @Override
    public List<Item> getItemsByOwner(Long userId) {
        userService.findUser(userId);

        return itemStorage.findAll().stream()
                .filter(item -> item.getOwner().getId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        String searchText = text.toLowerCase();
        return itemStorage.findAll().stream()
                .filter(item -> item.getAvailable() != null && item.getAvailable())
                .filter(item -> (item.getName() != null && item.getName().toLowerCase().contains(searchText))
                        || (item.getDescription() != null && item.getDescription().toLowerCase().contains(searchText)))
                .collect(Collectors.toList());
    }
}