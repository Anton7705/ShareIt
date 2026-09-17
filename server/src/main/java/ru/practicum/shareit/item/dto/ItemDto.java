package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@NoArgsConstructor
@Data
public class ItemDto {
    Long id;
    private String name;
    private String description;
    private Boolean available;
    private Long requestId;
}
