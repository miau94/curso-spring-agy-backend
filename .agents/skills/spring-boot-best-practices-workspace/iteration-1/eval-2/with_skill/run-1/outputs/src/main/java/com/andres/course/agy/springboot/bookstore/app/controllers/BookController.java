package com.andres.course.agy.springboot.bookstore.app.controllers;

import com.andres.course.agy.springboot.bookstore.app.dto.BookDto;
import com.andres.course.agy.springboot.bookstore.app.services.BookService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping({"/", "/books"})
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public String listBooks(Model model) {
        List<BookDto> books = bookService.findAll();
        model.addAttribute("books", books);
        model.addAttribute("newBook", new BookDto(null, null, null, null, null, null));
        return "books/list";
    }

    @PostMapping("/add")
    public String addBook(@Valid @ModelAttribute("newBook") BookDto bookDto, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("books", bookService.findAll());
            return "books/list";
        }
        bookService.save(bookDto);
        return "redirect:/books";
    }
}
