package com.andres.course.agy.springboot.bookstore.app.mappers;

import com.andres.course.agy.springboot.bookstore.app.dto.BookDto;
import com.andres.course.agy.springboot.bookstore.app.models.Book;
import org.springframework.stereotype.Component;

@Component
public class BookMapper {

    public BookDto entityToDto(Book book) {
        if (book == null) {
            return null;
        }
        return new BookDto(
            book.getId(),
            book.getTitle(),
            book.getAuthor(),
            book.getIsbn(),
            book.getPrice(),
            book.getDescription()
        );
    }

    public Book dtoToEntity(BookDto dto) {
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
        return book;
    }
}
