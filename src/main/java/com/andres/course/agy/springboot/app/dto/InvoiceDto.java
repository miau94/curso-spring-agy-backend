package com.andres.course.agy.springboot.app.dto;

import com.andres.course.agy.springboot.app.models.InvoiceStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record InvoiceDto(
        Long id,

        @NotNull(message = "Customer ID is mandatory")
        Long customerId,

        String customerName,

        @NotBlank(message = "Description is mandatory")
        @Size(max = 255, message = "Description must not exceed 255 characters")
        String description,

        @DecimalMin(value = "0.00", message = "Amount cannot be negative")
        BigDecimal amount,

        InvoiceStatus status,

        List<@Valid InvoiceItemDto> items
) {
}
