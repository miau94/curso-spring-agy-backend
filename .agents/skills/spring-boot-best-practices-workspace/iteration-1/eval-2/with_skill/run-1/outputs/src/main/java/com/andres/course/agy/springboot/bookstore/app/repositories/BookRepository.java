package com.andres.course.agy.springboot.bookstore.app.repositories;

import com.andres.course.agy.springboot.bookstore.app.models.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
}
