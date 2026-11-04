package com.andres.course.agy.springboot.app.services;

import com.andres.course.agy.springboot.app.dto.InvoiceDto;

import java.util.List;

public interface InvoiceService {

    List<InvoiceDto> findAll();

    List<InvoiceDto> findByCustomerId(Long customerId);

    InvoiceDto findById(Long id);

    InvoiceDto create(InvoiceDto invoiceDto);

    InvoiceDto update(Long id, InvoiceDto invoiceDto);

    void delete(Long id);
}
