package com.bookstore.repository;

import com.bookstore.model.Book;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class BookRepository {
    private final ConcurrentHashMap<Long, Book> books = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public BookRepository() {
        // Initialize with some mock data containing sensitive fields (costPrice, supplierContact, internalNotes)
        save(new Book(null, "The Great Gatsby", "F. Scott Fitzgerald", "9780743273565", new BigDecimal("14.99"), 
            "A novel that depicts the romantic obsession, wealth, and social history of the Jazz Age.", 1925, 
            new BigDecimal("6.50"), "Classic Books Publishing (distributor@classicbooks.com)", "Popular during school semesters"));
            
        save(new Book(null, "To Kill a Mockingbird", "Harper Lee", "9780061120084", new BigDecimal("12.99"), 
            "The story of racial injustice and the destruction of innocence in a small Southern town.", 1960, 
            new BigDecimal("5.25"), "HarperCollins Distribution (sales@harpercollins.com)", "Always keep 10+ copies in stock"));
            
        save(new Book(null, "1984", "George Orwell", "9780451524935", new BigDecimal("9.99"), 
            "A dystopian social science fiction novel focusing on the consequences of totalitarianism, mass surveillance, and repressive regimentation.", 1949, 
            new BigDecimal("4.00"), "Signet Classics (signet@penguinrandomhouse.com)", "Steady demand"));

        save(new Book(null, "The Hobbit", "J.R.R. Tolkien", "9780547928227", new BigDecimal("15.99"), 
            "A children's fantasy novel following the quest of home-loving hobbit Bilbo Baggins.", 1937, 
            new BigDecimal("7.10"), "Houghton Mifflin Harcourt (orders@hmhco.com)", "Check for anniversary edition editions"));
            
        save(new Book(null, "The Catcher in the Rye", "J.D. Salinger", "9780316769174", new BigDecimal("10.99"), 
            "A novel about a teenager's anger and alienation in New York City.", 1951, 
            new BigDecimal("4.80"), "Little, Brown and Company (littletraffic@hbgusa.com)", "Display near modern fiction section"));
    }

    public List<Book> findAll() {
        return new ArrayList<>(books.values());
    }

    public Optional<Book> findById(Long id) {
        return Optional.ofNullable(books.get(id));
    }

    public Book save(Book book) {
        if (book.getId() == null) {
            book.setId(idGenerator.getAndIncrement());
        }
        books.put(book.getId(), book);
        return book;
    }

    public void deleteById(Long id) {
        books.remove(id);
    }
}
