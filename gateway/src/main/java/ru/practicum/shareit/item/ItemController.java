package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.validation.Create;
import ru.practicum.shareit.validation.Update;

import java.util.List;
import java.util.Map;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.CommentDto;

@RestController
@RequestMapping("/items")
@Validated
@RequiredArgsConstructor
public class ItemController {
    private final BaseClient client;

    @PostMapping
    public ResponseEntity<Object> create(@Validated(Create.class) @RequestBody ItemDto dto,
                                         @Positive @RequestHeader("X-Sharer-User-Id") Long userId) {
        return client.send(HttpMethod.POST, "/items", userId, dto, Map.of());
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@Positive @PathVariable Long itemId,
                                         @Validated(Update.class) @RequestBody ItemDto dto,
                                         @Positive @RequestHeader("X-Sharer-User-Id") Long userId) {
        return client.send(HttpMethod.PATCH, "/items/" + itemId, userId, dto, Map.of());
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> find(@Positive @PathVariable Long itemId,
                                       @Positive @RequestHeader("X-Sharer-User-Id") Long userId) {
        return client.send(HttpMethod.GET, "/items/" + itemId, userId, null, Map.of());
    }

    @GetMapping
    public ResponseEntity<Object> findOwn(@Positive @RequestHeader("X-Sharer-User-Id") Long userId) {
        return client.send(HttpMethod.GET, "/items", userId, null, Map.of());
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestParam String text,
                                         @Positive @RequestHeader("X-Sharer-User-Id") Long userId) {
        if (text == null || text.isBlank()) {
            return ResponseEntity.ok(List.of());
        }
        return client.send(HttpMethod.GET, "/items/search", userId, null, Map.of("text", text));
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> comment(@Positive @PathVariable Long itemId,
                                          @Positive @RequestHeader("X-Sharer-User-Id") Long userId,
                                          @Valid @RequestBody CommentDto dto) {
        return client.send(HttpMethod.POST, "/items/" + itemId + "/comment", userId, dto, Map.of());
    }
}
