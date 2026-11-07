package com.example.perk.service;

import com.example.perk.client.ExternalClient;
import com.example.perk.model.UserResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    public final ExternalClient externalClient;

    public UserService(ExternalClient externalClient) {
        this.externalClient = externalClient;
    }

    public List<UserResponse> getUsers () {
        return externalClient.getUsers();
    }

}
