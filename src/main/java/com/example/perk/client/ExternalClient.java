package com.example.perk.client;

import com.example.perk.model.UserResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Component
public class ExternalClient {

    private final RestClient restClient;

    public ExternalClient(
        RestClient.Builder restClientBuilder,
        @Value("${external-api.base-url}") String url,
        @Value("${external-api.api-key}") String apiKey) {
        this.restClient = restClientBuilder
                .baseUrl(url)
                .defaultHeader("X-API-KEY", apiKey)
                .build();
    }


    public List<UserResponse> getUsers(){
        return restClient
                .get()
                .uri("/users")
                .retrieve()
                .body(new ParameterizedTypeReference<List<UserResponse>>() {});
    }

}
