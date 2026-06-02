package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto createItem(ItemDto item, Long userId);

    ItemDto updateItem(Long itemId, ItemDto itemDto, Long userId);

    ItemDto findItem(Long itemId);

    List<ItemDto> findItemsByOwner(Long userId);

    List<ItemDto> searchItems(String text);
}