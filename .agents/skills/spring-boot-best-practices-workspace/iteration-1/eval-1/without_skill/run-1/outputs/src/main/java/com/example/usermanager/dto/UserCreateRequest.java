package com.example.usermanager.dto;

public record UserCreateRequest(
    String username,
    String email,
    String password
) {}
