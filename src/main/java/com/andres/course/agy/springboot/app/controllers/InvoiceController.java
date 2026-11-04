package com.andres.course.agy.springboot.app.controllers;

import com.andres.course.agy.springboot.app.dto.InvoiceDto;
import com.andres.course.agy.springboot.app.services.InvoiceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping
    public ResponseEntity<List<InvoiceDto>> getAllInvoices(
            @RequestParam(required = false) Long customerId) {
        List<InvoiceDto> invoices = customerId != null
                ? invoiceService.findByCustomerId(customerId)
                : invoiceService.findAll();
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceDto> getInvoiceById(@PathVariable Long id) {
        InvoiceDto invoice = invoiceService.findById(id);
        return ResponseEntity.ok(invoice);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<InvoiceDto>> getInvoicesByCustomer(@PathVariable Long customerId) {
        List<InvoiceDto> invoices = invoiceService.findByCustomerId(customerId);
        return ResponseEntity.ok(invoices);
    }

    @PostMapping
    public ResponseEntity<InvoiceDto> createInvoice(@Valid @RequestBody InvoiceDto invoiceDto) {
        InvoiceDto createdInvoice = invoiceService.create(invoiceDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdInvoice);
    }

    @PutMapping("/{id}")
    public ResponseEntity<InvoiceDto> updateInvoice(@PathVariable Long id,
            @Valid @RequestBody InvoiceDto invoiceDto) {
        InvoiceDto updatedInvoice = invoiceService.update(id, invoiceDto);
        return ResponseEntity.ok(updatedInvoice);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvoice(@PathVariable Long id) {
        invoiceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
