package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class CommentDto {
    private Long id;
    @NotBlank(message = "Содержание не может быть пустым")
    @Size(max = 1000)
    private String text;
    private String authorName;
    private LocalDateTime created;
}
