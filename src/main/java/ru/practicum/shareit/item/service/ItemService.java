package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    Item createItem(Item item, Long userId);

    Item updateItem(Long itemId, ItemDto itemDto, Long userId);

    Item getItem(Long itemId);

    List<Item> getItemsByOwner(Long userId);

    List<Item> searchItems(String text);
}