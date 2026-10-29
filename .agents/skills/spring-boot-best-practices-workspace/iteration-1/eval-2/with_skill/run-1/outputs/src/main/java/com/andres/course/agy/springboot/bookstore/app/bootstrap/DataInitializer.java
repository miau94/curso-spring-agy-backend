package com.andres.course.agy.springboot.bookstore.app.bootstrap;

import com.andres.course.agy.springboot.bookstore.app.dto.BookDto;
import com.andres.course.agy.springboot.bookstore.app.services.BookService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final BookService bookService;

    public DataInitializer(BookService bookService) {
        this.bookService = bookService;
    }

    @Override
    public void run(String... args) throws Exception {
        bookService.save(new BookDto(null, "The Great Gatsby", "F. Scott Fitzgerald", "978-0743273565", 14.99, "A classic novel set in the jazz age."));
        bookService.save(new BookDto(null, "To Kill a Mockingbird", "Harper Lee", "978-0061120084", 12.99, "A story of racial injustice in the American South."));
        bookService.save(new BookDto(null, "1984", "George Orwell", "978-0451524935", 9.99, "A dystopian masterpiece showing totalitarian rule."));
        bookService.save(new BookDto(null, "Pride and Prejudice", "Jane Austen", "978-0141439518", 8.49, "A classic romantic novel of manners."));
    }
}
