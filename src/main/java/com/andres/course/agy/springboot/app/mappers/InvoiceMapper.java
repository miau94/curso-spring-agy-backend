package com.andres.course.agy.springboot.app.mappers;

import com.andres.course.agy.springboot.app.dto.InvoiceDto;
import com.andres.course.agy.springboot.app.dto.InvoiceItemDto;
import com.andres.course.agy.springboot.app.models.Customer;
import com.andres.course.agy.springboot.app.models.Invoice;
import com.andres.course.agy.springboot.app.models.InvoiceItem;
import com.andres.course.agy.springboot.app.models.InvoiceStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class InvoiceMapper {

    public InvoiceDto entityToDto(Invoice invoice) {
        if (invoice == null) {
            return null;
        }
        Customer customer = invoice.getCustomer();
        Long customerId = customer != null ? customer.getId() : null;
        String customerName = customer != null
                ? (customer.getFirstName() + " " + customer.getLastName()).trim()
                : null;

        List<InvoiceItemDto> itemDtos = invoice.getItems() != null
                ? invoice.getItems().stream()
                        .map(this::itemEntityToDto)
                        .collect(Collectors.toList())
                : Collections.emptyList();

        return new InvoiceDto(
                invoice.getId(),
                customerId,
                customerName,
                invoice.getDescription(),
                invoice.getAmount(),
                invoice.getStatus(),
                itemDtos
        );
    }

    public Invoice dtoToEntity(InvoiceDto dto) {
        if (dto == null) {
            return null;
        }
        Invoice invoice = new Invoice();
        invoice.setId(dto.id());
        invoice.setDescription(dto.description());
        invoice.setStatus(dto.status() != null ? dto.status() : InvoiceStatus.PENDING);

        if (dto.items() != null && !dto.items().isEmpty()) {
            BigDecimal computedAmount = BigDecimal.ZERO;
            for (InvoiceItemDto itemDto : dto.items()) {
                InvoiceItem item = itemDtoToEntity(itemDto);
                invoice.addItem(item);
                if (item.getSubtotal() != null) {
                    computedAmount = computedAmount.add(item.getSubtotal());
                }
            }
            invoice.setAmount(dto.amount() != null ? dto.amount() : computedAmount);
        } else {
            invoice.setAmount(dto.amount() != null ? dto.amount() : BigDecimal.ZERO);
        }

        return invoice;
    }

    public InvoiceItemDto itemEntityToDto(InvoiceItem item) {
        if (item == null) {
            return null;
        }
        return new InvoiceItemDto(
                item.getId(),
                item.getDescription(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal()
        );
    }

    public InvoiceItem itemDtoToEntity(InvoiceItemDto dto) {
        if (dto == null) {
            return null;
        }
        InvoiceItem item = new InvoiceItem();
        item.setId(dto.id());
        item.setDescription(dto.description());
        item.setQuantity(dto.quantity() != null ? dto.quantity() : 1);
        item.setUnitPrice(dto.unitPrice() != null ? dto.unitPrice() : BigDecimal.ZERO);
        if (dto.subtotal() != null) {
            item.setSubtotal(dto.subtotal());
        } else {
            item.calculateSubtotal();
        }
        return item;
    }
}
