package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestServiceTest {
    @Mock private ItemRequestRepository requests;
    @Mock private ItemRepository items;
    @Mock private UserService users;
    @InjectMocks private ItemRequestServiceImpl service;

    @Test
    void createIgnoresClientIdentityAndTimestamp() {
        User user = User.builder().id(1L).name("A").email("a@b.org").build();
        when(users.getUser(1L)).thenReturn(user);
        when(requests.save(any())).thenAnswer(invocation -> {
            ItemRequest request = invocation.getArgument(0);
            assertThat(request.getId()).isNull();
            assertThat(request.getRequestor()).isSameAs(user);
            assertThat(request.getCreated()).isAfter(LocalDateTime.of(2020, 1, 1, 0, 0));
            request.setId(2L);
            return request;
        });
        ItemRequestDto result = service.create(ItemRequestDto.builder().id(999L).description("Drill")
                .created(LocalDateTime.of(2000, 1, 1, 0, 0)).build(), 1L);
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getItems()).isEmpty();
        verifyNoInteractions(items);
    }

    @Test
    void emptyRequestListDoesNotQueryItems() {
        when(requests.findByRequestorIdOrderByCreatedDescIdDesc(1L)).thenReturn(List.of());
        assertThat(service.findOwn(1L)).isEmpty();
        verify(users).getUser(1L);
        verifyNoInteractions(items);
    }

    @Test
    void missingUserIsCheckedBeforeSaving() {
        when(users.getUser(1L)).thenThrow(new NoSuchElementException("missing"));
        assertThatThrownBy(() -> service.create(ItemRequestDto.builder().description("Drill").build(), 1L))
                .isInstanceOf(NoSuchElementException.class);
        verifyNoInteractions(requests, items);
    }
}
