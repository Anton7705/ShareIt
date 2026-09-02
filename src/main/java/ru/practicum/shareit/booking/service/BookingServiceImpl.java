package ru.practicum.shareit.booking.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@AllArgsConstructor
@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ItemService itemService;
    private final UserService userService;

    @Transactional
    @Override
    public BookingResponseDto createBooking(BookingDto bookingDto, Long userId) {
        log.info("Создание бронирования пользователем с id: {}", userId);

        if (bookingDto.getStart().isAfter(bookingDto.getEnd()) ||
                bookingDto.getStart().equals(bookingDto.getEnd())) {
            throw new IllegalArgumentException("Дата окончания должна быть позже даты начала");
        }

        Item item = itemService.getItem(bookingDto.getItemId());

        if (!item.getAvailable()) {
            throw new IllegalArgumentException("Вещь недоступна для бронирования");
        }


        if (item.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Владелец не может бронировать свою вещь");
        }

        User booker = userService.getUser(userId);

        Booking booking = BookingMapper.toModel(bookingDto);
        booking.setItem(item);
        booking.setBooker(booker);

        Booking saved = bookingRepository.save(booking);
        log.info("Создано бронирование с id: {}", saved.getId());

        return BookingMapper.toResponseDto(saved);
    }

    @Transactional
    @Override
    public BookingResponseDto updateStatus(Long userId, Long bookingId, Boolean approved) {
        log.info("Обновление статуса бронирования {} на {}", bookingId, approved);

        Booking booking = getBooking(bookingId);

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Подтвердить бронирование может только владелец вещи");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            log.warn("Бронирование с id {} уже было обработано ранее", bookingId);
            throw new IllegalArgumentException("Бронирование уже обработано. Текущий статус: " + booking.getStatus());
        }

        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }

        Booking updated = bookingRepository.save(booking);

        log.info("Статус бронирования {} обновлен на {}", bookingId, updated.getStatus());

        return BookingMapper.toResponseDto(updated);

    }

    @Transactional(readOnly = true)
    @Override
    public BookingResponseDto findBooking(Long userId, Long bookingId) {
        log.info("Просмотр статуса бронирования {}", bookingId);

        Booking booking = getBooking(bookingId);

        if (!booking.getItem().getOwner().getId().equals(userId) && !booking.getBooker().getId().equals(userId)) {
            throw new AccessDeniedException("Право на просмотр данных о бронировании имеет только владелец вещи и арендатор");
        }

        return BookingMapper.toResponseDto(booking);
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookingResponseDto> getUserBookings(Long userId, BookingState state) {
        userService.getUser(userId);

        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;

        switch (state) {
            case ALL:
                bookings = bookingRepository.findByBookerIdOrderByStartDesc(userId);
                break;
            case CURRENT:
                bookings = bookingRepository.findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                        userId, now, now);
                break;
            case PAST:
                bookings = bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(userId, now);
                break;
            case FUTURE:
                bookings = bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(userId, now);
                break;
            case WAITING:
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(
                        userId, BookingStatus.WAITING);
                break;
            case REJECTED:
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(
                        userId, BookingStatus.REJECTED);
                break;
            default:
                throw new IllegalArgumentException("Unknown state: " + state);
        }

        return bookings.stream()
                .map(BookingMapper::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookingResponseDto> getOwnerBookings(Long userId, BookingState state) {
        userService.getUser(userId);

        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;

        switch (state) {
            case ALL:
                bookings = bookingRepository.findByItemOwnerIdOrderByStartDesc(userId);
                break;
            case CURRENT:
                bookings = bookingRepository.findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                        userId, now, now);
                break;
            case PAST:
                bookings = bookingRepository.findByItemOwnerIdAndEndBeforeOrderByStartDesc(userId, now);
                break;
            case FUTURE:
                bookings = bookingRepository.findByItemOwnerIdAndStartAfterOrderByStartDesc(userId, now);
                break;
            case WAITING:
                bookings = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(
                        userId, BookingStatus.WAITING);
                break;
            case REJECTED:
                bookings = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(
                        userId, BookingStatus.REJECTED);
                break;
            default:
                throw new IllegalArgumentException("Unknown state: " + state);
        }

        return bookings.stream()
                .map(BookingMapper::toResponseDto)
                .toList();
    }

    private Booking getBooking(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Отсутствует бронирование с id :" + id));
    }
}
