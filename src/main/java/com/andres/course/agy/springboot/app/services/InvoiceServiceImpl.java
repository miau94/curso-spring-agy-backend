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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final InvoiceMapper invoiceMapper;

    public InvoiceServiceImpl(InvoiceRepository invoiceRepository,
                              CustomerRepository customerRepository,
                              InvoiceMapper invoiceMapper) {
        this.invoiceRepository = invoiceRepository;
        this.customerRepository = customerRepository;
        this.invoiceMapper = invoiceMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceDto> findAll() {
        return invoiceRepository.findAll().stream()
                .map(invoiceMapper::entityToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceDto> findByCustomerId(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException(customerId);
        }
        return invoiceRepository.findByCustomerId(customerId).stream()
                .map(invoiceMapper::entityToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceDto findById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new InvoiceNotFoundException(id));
        return invoiceMapper.entityToDto(invoice);
    }

    @Override
    @Transactional
    public InvoiceDto create(InvoiceDto invoiceDto) {
        Customer customer = customerRepository.findById(invoiceDto.customerId())
                .orElseThrow(() -> new CustomerNotFoundException(invoiceDto.customerId()));

        Invoice invoice = invoiceMapper.dtoToEntity(invoiceDto);
        invoice.setId(null);
        invoice.setCustomer(customer);
        if (invoice.getItems() != null) {
            invoice.getItems().forEach(item -> item.setId(null));
        }

        Invoice savedInvoice = invoiceRepository.save(invoice);
        return invoiceMapper.entityToDto(savedInvoice);
    }

    @Override
    @Transactional
    public InvoiceDto update(Long id, InvoiceDto invoiceDto) {
        Invoice existingInvoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new InvoiceNotFoundException(id));

        Customer customer = customerRepository.findById(invoiceDto.customerId())
                .orElseThrow(() -> new CustomerNotFoundException(invoiceDto.customerId()));

        existingInvoice.setCustomer(customer);
        existingInvoice.setDescription(invoiceDto.description());
        existingInvoice.setStatus(invoiceDto.status() != null ? invoiceDto.status() : InvoiceStatus.PENDING);

        existingInvoice.clearItems();
        if (invoiceDto.items() != null && !invoiceDto.items().isEmpty()) {
            BigDecimal computedAmount = BigDecimal.ZERO;
            for (InvoiceItemDto itemDto : invoiceDto.items()) {
                InvoiceItem item = invoiceMapper.itemDtoToEntity(itemDto);
                item.setId(null);
                existingInvoice.addItem(item);
                if (item.getSubtotal() != null) {
                    computedAmount = computedAmount.add(item.getSubtotal());
                }
            }
            existingInvoice.setAmount(invoiceDto.amount() != null ? invoiceDto.amount() : computedAmount);
        } else {
            existingInvoice.setAmount(invoiceDto.amount() != null ? invoiceDto.amount() : BigDecimal.ZERO);
        }

        Invoice updatedInvoice = invoiceRepository.save(existingInvoice);
        return invoiceMapper.entityToDto(updatedInvoice);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!invoiceRepository.existsById(id)) {
            throw new InvoiceNotFoundException(id);
        }
        invoiceRepository.deleteById(id);
    }
}
