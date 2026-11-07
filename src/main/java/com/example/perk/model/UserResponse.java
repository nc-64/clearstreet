package com.example.perk.model;

public record UserResponse(
        Long id,
        String name,
        String username,
        String email
) {
}
