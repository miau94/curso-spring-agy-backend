package com.andres.course.agy.springboot.app.services;

import com.andres.course.agy.springboot.app.dto.CustomerDto;
import com.andres.course.agy.springboot.app.mappers.CustomerMapper;
import com.andres.course.agy.springboot.app.exceptions.CustomerNotFoundException;
import com.andres.course.agy.springboot.app.exceptions.EmailAlreadyExistsException;
import com.andres.course.agy.springboot.app.models.Customer;
import com.andres.course.agy.springboot.app.repositories.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTests {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerServiceImpl customerService;

    @Test
    void testFindAll() {
        Customer customer1 = new Customer("John", "Doe", "john@example.com", "123", "Street");
        Customer customer2 = new Customer("Jane", "Doe", "jane@example.com", "456", "Avenue");
        List<Customer> customers = Arrays.asList(customer1, customer2);

        CustomerDto dto1 = new CustomerDto(1L, "John", "Doe", "john@example.com", "123", "Street");
        CustomerDto dto2 = new CustomerDto(2L, "Jane", "Doe", "jane@example.com", "456", "Avenue");

        when(customerRepository.findAll()).thenReturn(customers);
        when(customerMapper.entityToDto(customer1)).thenReturn(dto1);
        when(customerMapper.entityToDto(customer2)).thenReturn(dto2);

        List<CustomerDto> result = customerService.findAll();

        assertEquals(2, result.size());
        assertEquals("John", result.get(0).firstName());
        assertEquals("Jane", result.get(1).firstName());
        verify(customerRepository, times(1)).findAll();
    }

    @Test
    void testFindByIdSuccess() {
        Customer customer = new Customer("John", "Doe", "john@example.com", "123", "Street");
        customer.setId(1L);
        CustomerDto dto = new CustomerDto(1L, "John", "Doe", "john@example.com", "123", "Street");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerMapper.entityToDto(customer)).thenReturn(dto);

        CustomerDto result = customerService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("John", result.firstName());
        verify(customerRepository, times(1)).findById(1L);
    }

    @Test
    void testFindByIdNotFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        CustomerNotFoundException exception = assertThrows(CustomerNotFoundException.class,
                () -> customerService.findById(1L));
        assertEquals("Customer not found with id: 1", exception.getMessage());
        verify(customerRepository, times(1)).findById(1L);
        verifyNoInteractions(customerMapper);
    }

    @Test
    void testCreateSuccess() {
        CustomerDto inputDto = new CustomerDto(null, "John", "Doe", "john@example.com", "123", "Street");
        Customer entity = new Customer("John", "Doe", "john@example.com", "123", "Street");
        Customer savedEntity = new Customer("John", "Doe", "john@example.com", "123", "Street");
        savedEntity.setId(1L);
        CustomerDto outputDto = new CustomerDto(1L, "John", "Doe", "john@example.com", "123", "Street");

        when(customerRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());
        when(customerMapper.dtoToEntity(inputDto)).thenReturn(entity);
        when(customerRepository.save(entity)).thenReturn(savedEntity);
        when(customerMapper.entityToDto(savedEntity)).thenReturn(outputDto);

        CustomerDto result = customerService.create(inputDto);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("john@example.com", result.email());
        verify(customerRepository, times(1)).findByEmail("john@example.com");
        verify(customerRepository, times(1)).save(entity);
    }

    @Test
    void testCreateEmailAlreadyExists() {
        CustomerDto inputDto = new CustomerDto(null, "John", "Doe", "john@example.com", "123", "Street");
        Customer existingCustomer = new Customer("Jane", "Doe", "john@example.com", "456", "Avenue");

        when(customerRepository.findByEmail("john@example.com")).thenReturn(Optional.of(existingCustomer));

        EmailAlreadyExistsException exception = assertThrows(EmailAlreadyExistsException.class,
                () -> customerService.create(inputDto));
        assertEquals("Email already in use: john@example.com", exception.getMessage());
        verify(customerRepository, times(1)).findByEmail("john@example.com");
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void testUpdateSuccess() {
        CustomerDto inputDto = new CustomerDto(1L, "JohnUpdated", "DoeUpdated", "john@example.com", "789", "Road");
        Customer existingCustomer = new Customer("John", "Doe", "john@example.com", "123", "Street");
        existingCustomer.setId(1L);
        Customer savedCustomer = new Customer("JohnUpdated", "DoeUpdated", "john@example.com", "789", "Road");
        savedCustomer.setId(1L);
        CustomerDto outputDto = new CustomerDto(1L, "JohnUpdated", "DoeUpdated", "john@example.com", "789", "Road");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(existingCustomer));
        when(customerRepository.save(existingCustomer)).thenReturn(savedCustomer);
        when(customerMapper.entityToDto(savedCustomer)).thenReturn(outputDto);

        CustomerDto result = customerService.update(1L, inputDto);

        assertNotNull(result);
        assertEquals("JohnUpdated", result.firstName());
        assertEquals("789", result.phone());
        verify(customerRepository, times(1)).findById(1L);
        verify(customerRepository, times(1)).save(existingCustomer);
    }

    @Test
    void testUpdateEmailConflict() {
        CustomerDto inputDto = new CustomerDto(1L, "John", "Doe", "conflict@example.com", "123", "Street");
        Customer existingCustomer = new Customer("John", "Doe", "john@example.com", "123", "Street");
        existingCustomer.setId(1L);
        Customer otherCustomer = new Customer("Jane", "Doe", "conflict@example.com", "456", "Avenue");
        otherCustomer.setId(2L);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(existingCustomer));
        when(customerRepository.findByEmail("conflict@example.com")).thenReturn(Optional.of(otherCustomer));

        EmailAlreadyExistsException exception = assertThrows(EmailAlreadyExistsException.class,
                () -> customerService.update(1L, inputDto));
        assertEquals("Email already in use: conflict@example.com", exception.getMessage());
        verify(customerRepository, times(1)).findById(1L);
        verify(customerRepository, times(1)).findByEmail("conflict@example.com");
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void testDeleteSuccess() {
        when(customerRepository.existsById(1L)).thenReturn(true);

        assertDoesNotThrow(() -> customerService.delete(1L));

        verify(customerRepository, times(1)).existsById(1L);
        verify(customerRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteNotFound() {
        when(customerRepository.existsById(1L)).thenReturn(false);

        CustomerNotFoundException exception = assertThrows(CustomerNotFoundException.class,
                () -> customerService.delete(1L));
        assertEquals("Customer not found with id: 1", exception.getMessage());
        verify(customerRepository, times(1)).existsById(1L);
        verify(customerRepository, never()).deleteById(1L);
    }
}
