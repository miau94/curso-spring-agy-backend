package com.andres.course.agy.springboot.app.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record InvoiceItemDto(
        Long id,

        @NotBlank(message = "Item description is mandatory")
        @Size(max = 255, message = "Item description must not exceed 255 characters")
        String description,

        @NotNull(message = "Quantity is mandatory")
        @Min(value = 1, message = "Quantity must be at least 1")
        Integer quantity,

        @NotNull(message = "Unit price is mandatory")
        @DecimalMin(value = "0.00", message = "Unit price cannot be negative")
        BigDecimal unitPrice,

        BigDecimal subtotal
) {
}
