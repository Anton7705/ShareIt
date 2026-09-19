package ru.practicum.shareit.user;

import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.validation.Create;
import ru.practicum.shareit.validation.Update;
import java.util.Map;
import ru.practicum.shareit.user.dto.UserDto;

@RestController
@RequestMapping("/users")
@Validated
@RequiredArgsConstructor
public class UserController {
    private final BaseClient client;

    @PostMapping
    public ResponseEntity<Object> create(@Validated(Create.class) @RequestBody UserDto dto) {
        return client.send(HttpMethod.POST, "/users", null, dto, Map.of());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> update(@Positive @PathVariable Long id,
                                         @Validated(Update.class) @RequestBody UserDto dto) {
        return client.send(HttpMethod.PATCH, "/users/" + id, null, dto, Map.of());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> find(@Positive @PathVariable Long id) {
        return client.send(HttpMethod.GET, "/users/" + id, null, null, Map.of());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@Positive @PathVariable Long id) {
        return client.send(HttpMethod.DELETE, "/users/" + id, null, null, Map.of());
    }
}
