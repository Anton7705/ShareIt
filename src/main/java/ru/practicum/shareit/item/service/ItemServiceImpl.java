package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final UserService userService;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Transactional
    @Override
    public ItemDto createItem(ItemDto itemDto, Long userId) {
        Item item = ItemMapper.toModel(itemDto);
        item.setId(null);
        User user = UserMapper.toModel(userService.findUser(userId));
        item.setOwner(user);
        itemRepository.save(item);
        return ItemMapper.toDto(item);
    }

    @Transactional
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

        itemRepository.save(oldItem);
        return ItemMapper.toDto(oldItem);
    }

    @Transactional(readOnly = true)
    @Override
    public ItemResponseDto findItem(Long itemId, Long userId) {
        log.info("Запрос элемента с id : {} от пользователя {}", itemId, userId);
        Item item = getItem(itemId);
        return enrichWithBookings(item, userId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemResponseDto> findItemsByOwner(Long userId) {
        log.info("Запрос всех элементов пользователя с id : {}", userId);
        userService.getUser(userId);

        List<Item> items = itemRepository.findAllByOwnerId(userId);

        if (items.isEmpty()) {
            return List.of();
        }

        return enrichItemsWithBookings(items, userId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemDto> searchItems(String text) {
        log.info("Поиск элемента по совпадению описания/имени");
        if (text == null || text.isBlank()) {
            return List.of();
        }

        String searchText = text.toLowerCase();
        return itemRepository.searchByText(searchText).stream()
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Item getItem(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Отсутствует элемент с id : " + itemId));
    }

    @Transactional
    @Override
    public CommentDto addComment(Long itemId, Long userId, CommentDto commentDto) {
        log.info("Добавление комментария к вещи {} от пользователя {}", itemId, userId);

        Item item = getItem(itemId);
        User author = userService.getUser(userId);

        boolean hasCompletedBooking = bookingRepository.existsCompletedBooking(
                userId, itemId, LocalDateTime.now());

        if (!hasCompletedBooking) {
            throw new IllegalArgumentException("Пользователь не брал эту вещь в аренду или аренда еще не завершена");
        }

        Comment comment = ItemMapper.toComment(commentDto, item, author);
        Comment saved = commentRepository.save(comment);

        return ItemMapper.toCommentDto(saved);
    }

    private ItemResponseDto enrichWithBookings(Item item, Long userId) {
        boolean isOwner = item.getOwner().getId().equals(userId);

        BookingShortDto lastBooking = null;
        BookingShortDto nextBooking = null;

        if (isOwner) {
            LocalDateTime now = LocalDateTime.now();

            List<Booking> pastBookings = bookingRepository.findPastBookingsForItem(item.getId(), now);
            if (!pastBookings.isEmpty()) {
                lastBooking = ItemMapper.toBookingShortDto(pastBookings.get(0));
            }

            List<Booking> futureBookings = bookingRepository.findFutureBookingsForItem(item.getId(), now);
            if (!futureBookings.isEmpty()) {
                nextBooking = ItemMapper.toBookingShortDto(futureBookings.get(0));
            }
        }

        List<CommentDto> comments = commentRepository.findByItemIdOrderByCreatedDesc(item.getId())
                .stream()
                .map(ItemMapper::toCommentDto)
                .collect(Collectors.toList());

        return ItemMapper.toResponseDto(item, lastBooking, nextBooking, comments);
    }

    private List<ItemResponseDto> enrichItemsWithBookings(List<Item> items, Long userId) {
        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .collect(Collectors.toList());

        LocalDateTime now = LocalDateTime.now();

        List<Booking> allPastBookings = bookingRepository.findPastBookingsForItems(itemIds, now);
        List<Booking> allFutureBookings = bookingRepository.findFutureBookingsForItems(itemIds, now);

        Map<Long, Booking> lastBookingMap = allPastBookings.stream()
                .collect(Collectors.toMap(
                        b -> b.getItem().getId(),
                        b -> b,
                        (existing, replacement) -> existing
                ));

        Map<Long, Booking> nextBookingMap = allFutureBookings.stream()
                .collect(Collectors.toMap(
                        b -> b.getItem().getId(),
                        b -> b,
                        (existing, replacement) -> existing
                ));

        List<Comment> allComments = commentRepository.findByItemIdInOrderByCreatedDesc(itemIds);
        Map<Long, List<CommentDto>> commentsMap = allComments.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getItem().getId(),
                        Collectors.mapping(ItemMapper::toCommentDto, Collectors.toList())
                ));

        return items.stream()
                .map(item -> {
                    BookingShortDto last = null;
                    BookingShortDto next = null;

                    if (item.getOwner().getId().equals(userId)) {
                        Booking lastBooking = lastBookingMap.get(item.getId());
                        if (lastBooking != null) {
                            last = ItemMapper.toBookingShortDto(lastBooking);
                        }
                        Booking nextBooking = nextBookingMap.get(item.getId());
                        if (nextBooking != null) {
                            next = ItemMapper.toBookingShortDto(nextBooking);
                        }
                    }
                    List<CommentDto> comments = commentsMap.getOrDefault(item.getId(), List.of());

                    return ItemMapper.toResponseDto(item, last, next, comments);
                })
                .collect(Collectors.toList());
    }
}