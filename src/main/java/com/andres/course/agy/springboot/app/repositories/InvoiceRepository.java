package com.andres.course.agy.springboot.app.repositories;

import com.andres.course.agy.springboot.app.models.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    List<Invoice> findByCustomerId(Long customerId);
}
