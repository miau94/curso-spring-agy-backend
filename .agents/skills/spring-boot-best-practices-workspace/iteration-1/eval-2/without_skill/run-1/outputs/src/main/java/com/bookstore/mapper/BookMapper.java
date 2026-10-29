package com.bookstore.mapper;

import com.bookstore.dto.BookDto;
import com.bookstore.model.Book;
import org.springframework.stereotype.Component;

@Component
public class BookMapper {

    public BookDto toDto(Book book) {
        if (book == null) {
            return null;
        }
        return new BookDto(
            book.getId(),
            book.getTitle(),
            book.getAuthor(),
            book.getIsbn(),
            book.getPrice(),
            book.getDescription(),
            book.getPublishYear()
        );
    }

    public Book toEntity(BookDto dto) {
        if (dto == null) {
            return null;
        }
        Book book = new Book();
        book.setId(dto.id());
        book.setTitle(dto.title());
        book.setAuthor(dto.author());
        book.setIsbn(dto.isbn());
        book.setPrice(dto.price());
        book.setDescription(dto.description());
        book.setPublishYear(dto.publishYear());
        // Note: Sensitive fields costPrice, supplierContact, internalNotes are NOT mapped from the DTO.
        return book;
    }
}
