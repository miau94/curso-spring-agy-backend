package com.andres.course.agy.springboot.app.services;

import com.andres.course.agy.springboot.app.dto.InvoiceDto;
import com.andres.course.agy.springboot.app.dto.InvoiceItemDto;
import com.andres.course.agy.springboot.app.exceptions.CustomerNotFoundException;
import com.andres.course.agy.springboot.app.exceptions.InvoiceNotFoundException;
import com.andres.course.agy.springboot.app.mappers.InvoiceMapper;
import com.andres.course.agy.springboot.app.models.Customer;
import com.andres.course.agy.springboot.app.models.Invoice;
import com.andres.course.agy.springboot.app.models.InvoiceItem;
import com.andres.course.agy.springboot.app.models.InvoiceStatus;
import com.andres.course.agy.springboot.app.repositories.CustomerRepository;
import com.andres.course.agy.springboot.app.repositories.InvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceImplTests {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private CustomerRepository customerRepository;

    private InvoiceMapper invoiceMapper;
    private InvoiceServiceImpl invoiceService;

    private Customer sampleCustomer;
    private Invoice sampleInvoice;

    @BeforeEach
    void setUp() {
        invoiceMapper = new InvoiceMapper();
        invoiceService = new InvoiceServiceImpl(invoiceRepository, customerRepository, invoiceMapper);

        sampleCustomer = new Customer("Carlos", "García", "carlos@example.com", "600111222", "Madrid");
        sampleCustomer.setId(1L);

        sampleInvoice = new Invoice(sampleCustomer, "Consultoría IT", new BigDecimal("1000.00"), InvoiceStatus.PAID);
        sampleInvoice.setId(10L);
        InvoiceItem item = new InvoiceItem("Horas de desarrollo", 2, new BigDecimal("500.00"));
        item.setId(100L);
        sampleInvoice.addItem(item);
    }

    @Test
    void testFindAllReturnsInvoiceDtos() {
        when(invoiceRepository.findAll()).thenReturn(List.of(sampleInvoice));

        List<InvoiceDto> result = invoiceService.findAll();

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).id());
        assertEquals(1L, result.get(0).customerId());
        assertEquals("Carlos García", result.get(0).customerName());
        assertEquals(new BigDecimal("1000.00"), result.get(0).amount());
        assertEquals(1, result.get(0).items().size());
    }

    @Test
    void testFindByCustomerIdThrowsWhenCustomerNotFound() {
        when(customerRepository.existsById(99L)).thenReturn(false);

        assertThrows(CustomerNotFoundException.class, () -> invoiceService.findByCustomerId(99L));
    }

    @Test
    void testFindByIdReturnsInvoiceDto() {
        when(invoiceRepository.findById(10L)).thenReturn(Optional.of(sampleInvoice));

        InvoiceDto result = invoiceService.findById(10L);

        assertNotNull(result);
        assertEquals("Consultoría IT", result.description());
        assertEquals(InvoiceStatus.PAID, result.status());
    }

    @Test
    void testFindByIdThrowsWhenNotFound() {
        when(invoiceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(InvoiceNotFoundException.class, () -> invoiceService.findById(99L));
    }

    @Test
    void testCreateComputesAmountFromItemsWhenAmountIsNull() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(sampleCustomer));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> {
            Invoice saved = inv.getArgument(0);
            saved.setId(20L);
            return saved;
        });

        InvoiceItemDto itemDto = new InvoiceItemDto(null, "Licencia", 3, new BigDecimal("200.00"), null);
        InvoiceDto request = new InvoiceDto(null, 1L, null, "Compra de licencias", null, InvoiceStatus.PENDING, List.of(itemDto));

        InvoiceDto created = invoiceService.create(request);

        assertEquals(20L, created.id());
        assertEquals(new BigDecimal("600.00"), created.amount());
        assertEquals(1, created.items().size());
        assertEquals(new BigDecimal("600.00"), created.items().get(0).subtotal());
    }

    @Test
    void testDeleteRemovesExistingInvoice() {
        when(invoiceRepository.existsById(10L)).thenReturn(true);

        invoiceService.delete(10L);

        verify(invoiceRepository, times(1)).deleteById(10L);
    }
}
