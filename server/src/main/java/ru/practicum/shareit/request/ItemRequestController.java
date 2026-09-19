package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import java.util.List;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private final ItemRequestService requestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemRequestDto create(@RequestBody ItemRequestDto dto,
                                 @RequestHeader("X-Sharer-User-Id") Long userId) {
        return requestService.create(dto, userId);
    }

    @GetMapping
    public List<ItemRequestDto> findOwn(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return requestService.findOwn(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> findOthers(@RequestHeader("X-Sharer-User-Id") Long userId,
                                           @RequestParam(defaultValue = "0") int from,
                                           @RequestParam(defaultValue = "2147483647") int size) {
        return requestService.findOthers(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto findOne(@PathVariable Long requestId,
                                  @RequestHeader("X-Sharer-User-Id") Long userId) {
        return requestService.findOne(requestId, userId);
    }
}
