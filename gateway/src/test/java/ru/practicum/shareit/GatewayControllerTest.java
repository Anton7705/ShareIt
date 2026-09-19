package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.*;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.client.BaseClient;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Stream;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
class GatewayControllerTest {
    @Autowired private MockMvc mvc;
    @MockBean private BaseClient client;
    private static final String HEADER = "X-Sharer-User-Id";

    @BeforeEach
    void setup() {
        when(client.send(any(), anyString(), nullable(Long.class), nullable(Object.class), anyMap()))
                .thenReturn(ResponseEntity.ok(Map.of("id", 12)));
    }

    static Stream<Arguments> routes() {
        String booking = "{\"itemId\":2,\"start\":\"" + LocalDateTime.now().plusDays(1)
                + "\",\"end\":\"" + LocalDateTime.now().plusDays(2) + "\"}";
        return Stream.of(
                Arguments.of("POST", "/users", "{\"name\":\"A\",\"email\":\"a@b.org\"}"),
                Arguments.of("PATCH", "/users/1", "{\"name\":\"B\"}"),
                Arguments.of("GET", "/users/1", ""),
                Arguments.of("DELETE", "/users/1", ""),
                Arguments.of("POST", "/items", "{\"name\":\"Drill\",\"description\":\"Tool\",\"available\":true}"),
                Arguments.of("PATCH", "/items/2", "{\"available\":false}"),
                Arguments.of("GET", "/items/2", ""),
                Arguments.of("GET", "/items", ""),
                Arguments.of("GET", "/items/search?text=drill", ""),
                Arguments.of("POST", "/items/2/comment", "{\"text\":\"Good\"}"),
                Arguments.of("POST", "/bookings", booking),
                Arguments.of("PATCH", "/bookings/2?approved=true", ""),
                Arguments.of("GET", "/bookings/2", ""),
                Arguments.of("GET", "/bookings", ""),
                Arguments.of("GET", "/bookings/owner?state=PAST", ""),
                Arguments.of("POST", "/requests", "{\"description\":\"Need a drill\"}"),
                Arguments.of("GET", "/requests", ""),
                Arguments.of("GET", "/requests/all?from=1&size=2", ""),
                Arguments.of("GET", "/requests/2", ""));
    }

    @ParameterizedTest
    @MethodSource("routes")
    void allRoutesForwardToServer(String method, String url, String body) throws Exception {
        mvc.perform(request(HttpMethod.valueOf(method), url).header(HEADER, 1)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(12));
        String path = url.split("\\?")[0];
        verify(client).send(eq(HttpMethod.valueOf(method)), eq(path),
                path.startsWith("/users") ? isNull() : eq(1L), nullable(Object.class), anyMap());
    }

    static Stream<Arguments> invalidRequests() {
        String future = LocalDateTime.now().plusDays(2).toString();
        String earlier = LocalDateTime.now().plusDays(1).toString();
        return Stream.of(
                Arguments.of("POST", "/users", "{}"),
                Arguments.of("POST", "/users", "{\"name\":\"A\",\"email\":\"bad\"}"),
                Arguments.of("PATCH", "/users/1", "{\"email\":\"bad\"}"),
                Arguments.of("PATCH", "/users/1", "{\"name\":\" \"}"),
                Arguments.of("POST", "/items", "{\"name\":\"Drill\"}"),
                Arguments.of("PATCH", "/items/2", "{\"description\":\" \"}"),
                Arguments.of("POST", "/items/2/comment", "{\"text\":\" \"}"),
                Arguments.of("POST", "/requests", "{\"description\":\" \"}"),
                Arguments.of("POST", "/requests", "{}"),
                Arguments.of("POST", "/requests", "{"),
                Arguments.of("GET", "/requests/all?from=-1", ""),
                Arguments.of("GET", "/requests/all?size=0", ""),
                Arguments.of("GET", "/requests/0", ""),
                Arguments.of("GET", "/bookings?state=UNKNOWN", ""),
                Arguments.of("PATCH", "/bookings/1", ""),
                Arguments.of("PATCH", "/bookings/1?approved=invalid", ""),
                Arguments.of("POST", "/bookings", "{\"itemId\":1}"),
                Arguments.of("POST", "/bookings", "{\"itemId\":1,\"start\":\"" + future + "\",\"end\":\"" + future + "\"}"),
                Arguments.of("POST", "/bookings", "{\"itemId\":1,\"start\":\"" + future + "\",\"end\":\"" + earlier + "\"}"),
                Arguments.of("POST", "/bookings", "{\"itemId\":1,\"start\":\"2000-01-01T00:00:00\",\"end\":\"" + future + "\"}"));
    }

    @ParameterizedTest
    @MethodSource("invalidRequests")
    void invalidInputNeverReachesServer(String method, String url, String body) throws Exception {
        mvc.perform(request(HttpMethod.valueOf(method), url).header(HEADER, 1)
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isBadRequest());
        verifyNoInteractions(client);
    }

    @Test
    void missingAndInvalidHeaderDoNotReachServer() throws Exception {
        mvc.perform(get("/requests")).andExpect(status().isBadRequest());
        mvc.perform(get("/requests").header(HEADER, -1)).andExpect(status().isBadRequest());
        mvc.perform(get("/requests").header(HEADER, "abc")).andExpect(status().isBadRequest());
        verifyNoInteractions(client);
    }

    @Test
    void queryParametersAreForwarded() throws Exception {
        mvc.perform(get("/requests/all").header(HEADER, 1).param("from", "1").param("size", "2"))
                .andExpect(status().isOk());
        verify(client).send(HttpMethod.GET, "/requests/all", 1L, null, Map.of("from", 1, "size", 2));
    }

    @Test
    void upstreamUnavailableReturnsBadGateway() throws Exception {
        when(client.send(any(), anyString(), anyLong(), any(), anyMap()))
                .thenThrow(new org.springframework.web.client.ResourceAccessException("offline"));
        mvc.perform(get("/requests").header(HEADER, 1)).andExpect(status().isBadGateway());
    }
}
