package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import ru.practicum.shareit.validation.Create;
import ru.practicum.shareit.validation.Update;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@Data
@AllArgsConstructor
public class UserDto {
    private Long id;
    @NotBlank(groups = Create.class, message = "Имя не может быть пустым")
    @Pattern(regexp = "(?s).*\\S.*", groups = Update.class, message = "Поле не должно быть пустым")
    @Size(max = 255, groups = {Create.class, Update.class})
    private String name;
    @NotBlank(groups = Create.class, message = "Email не может быть пустым")
    @Email(groups = {Create.class, Update.class}, message = "Некорректный формат email")
    @Pattern(regexp = "(?s).*\\S.*", groups = Update.class, message = "Поле не должно быть пустым")
    @Size(max = 512, groups = {Create.class, Update.class})
    private String email;
}
