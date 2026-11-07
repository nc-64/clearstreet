package com.example.perk.client;

import com.example.perk.exception.ExternalApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ExternalClientTest {

    private ExternalClient externalClient;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        externalClient = new ExternalClient(builder, "https://jsonplaceholder.typicode.com", "test-key");
    }

    @Test
    @DisplayName("get(uri, Class) sends configured auth header and deserializes single object")
    void getSingleObject_success() {
        server.expect(requestTo("https://jsonplaceholder.typicode.com/posts/1"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-API-KEY", "test-key"))
                .andRespond(withSuccess("{\"id\": 1, \"title\": \"foo\"}", MediaType.APPLICATION_JSON));

        Map<String, Object> post = externalClient.get("/posts/1", Map.class);

        assertThat(post).containsEntry("id", 1).containsEntry("title", "foo");
        server.verify();
    }

    @Test
    @DisplayName("get(uri, ParameterizedTypeReference) deserializes lists")
    void getList_success() {
        server.expect(requestTo("https://jsonplaceholder.typicode.com/posts"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[{\"id\": 1}]", MediaType.APPLICATION_JSON));

        List<Map<String, Object>> posts = externalClient.get(
                "/posts",
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
        );

        assertThat(posts).hasSize(1);
        server.verify();
    }

    @Test
    @DisplayName("get(UriBuilder, ...) builds query parameters safely")
    void getWithQueryParams_success() {
        server.expect(requestTo("https://jsonplaceholder.typicode.com/users?name=Leanne%20Graham"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("name", "Leanne%20Graham"))
                .andRespond(withSuccess("[{\"id\": 1, \"name\": \"Leanne Graham\"}]", MediaType.APPLICATION_JSON));

        List<Map<String, Object>> users = externalClient.get(
                b -> b.path("/users").queryParam("name", "Leanne Graham").build(),
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
        );

        assertThat(users).hasSize(1);
        server.verify();
    }

    @Test
    @DisplayName("External 5xx error throws ExternalApiException mapped to BAD_GATEWAY")
    void external500_throwsExternalApiException() {
        server.expect(requestTo("https://jsonplaceholder.typicode.com/posts/999"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> externalClient.get("/posts/999", Map.class))
                .isInstanceOf(ExternalApiException.class)
                .satisfies(ex -> {
                    ExternalApiException apiEx = (ExternalApiException) ex;
                    assertThat(apiEx.getStatus()).isEqualTo(HttpStatus.BAD_GATEWAY);
                });

        server.verify();
    }
}
