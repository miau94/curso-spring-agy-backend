package com.andres.course.agy.springboot.app.mappers;

import com.andres.course.agy.springboot.app.dto.CustomerDto;
import com.andres.course.agy.springboot.app.models.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    /**
     * Map Customer Entity to CustomerDto.
     *
     * @param customer the Customer entity
     * @return the CustomerDto record
     */
    public CustomerDto entityToDto(Customer customer) {
        if (customer == null) {
            return null;
        }
        return new CustomerDto(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getAddress());
    }

    /**
     * Map CustomerDto to Customer Entity.
     *
     * @param dto the CustomerDto record
     * @return the Customer entity
     */
    public Customer dtoToEntity(CustomerDto dto) {
        if (dto == null) {
            return null;
        }
        Customer customer = new Customer();
        customer.setId(dto.id());
        customer.setFirstName(dto.firstName());
        customer.setLastName(dto.lastName());
        customer.setEmail(dto.email());
        customer.setPhone(dto.phone());
        customer.setAddress(dto.address());
        return customer;
    }
}
