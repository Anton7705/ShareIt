package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.test.web.client.MockRestServiceServer;
import ru.practicum.shareit.client.BaseClient;
import java.util.Map;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class BaseClientTest {
    private BaseClient client;
    private MockRestServiceServer server;

    @BeforeEach
    void setup() {
        RestTemplateBuilder builder = new RestTemplateBuilder().additionalCustomizers(rest ->
                server = MockRestServiceServer.bindTo(rest).build());
        client = new BaseClient(builder, "http://localhost:9090");
    }

    @Test
    void patchUsesUserHeaderAndPreservesStatus() {
        server.expect(requestTo("http://localhost:9090/items/1")).andExpect(method(HttpMethod.PATCH))
                .andExpect(header("X-Sharer-User-Id", "2"))
                .andExpect(content().json("{\"available\":false}"))
                .andRespond(withSuccess("{\"id\":1}", MediaType.APPLICATION_JSON));
        assertThat(client.send(HttpMethod.PATCH, "/items/1", 2L, Map.of("available", false), Map.of()).getStatusCode())
                .isEqualTo(HttpStatus.OK);
        server.verify();
    }

    @Test
    void errorsPreserveJsonAndStatus() {
        server.expect(requestTo("http://localhost:9090/users/1"))
                .andRespond(withStatus(HttpStatus.CONFLICT).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":\"duplicate\"}"));
        ResponseEntity<Object> response = client.send(HttpMethod.GET, "/users/1", null, null, Map.of());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(new String((byte[]) response.getBody(), java.nio.charset.StandardCharsets.UTF_8))
                .isEqualTo("{\"error\":\"duplicate\"}");
        server.verify();
    }

    @Test
    void searchEncodesReservedCharactersAndUnicode() {
        server.expect(requestTo("http://localhost:9090/items/search?text=A%2BB%20%26%20%D0%B4%D1%80%D0%B5%D0%BB%D1%8C"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));
        client.send(HttpMethod.GET, "/items/search", 1L, null, Map.of("text", "A+B & дрель"));
        server.verify();
    }

    @Test
    void deletePreservesNoContent() {
        server.expect(requestTo("http://localhost:9090/users/1")).andExpect(method(HttpMethod.DELETE))
                .andExpect(headerDoesNotExist("X-Sharer-User-Id")).andRespond(withNoContent());
        assertThat(client.send(HttpMethod.DELETE, "/users/1", null, null, Map.of()).getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);
        server.verify();
    }
}
