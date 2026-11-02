package com.andres.course.agy.springboot.app.services;

import com.andres.course.agy.springboot.app.dto.CustomerDto;
import com.andres.course.agy.springboot.app.mappers.CustomerMapper;
import com.andres.course.agy.springboot.app.models.Customer;
import com.andres.course.agy.springboot.app.exceptions.CustomerNotFoundException;
import com.andres.course.agy.springboot.app.exceptions.EmailAlreadyExistsException;
import com.andres.course.agy.springboot.app.repositories.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public CustomerServiceImpl(CustomerRepository customerRepository, CustomerMapper customerMapper) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerDto> findAll() {
        return customerRepository.findAll().stream()
                .map(customerMapper::entityToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerDto findById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
        return customerMapper.entityToDto(customer);
    }

    @Override
    @Transactional
    public CustomerDto create(CustomerDto customerDto) {
        if (customerRepository.findByEmail(customerDto.email()).isPresent()) {
            throw new EmailAlreadyExistsException(customerDto.email());
        }
        Customer customer = customerMapper.dtoToEntity(customerDto);
        customer.setId(null); // Ensure creation instead of update
        Customer savedCustomer = customerRepository.save(customer);
        return customerMapper.entityToDto(savedCustomer);
    }

    @Override
    @Transactional
    public CustomerDto update(Long id, CustomerDto customerDto) {
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        // Check if new email is already taken by another customer
        if (!existingCustomer.getEmail().equalsIgnoreCase(customerDto.email()) &&
                customerRepository.findByEmail(customerDto.email()).isPresent()) {
            throw new EmailAlreadyExistsException(customerDto.email());
        }

        existingCustomer.setFirstName(customerDto.firstName());
        existingCustomer.setLastName(customerDto.lastName());
        existingCustomer.setEmail(customerDto.email());
        existingCustomer.setPhone(customerDto.phone());
        existingCustomer.setAddress(customerDto.address());

        Customer updatedCustomer = customerRepository.save(existingCustomer);
        return customerMapper.entityToDto(updatedCustomer);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new CustomerNotFoundException(id);
        }
        customerRepository.deleteById(id);
    }
}
