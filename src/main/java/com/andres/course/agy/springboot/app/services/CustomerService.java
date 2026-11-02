package com.andres.course.agy.springboot.app.services;

import java.util.List;
import com.andres.course.agy.springboot.app.dto.CustomerDto;

public interface CustomerService {

    List<CustomerDto> findAll();

    CustomerDto findById(Long id);

    CustomerDto create(CustomerDto customerDto);

    CustomerDto update(Long id, CustomerDto customerDto);

    void delete(Long id);

}