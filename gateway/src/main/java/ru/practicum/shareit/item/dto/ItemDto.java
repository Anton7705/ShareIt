package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.validation.Create;
import ru.practicum.shareit.validation.Update;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@NoArgsConstructor
@Data
public class ItemDto {
    Long id;
    @NotBlank(groups = Create.class, message = "Имя не может быть пустым")
    @Pattern(regexp = "(?s).*\\S.*", groups = Update.class, message = "Поле не должно быть пустым")
    @Size(max = 255, groups = {Create.class, Update.class})
    private String name;
    @NotBlank(groups = Create.class, message = "Описание не может быть пустым")
    @Pattern(regexp = "(?s).*\\S.*", groups = Update.class, message = "Поле не должно быть пустым")
    private String description;
    @NotNull(groups = Create.class, message = "Доступность не может быть пустой")
    private Boolean available;
    @Positive(groups = {Create.class, Update.class})
    private Long requestId;
}
