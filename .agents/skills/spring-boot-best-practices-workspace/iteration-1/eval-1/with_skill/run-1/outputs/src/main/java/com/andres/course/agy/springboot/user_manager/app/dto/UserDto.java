package com.andres.course.agy.springboot.user_manager.app.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserDto(
    Long id,
    @NotNull @Size(min = 3, max = 50) String username,
    @NotNull @Email String email
) {}
