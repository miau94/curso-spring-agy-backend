package com.andres.course.agy.springboot.bookstore.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BookDto(
    Long id,

    @NotBlank(message = "Title is required")
    @Size(max = 100)
    String title,

    @NotBlank(message = "Author is required")
    @Size(max = 100)
    String author,

    @NotBlank(message = "ISBN is required")
    @Size(max = 20)
    String isbn,

    @NotNull(message = "Price is required")
    Double price,

    @Size(max = 500)
    String description
) {
}
