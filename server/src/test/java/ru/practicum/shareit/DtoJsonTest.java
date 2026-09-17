package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.request.dto.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import java.time.LocalDateTime;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

@JsonTest
class DtoJsonTest {
    @Autowired private JacksonTester<ItemRequestDto> requests;
    @Autowired private JacksonTester<BookingDto> bookings;

    @Test
    void requestJsonIncludesIsoDateAndOwnerWithoutExposingUserEntity() throws Exception {
        ItemRequestDto dto = ItemRequestDto.builder().id(1L).description("Drill")
                .created(LocalDateTime.of(2030, 1, 2, 3, 4, 5))
                .items(List.of(RequestItemDto.builder().id(2L).name("Drill").ownerId(3L).build())).build();
        assertThat(requests.write(dto)).extractingJsonPathStringValue("$.created").isEqualTo("2030-01-02T03:04:05");
        assertThat(requests.write(dto)).extractingJsonPathNumberValue("$.items[0].ownerId").isEqualTo(3);
        assertThat(requests.write(dto)).doesNotHaveJsonPath("$.requestor");
        assertThat(requests.parseObject(requests.write(dto).getJson())).isEqualTo(dto);
    }

    @Test
    void bookingDeserializesIsoDates() throws Exception {
        BookingDto dto = bookings.parseObject("{\"itemId\":1,\"start\":\"2030-01-02T03:04:05\",\"end\":\"2030-01-03T03:04:05\"}");
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2030, 1, 2, 3, 4, 5));
        assertThat(dto.getEnd()).isAfter(dto.getStart());
    }
}
