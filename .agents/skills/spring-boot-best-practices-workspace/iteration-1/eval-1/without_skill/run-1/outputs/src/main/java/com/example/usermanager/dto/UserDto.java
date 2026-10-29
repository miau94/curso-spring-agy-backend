package com.example.usermanager.dto;

public record UserDto(
    Long id,
    String username,
    String email
) {}
