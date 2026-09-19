package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.dto.*;
import ru.practicum.shareit.booking.model.*;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class ServiceIntegrationTest {
    @Autowired private UserService users;
    @Autowired private ItemService items;
    @Autowired private ItemRequestService requests;
    @Autowired private BookingService bookings;
    @Autowired private BookingRepository bookingRepository;
    private Long owner;
    private Long booker;
    private Long stranger;
    private Long itemId;

    @BeforeEach
    void setup() {
        owner = users.createUser(UserDto.builder().name("Owner").email("owner@test.org").build()).getId();
        booker = users.createUser(UserDto.builder().name("Booker").email("booker@test.org").build()).getId();
        stranger = users.createUser(UserDto.builder().name("Stranger").email("stranger@test.org").build()).getId();
        itemId = items.createItem(ItemDto.builder().name("Drill").description("Electric TOOL")
                .available(true).build(), owner).getId();
    }

    @Test
    void usersCreateReadUpdateDeleteAndEmailConflicts() {
        assertThat(users.findUser(owner).getName()).isEqualTo("Owner");
        assertThat(users.updateUser(UserDto.builder().name("New name").build(), owner).getEmail())
                .isEqualTo("owner@test.org");
        assertThat(users.updateUser(UserDto.builder().email("new@test.org").build(), owner).getName())
                .isEqualTo("New name");
        assertThatThrownBy(() -> users.createUser(UserDto.builder().name("Duplicate").email("new@test.org").build()))
                .isInstanceOf(ConflictException.class);
        assertThatThrownBy(() -> users.updateUser(UserDto.builder().email("booker@test.org").build(), owner))
                .isInstanceOf(ConflictException.class);
        users.deleteUser(stranger);
        assertThatThrownBy(() -> users.findUser(stranger)).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void requestsHaveOrderedAnswersAndAreVisibleToOtherUsers() {
        ItemRequestDto first = requests.create(ItemRequestDto.builder().description("Need a drill").build(), booker);
        ItemRequestDto second = requests.create(ItemRequestDto.builder().description("Need a saw").build(), booker);
        requests.create(ItemRequestDto.builder().description("Own request").build(), owner);
        ItemDto answer = items.createItem(ItemDto.builder().name("Answer").description("Available drill")
                .available(true).requestId(first.getId()).build(), owner);
        assertThat(answer.getRequestId()).isEqualTo(first.getId());
        assertThat(requests.findOwn(booker)).extracting(ItemRequestDto::getId)
                .containsExactly(second.getId(), first.getId());
        assertThat(requests.findOthers(owner, 0, 10)).extracting(ItemRequestDto::getId)
                .containsExactly(second.getId(), first.getId());
        assertThat(requests.findOthers(owner, 1, 1)).extracting(ItemRequestDto::getId).containsExactly(first.getId());
        assertThat(requests.findOthers(owner, 99, 1)).isEmpty();
        assertThat(requests.findOne(first.getId(), stranger).getItems()).singleElement().satisfies(item -> {
            assertThat(item.getId()).isEqualTo(answer.getId());
            assertThat(item.getName()).isEqualTo("Answer");
            assertThat(item.getOwnerId()).isEqualTo(owner);
        });
        assertThat(requests.findOne(second.getId(), owner).getItems()).isEmpty();
        assertThat(requests.findOwn(stranger)).isEmpty();
        assertThatThrownBy(() -> requests.findOne(-1L, owner)).isInstanceOf(NoSuchElementException.class);
        assertThatThrownBy(() -> requests.findOwn(-1L)).isInstanceOf(NoSuchElementException.class);
        assertThatThrownBy(() -> requests.findOthers(-1L, 0, 10)).isInstanceOf(NoSuchElementException.class);
        assertThatThrownBy(() -> requests.create(first, -1L)).isInstanceOf(NoSuchElementException.class);
        assertThatThrownBy(() -> items.createItem(ItemDto.builder().name("Bad").description("Bad")
                .available(true).requestId(-1L).build(), owner)).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void itemsSupportPartialUpdatesSearchAndOwnership() {
        assertThat(items.findItem(itemId, owner).getRequestId()).isNull();
        assertThat(items.searchItems("tOoL")).extracting(ItemDto::getId).containsExactly(itemId);
        assertThatThrownBy(() -> items.updateItem(itemId, ItemDto.builder().name("Stolen").build(), booker))
                .isInstanceOf(AccessDeniedException.class);
        assertThat(items.updateItem(itemId, ItemDto.builder().name("Saw").build(), owner).getDescription())
                .isEqualTo("Electric TOOL");
        assertThat(items.updateItem(itemId, ItemDto.builder().description("Hand tool").available(false).build(), owner)
                .getAvailable()).isFalse();
        assertThat(items.searchItems("tool")).isEmpty();
        assertThat(items.findItemsByOwner(owner)).extracting(ItemResponseDto::getId).containsExactly(itemId);
        assertThat(items.findItemsByOwner(booker)).isEmpty();
        assertThatThrownBy(() -> items.findItem(-1L, owner)).isInstanceOf(NoSuchElementException.class);
    }

    private BookingResponseDto book() {
        return bookings.createBooking(BookingDto.builder().itemId(itemId).start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2)).status(BookingStatus.APPROVED).id(999L).build(), booker);
    }

    @Test
    void bookingLifecycleRejectsUnauthorizedAndRepeatedApproval() {
        BookingResponseDto created = book();
        assertThat(created.getId()).isNotEqualTo(999L);
        assertThat(created.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(bookings.findBooking(booker, created.getId()).getItem().getId()).isEqualTo(itemId);
        assertThat(bookings.findBooking(owner, created.getId()).getBooker().getId()).isEqualTo(booker);
        assertThatThrownBy(() -> bookings.findBooking(stranger, created.getId())).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> bookings.updateStatus(booker, created.getId(), true)).isInstanceOf(AccessDeniedException.class);
        assertThat(bookings.updateStatus(owner, created.getId(), true).getStatus()).isEqualTo(BookingStatus.APPROVED);
        assertThatThrownBy(() -> bookings.updateStatus(owner, created.getId(), false)).isInstanceOf(IllegalArgumentException.class);
        BookingResponseDto rejected = book();
        assertThat(bookings.updateStatus(owner, rejected.getId(), false).getStatus()).isEqualTo(BookingStatus.REJECTED);
        assertThatThrownBy(() -> bookings.findBooking(owner, -1L)).isInstanceOf(NoSuchElementException.class);
        BookingDto dto = BookingDto.builder().itemId(itemId).start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2)).build();
        assertThatThrownBy(() -> bookings.createBooking(dto, owner)).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> bookings.createBooking(dto, -1L)).isInstanceOf(NoSuchElementException.class);
        items.updateItem(itemId, ItemDto.builder().available(false).build(), owner);
        assertThatThrownBy(() -> bookings.createBooking(dto, booker)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> bookings.createBooking(dto, -1L)).isInstanceOf(NoSuchElementException.class);
    }

    @ParameterizedTest
    @EnumSource(BookingState.class)
    void bookingFiltersWorkForBookerAndOwner(BookingState state) {
        BookingResponseDto response = book();
        Booking booking = bookingRepository.findById(response.getId()).orElseThrow();
        LocalDateTime now = LocalDateTime.now();
        if (state == BookingState.PAST) {
            booking.setStart(now.minusDays(2));
            booking.setEnd(now.minusDays(1));
        } else if (state == BookingState.CURRENT) {
            booking.setStart(now.minusDays(1));
            booking.setEnd(now.plusDays(1));
        } else if (state == BookingState.REJECTED) {
            booking.setStatus(BookingStatus.REJECTED);
        }
        bookingRepository.saveAndFlush(booking);
        assertThat(bookings.getUserBookings(booker, state)).extracting(BookingResponseDto::getId)
                .containsExactly(response.getId());
        assertThat(bookings.getOwnerBookings(owner, state)).extracting(BookingResponseDto::getId)
                .containsExactly(response.getId());
        assertThat(bookings.getUserBookings(stranger, state)).isEmpty();
    }

    @Test
    void commentsRequireCompletedApprovedBookingAndEnrichmentIsOwnerOnly() {
        CommentDto text = CommentDto.builder().text("Great drill").build();
        assertThatThrownBy(() -> items.addComment(itemId, booker, text)).isInstanceOf(IllegalArgumentException.class);
        BookingResponseDto past = book();
        bookings.updateStatus(owner, past.getId(), true);
        assertThatThrownBy(() -> items.addComment(itemId, booker, text)).isInstanceOf(IllegalArgumentException.class);
        Booking stored = bookingRepository.findById(past.getId()).orElseThrow();
        stored.setStart(LocalDateTime.now().minusDays(2));
        stored.setEnd(LocalDateTime.now().minusDays(1));
        bookingRepository.saveAndFlush(stored);
        CommentDto comment = items.addComment(itemId, booker, text);
        assertThat(comment.getAuthorName()).isEqualTo("Booker");
        assertThat(comment.getCreated()).isNotNull();
        BookingResponseDto next = book();
        bookings.updateStatus(owner, next.getId(), true);
        BookingResponseDto waiting = book();
        assertThat(waiting.getStatus()).isEqualTo(BookingStatus.WAITING);
        ItemResponseDto item = items.findItem(itemId, owner);
        assertThat(item.getLastBooking().getId()).isEqualTo(past.getId());
        assertThat(item.getNextBooking().getId()).isEqualTo(next.getId());
        assertThat(item.getComments()).extracting(CommentDto::getText).containsExactly("Great drill");
        assertThat(items.findItemsByOwner(owner).getFirst()).isEqualTo(item);
        assertThat(items.findItem(itemId, booker).getLastBooking()).isNull();
        assertThat(items.findItem(itemId, stranger).getNextBooking()).isNull();
        assertThat(items.findItem(itemId, stranger).getComments()).hasSize(1);
    }
}
