package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.client.BaseClient;
import java.util.Map;
import ru.practicum.shareit.request.dto.ItemRequestDto;

@RestController
@RequestMapping("/requests")
@Validated
@RequiredArgsConstructor
public class ItemRequestController {
    private final BaseClient client;

    @PostMapping
    public ResponseEntity<Object> create(@Valid @RequestBody ItemRequestDto dto,
                                         @Positive @RequestHeader("X-Sharer-User-Id") Long userId) {
        return client.send(HttpMethod.POST, "/requests", userId, dto, Map.of());
    }

    @GetMapping
    public ResponseEntity<Object> findOwn(@Positive @RequestHeader("X-Sharer-User-Id") Long userId) {
        return client.send(HttpMethod.GET, "/requests", userId, null, Map.of());
    }

    @GetMapping("/all")
    public ResponseEntity<Object> findOthers(@Positive @RequestHeader("X-Sharer-User-Id") Long userId,
                                             @Min(0) @RequestParam(defaultValue = "0") int from,
                                             @Positive @RequestParam(defaultValue = "2147483647") int size) {
        return client.send(HttpMethod.GET, "/requests/all", userId, null, Map.of("from", from, "size", size));
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> find(@Positive @PathVariable Long requestId,
                                       @Positive @RequestHeader("X-Sharer-User-Id") Long userId) {
        return client.send(HttpMethod.GET, "/requests/" + requestId, userId, null, Map.of());
    }
}
