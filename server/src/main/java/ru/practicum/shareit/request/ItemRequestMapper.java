package ru.practicum.shareit.request;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.RequestItemDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public class ItemRequestMapper {

    public static ItemRequestDto toDto(ItemRequest request, List<Item> items) {
        return ItemRequestDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .items(items.stream()
                        .map(ItemRequestMapper::toRequestItemDto)
                        .toList())
                .build();
    }

    public static ItemRequestDto toDto(ItemRequest request) {
        return toDto(request, List.of());
    }

    public static RequestItemDto toRequestItemDto(Item item) {
        return RequestItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .ownerId(item.getOwner().getId())
                .build();
    }

    public static ItemRequest toModel(ItemRequestDto dto) {
        return ItemRequest.builder()
                .id(dto.getId())
                .description(dto.getDescription())
                .created(dto.getCreated())
                .build();
    }
}