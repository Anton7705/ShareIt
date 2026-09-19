package ru.practicum.shareit.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class BaseClient {
    private final RestTemplate rest;
    private final String serverUrl;

    public BaseClient(RestTemplateBuilder builder, @Value("${shareit-server.url}") String serverUrl) {
        this.serverUrl = serverUrl;
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(10));
        rest = builder.requestFactory(() -> factory).build();
    }

    public ResponseEntity<Object> send(HttpMethod method, String path, Long userId,
                                        Object body, Map<String, ?> parameters) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        if (userId != null) {
            headers.set("X-Sharer-User-Id", userId.toString());
        }
        UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(serverUrl).path(path);

        parameters.forEach((key, value) -> uri.queryParam(key, "{" + key + "}"));
        URI target = uri.encode().buildAndExpand(parameters).toUri();
        try {
            ResponseEntity<Object> response = rest.exchange(target, method, new HttpEntity<>(body, headers), Object.class);
            return ResponseEntity.status(response.getStatusCode()).contentType(MediaType.APPLICATION_JSON)
                    .body(response.getBody());
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).contentType(MediaType.APPLICATION_JSON)
                    .body(e.getResponseBodyAsByteArray());
        }
    }
}
