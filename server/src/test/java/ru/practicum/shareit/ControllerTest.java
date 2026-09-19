package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.exception.*;
import java.util.NoSuchElementException;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
class ControllerTest {
    @Autowired private MockMvc mvc;
    @MockBean private UserService users;
    @MockBean private ItemService items;
    @MockBean private BookingService bookings;
    @MockBean private ItemRequestService requests;
    private static final String HEADER = "X-Sharer-User-Id";

    @Test
    void usersRoutes() throws Exception {
        mvc.perform(post("/users").contentType("application/json").content("{\"name\":\"A\",\"email\":\"a@b.org\"}"))
                .andExpect(status().isCreated());
        verify(users).createUser(argThat(dto -> dto.getName().equals("A")));
        mvc.perform(patch("/users/1").contentType("application/json").content("{\"name\":\"B\"}"))
                .andExpect(status().isOk());
        verify(users).updateUser(argThat(dto -> dto.getName().equals("B")), eq(1L));
        mvc.perform(get("/users/1")).andExpect(status().isOk());
        verify(users).findUser(1L);
        mvc.perform(delete("/users/1")).andExpect(status().isNoContent());
        verify(users).deleteUser(1L);
    }

    @Test
    void itemRoutes() throws Exception {
        mvc.perform(post("/items").header(HEADER, 1).contentType("application/json").content("{\"name\":\"Drill\"}"))
                .andExpect(status().isCreated());
        verify(items).createItem(argThat(dto -> dto.getName().equals("Drill")), eq(1L));
        mvc.perform(patch("/items/2").header(HEADER, 1).contentType("application/json").content("{\"available\":false}"))
                .andExpect(status().isOk());
        verify(items).updateItem(eq(2L), argThat(dto -> Boolean.FALSE.equals(dto.getAvailable())), eq(1L));
        mvc.perform(get("/items/2").header(HEADER, 1)).andExpect(status().isOk());
        verify(items).findItem(2L, 1L);
        mvc.perform(get("/items").header(HEADER, 1)).andExpect(status().isOk());
        verify(items).findItemsByOwner(1L);
        mvc.perform(get("/items/search").param("text", "drill")).andExpect(status().isOk());
        verify(items).searchItems("drill");
        mvc.perform(post("/items/2/comment").header(HEADER, 1).contentType("application/json").content("{\"text\":\"Good\"}"))
                .andExpect(status().isOk());
        verify(items).addComment(eq(2L), eq(1L), argThat(dto -> dto.getText().equals("Good")));
    }

    @Test
    void bookingRoutes() throws Exception {
        mvc.perform(post("/bookings").header(HEADER, 1).contentType("application/json").content("{\"itemId\":2}"))
                .andExpect(status().isCreated());
        verify(bookings).createBooking(argThat(dto -> dto.getItemId().equals(2L)), eq(1L));
        mvc.perform(patch("/bookings/2").header(HEADER, 1).param("approved", "true")).andExpect(status().isOk());
        verify(bookings).updateStatus(1L, 2L, true);
        mvc.perform(get("/bookings/2").header(HEADER, 1)).andExpect(status().isOk());
        verify(bookings).findBooking(1L, 2L);
        mvc.perform(get("/bookings").header(HEADER, 1)).andExpect(status().isOk());
        verify(bookings).getUserBookings(1L, BookingState.ALL);
        mvc.perform(get("/bookings/owner").header(HEADER, 1).param("state", "PAST")).andExpect(status().isOk());
        verify(bookings).getOwnerBookings(1L, BookingState.PAST);
    }

    @Test
    void requestRoutes() throws Exception {
        mvc.perform(post("/requests").header(HEADER, 1).contentType("application/json").content("{\"description\":\"Drill\"}"))
                .andExpect(status().isCreated());
        verify(requests).create(argThat(dto -> dto.getDescription().equals("Drill")), eq(1L));
        mvc.perform(get("/requests").header(HEADER, 1)).andExpect(status().isOk());
        verify(requests).findOwn(1L);
        mvc.perform(get("/requests/all").header(HEADER, 1).param("from", "1").param("size", "2"))
                .andExpect(status().isOk());
        verify(requests).findOthers(1L, 1, 2);
        mvc.perform(get("/requests/2").header(HEADER, 1)).andExpect(status().isOk());
        verify(requests).findOne(2L, 1L);
    }

    @Test
    void errorsHaveExpectedStatus() throws Exception {
        when(users.findUser(1L)).thenThrow(new NoSuchElementException("missing"));
        mvc.perform(get("/users/1")).andExpect(status().isNotFound()).andExpect(jsonPath("$.error").exists());
        when(users.findUser(2L)).thenThrow(new ConflictException("duplicate"));
        mvc.perform(get("/users/2")).andExpect(status().isConflict());
        when(users.findUser(3L)).thenThrow(new AccessDeniedException("denied"));
        mvc.perform(get("/users/3")).andExpect(status().isForbidden());
        when(users.findUser(4L)).thenThrow(new IllegalArgumentException("bad"));
        mvc.perform(get("/users/4")).andExpect(status().isBadRequest());
        mvc.perform(get("/requests")).andExpect(status().isBadRequest());
    }
}
