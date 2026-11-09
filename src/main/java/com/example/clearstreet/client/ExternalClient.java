
package com.example.clearstreet.client;

import com.example.clearstreet.exception.ExternalApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
@Component
public class ExternalClient {

    private final RestClient restClient;

    public ExternalClient(
            RestClient.Builder restClientBuilder,
            @Value("${external-api.base-url}") String baseUrl,
            @Value("${external-api.api-key:}") String apiKey) {

        RestClient.Builder builder = restClientBuilder
                .baseUrl(baseUrl)
                .defaultStatusHandler(
                        HttpStatusCode::isError,
                        (request, response) -> {
                            String message = String.format(
                                    "External API [%s %s] failed with status: %s",
                                    request.getMethod(),
                                    request.getURI(),
                                    response.getStatusCode()
                            );

                            log.error(message);

                            throw new ExternalApiException(
                                    message,
                                    HttpStatus.BAD_GATEWAY
                            );
                        }
                );

        // Add API key only when one is configured
        if (apiKey != null && !apiKey.isBlank()) {
            builder.defaultHeader("X-API-KEY", apiKey);
        }

        this.restClient = builder.build();
    }

    /**
     * GET request returning a single object.
     */
    public <T> T get(String uri, Class<T> responseType) {
        return execute(() -> restClient.get()
                .uri(uri)
                .retrieve()
                .body(responseType));
    }

    /**
     * GET request returning a generic type, e.g. List<T>.
     */
    public <T> T get(
            String uri,
            ParameterizedTypeReference<T> responseType) {

        return execute(() -> restClient.get()
                .uri(uri)
                .retrieve()
                .body(responseType));
    }

    /**
     * GET request with query params via UriBuilder, returning a generic type (e.g. List<T>).
     */
    public <T> T get(
            Function<UriBuilder, URI> uriFunction,
            ParameterizedTypeReference<T> responseType) {

        return execute(() -> restClient.get()
                .uri(uriFunction)
                .retrieve()
                .body(responseType));
    }

    /**
     * GET request with query params via UriBuilder, returning a single object.
     */
    public <T> T get(
            Function<UriBuilder, URI> uriFunction,
            Class<T> responseType) {

        return execute(() -> restClient.get()
                .uri(uriFunction)
                .retrieve()
                .body(responseType));
    }

    /**
     * POST request with a body and typed response.
     */
    public <T, R> R post(
            String uri,
            T requestBody,
            Class<R> responseType) {

        return execute(() -> restClient.post()
                .uri(uri)
                .body(requestBody)
                .retrieve()
                .body(responseType));
    }

    /**
     * Shared HTTP execution and exception handling.
     */
    private <T> T execute(Supplier<T> clientCall) {
        try {
            return clientCall.get();

        } catch (ExternalApiException e) {
            throw e;

        } catch (ResourceAccessException e) {
            log.error("Unable to reach external API", e);

            throw new ExternalApiException(
                    "Unable to connect to external API",
                    HttpStatus.BAD_GATEWAY,
                    e
            );

        } catch (RestClientException e) {
            log.error("External API communication failure", e);

            throw new ExternalApiException(
                    "Error communicating with external API",
                    HttpStatus.BAD_GATEWAY,
                    e
            );
        }
    }
}
