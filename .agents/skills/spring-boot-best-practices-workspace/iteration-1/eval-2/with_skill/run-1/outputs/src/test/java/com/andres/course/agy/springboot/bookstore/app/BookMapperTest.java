package com.andres.course.agy.springboot.bookstore.app;

import com.andres.course.agy.springboot.bookstore.app.dto.BookDto;
import com.andres.course.agy.springboot.bookstore.app.mappers.BookMapper;
import com.andres.course.agy.springboot.bookstore.app.models.Book;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BookMapperTest {

    private final BookMapper bookMapper = new BookMapper();

    @Test
    void testEntityToDto() {
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Title");
        book.setAuthor("Author");
        book.setIsbn("12345");
        book.setPrice(10.00);
        book.setDescription("Desc");
        book.setPassword("sensitive_pwd");
        book.setCreatedAt(LocalDateTime.now());
        book.setUpdatedAt(LocalDateTime.now());

        BookDto dto = bookMapper.entityToDto(book);

        assertNotNull(dto);
        assertEquals(book.getId(), dto.id());
        assertEquals(book.getTitle(), dto.title());
        assertEquals(book.getAuthor(), dto.author());
        assertEquals(book.getIsbn(), dto.isbn());
        assertEquals(book.getPrice(), dto.price());
        assertEquals(book.getDescription(), dto.description());
        // Verify sensitive fields are not present (they are not fields of BookDto at all)
        // Tested by checking compilation and structure.
    }

    @Test
    void testDtoToEntity() {
        BookDto dto = new BookDto(1L, "Title", "Author", "12345", 10.00, "Desc");

        Book book = bookMapper.dtoToEntity(dto);

        assertNotNull(book);
        assertEquals(dto.id(), book.getId());
        assertEquals(dto.title(), book.getTitle());
        assertEquals(dto.author(), book.getAuthor());
        assertEquals(dto.isbn(), book.getIsbn());
        assertEquals(dto.price(), book.getPrice());
        assertEquals(dto.description(), book.getDescription());
        assertNull(book.getPassword());
        assertNull(book.getCreatedAt());
        assertNull(book.getUpdatedAt());
    }
}
