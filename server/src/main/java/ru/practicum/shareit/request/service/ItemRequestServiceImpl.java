package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository requestRepository;
    private final ItemRepository itemRepository;
    private final UserService userService;

    @Override
    @Transactional
    public ItemRequestDto create(ItemRequestDto dto, Long userId) {
        ItemRequest request = ItemRequest.builder()
                .description(dto.getDescription())
                .requestor(userService.getUser(userId))
                .created(LocalDateTime.now()).build();
        request = requestRepository.save(request);
        return ItemRequestMapper.toDto(request);
    }

    @Override
    public List<ItemRequestDto> findOwn(Long userId) {
        userService.getUser(userId);
        return withItems(requestRepository.findByRequestorIdOrderByCreatedDescIdDesc(userId));
    }

    @Override
    public List<ItemRequestDto> findOthers(Long userId, int from, int size) {
        userService.getUser(userId);
        List<ItemRequest> requests = requestRepository.findByRequestorIdNotOrderByCreatedDescIdDesc(userId)
                .stream().skip(from).limit(size).toList();
        return withItems(requests);
    }

    @Override
    public ItemRequestDto findOne(Long requestId, Long userId) {
        userService.getUser(userId);
        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NoSuchElementException("Запрос не найден: " + requestId));
        return withItems(List.of(request)).getFirst();
    }

    private List<ItemRequestDto> withItems(List<ItemRequest> requests) {
        if (requests.isEmpty()) {
            return List.of();
        }
        List<Long> ids = requests.stream().map(ItemRequest::getId).toList();
        Map<Long, List<Item>> items = itemRepository.findByRequestIdInOrderByIdAsc(ids).stream()
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));
        return requests.stream()
                .map(request -> ItemRequestMapper.toDto(request, items.getOrDefault(request.getId(), List.of())))
                .toList();
    }
}