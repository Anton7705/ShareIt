package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import java.util.List;

public interface ItemRequestService {
    ItemRequestDto create(ItemRequestDto dto, Long userId);

    List<ItemRequestDto> findOwn(Long userId);

    List<ItemRequestDto> findOthers(Long userId, int from, int size);

    ItemRequestDto findOne(Long requestId, Long userId);
}
