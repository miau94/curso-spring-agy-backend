package com.andres.course.agy.springboot.app.dto;

import com.andres.course.agy.springboot.app.models.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserDto(
        Long id,

        @NotBlank(message = "Email is mandatory")
        @Email(message = "Email should be valid")
        String email,

        Role role) {
}
