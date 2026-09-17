package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.client.BaseClient;
import java.util.Map;
import ru.practicum.shareit.booking.dto.BookingDto;

@RestController
@RequestMapping("/bookings")
@Validated
@RequiredArgsConstructor
public class BookingController {
    private final BaseClient client;

    @PostMapping
    public ResponseEntity<Object> create(@Valid @RequestBody BookingDto dto,
                                         @Positive @RequestHeader("X-Sharer-User-Id") Long userId) {
        return client.send(HttpMethod.POST, "/bookings", userId, dto, Map.of());
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approve(@Positive @PathVariable Long bookingId,
                                          @Positive @RequestHeader("X-Sharer-User-Id") Long userId,
                                          @RequestParam Boolean approved) {
        return client.send(HttpMethod.PATCH, "/bookings/" + bookingId, userId, null, Map.of("approved", approved));
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> find(@Positive @PathVariable Long bookingId,
                                       @Positive @RequestHeader("X-Sharer-User-Id") Long userId) {
        return client.send(HttpMethod.GET, "/bookings/" + bookingId, userId, null, Map.of());
    }

    @GetMapping
    public ResponseEntity<Object> findOwn(@Positive @RequestHeader("X-Sharer-User-Id") Long userId,
                                          @RequestParam(defaultValue = "ALL") BookingState state) {
        return client.send(HttpMethod.GET, "/bookings", userId, null, Map.of("state", state));
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> findOwner(@Positive @RequestHeader("X-Sharer-User-Id") Long userId,
                                            @RequestParam(defaultValue = "ALL") BookingState state) {
        return client.send(HttpMethod.GET, "/bookings/owner", userId, null, Map.of("state", state));
    }
}
