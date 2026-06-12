package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.util.List;

public interface BookingService {

    BookingResponseDto createBooking(BookingDto bookingDto, Long userId);

    BookingResponseDto updateStatus(Long userId, Long bookingId, Boolean approved);

    BookingResponseDto findBooking(Long userId, Long bookingId);

    List<BookingResponseDto> getUserBookings(Long userId, BookingState state);

    List<BookingResponseDto> getOwnerBookings(Long userId, BookingState state);
}
