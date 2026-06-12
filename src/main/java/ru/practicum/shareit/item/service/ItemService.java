package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    ItemDto createItem(ItemDto item, Long userId);

    ItemDto updateItem(Long itemId, ItemDto itemDto, Long userId);

    ItemResponseDto findItem(Long itemId, Long userId);

    List<ItemResponseDto> findItemsByOwner(Long userId);

    List<ItemDto> searchItems(String text);

    Item getItem(Long itemId);

    CommentDto addComment(Long itemId, Long userId, CommentDto commentDto);
}