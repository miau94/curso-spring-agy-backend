package com.andres.course.agy.springboot.app.controllers;

import com.andres.course.agy.springboot.app.dto.CustomerDto;
import com.andres.course.agy.springboot.app.dto.InvoiceDto;
import com.andres.course.agy.springboot.app.services.CustomerService;
import com.andres.course.agy.springboot.app.services.InvoiceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final InvoiceService invoiceService;

    public CustomerController(CustomerService customerService, InvoiceService invoiceService) {
        this.customerService = customerService;
        this.invoiceService = invoiceService;
    }

    @GetMapping
    public ResponseEntity<List<CustomerDto>> getAllCustomers() {
        List<CustomerDto> customers = customerService.findAll();
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerDto> getCustomerById(@PathVariable Long id) {
        CustomerDto customer = customerService.findById(id);
        return ResponseEntity.ok(customer);
    }

    @GetMapping("/{id}/invoices")
    public ResponseEntity<List<InvoiceDto>> getCustomerInvoices(@PathVariable Long id) {
        List<InvoiceDto> invoices = invoiceService.findByCustomerId(id);
        return ResponseEntity.ok(invoices);
    }

    @PostMapping
    public ResponseEntity<CustomerDto> createCustomer(@Valid @RequestBody CustomerDto customerDto) {
        CustomerDto createdCustomer = customerService.create(customerDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCustomer);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerDto> updateCustomer(@PathVariable Long id,
            @Valid @RequestBody CustomerDto customerDto) {
        CustomerDto updatedCustomer = customerService.update(id, customerDto);
        return ResponseEntity.ok(updatedCustomer);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
