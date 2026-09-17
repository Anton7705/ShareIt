package ru.practicum.shareit;

import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.booking.dto.BookingDto;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.*;

@JsonTest
class DtoJsonTest {
    @Autowired private JacksonTester<BookingDto> json;

    @Test
    void datesUseIsoFormatAndValidationPropertyIsNotSerialized() throws Exception {
        BookingDto dto = BookingDto.builder().itemId(1L).start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2)).build();
        assertThat(json.write(dto)).doesNotHaveJsonPath("$.endAfterStart");
        assertThat(json.parseObject(json.write(dto).getJson())).isEqualTo(dto);
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            assertThat(factory.getValidator().validate(dto)).isEmpty();
            dto.setEnd(dto.getStart());
            assertThat(factory.getValidator().validate(dto)).extracting(v -> v.getPropertyPath().toString())
                    .contains("endAfterStart");
        }
    }
}
