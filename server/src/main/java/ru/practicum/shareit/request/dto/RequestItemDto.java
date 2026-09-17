package ru.practicum.shareit.request.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestItemDto {
    private Long id;
    private String name;
    private Long ownerId;
}
