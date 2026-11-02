package com.andres.course.agy.springboot.app.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerDto(
        Long id,

        @NotBlank(message = "First name is mandatory") @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters") String firstName,

        @NotBlank(message = "Last name is mandatory") @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters") String lastName,

        @NotBlank(message = "Email is mandatory") @Email(message = "Email should be valid") String email,

        String phone,

        String address) {
}
