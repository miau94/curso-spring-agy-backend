package com.andres.course.agy.springboot.bookstore.app.services;

import com.andres.course.agy.springboot.bookstore.app.dto.BookDto;
import com.andres.course.agy.springboot.bookstore.app.mappers.BookMapper;
import com.andres.course.agy.springboot.bookstore.app.models.Book;
import com.andres.course.agy.springboot.bookstore.app.repositories.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    public BookService(BookRepository bookRepository, BookMapper bookMapper) {
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
    }

    @Transactional(readOnly = true)
    public List<BookDto> findAll() {
        return bookRepository.findAll().stream()
                .map(bookMapper::entityToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<BookDto> findById(Long id) {
        return bookRepository.findById(id)
                .map(bookMapper::entityToDto);
    }

    public BookDto save(BookDto bookDto) {
        Book book = bookMapper.dtoToEntity(bookDto);
        if (book.getId() == null) {
            book.setPassword("sensitive_password_123");
        }
        Book savedBook = bookRepository.save(book);
        return bookMapper.entityToDto(savedBook);
    }

    public void deleteById(Long id) {
        bookRepository.deleteById(id);
    }
}
