package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final UserService userService;
    private final ItemStorage itemStorage;

    @Override
    public ItemDto createItem(ItemDto itemDto, Long userId) {
        Item item = ItemMapper.toModel(itemDto);
        item.setId(null);
        User user = UserMapper.toModel(userService.findUser(userId));
        item.setOwner(user);
        itemStorage.save(item);
        return ItemMapper.toDto(item);
    }

    @Override
    public ItemDto updateItem(Long itemId, ItemDto itemDto, Long userId) {
        log.info("Обновление элемента с id : {}", itemId);
        userService.findUser(userId);

        Item oldItem = getItem(itemId);

        if (!oldItem.getOwner().getId().equals(userId)) {
            log.warn("Ошибка прав досутпа");
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
        return ItemMapper.toDto(oldItem);
    }

    @Override
    public ItemDto findItem(Long itemId) {
        log.info("Запрос элемента с id : {}", itemId);
        return ItemMapper.toDto(getItem(itemId));
    }

    @Override
    public List<ItemDto> findItemsByOwner(Long userId) {
        log.info("Запрос всех элементов пользователя с id : {}", userId);
        userService.findUser(userId);
        return itemStorage.findAllByOwnerId(userId).stream()
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        log.info("Поиск элемента по совпадению описания/имени");
        if (text == null || text.isBlank()) {
            return List.of();
        }

        String searchText = text.toLowerCase();
        return itemStorage.findAllContainsText(searchText).stream()
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
    }

    private Item getItem(Long itemId) {
        return itemStorage.get(itemId)
                .orElseThrow(() -> new NoSuchElementException("Отсутствует элемент с id : " + itemId));
    }
}