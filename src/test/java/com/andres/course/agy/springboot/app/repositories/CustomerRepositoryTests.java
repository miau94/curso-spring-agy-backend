package com.andres.course.agy.springboot.app.repositories;

import com.andres.course.agy.springboot.app.models.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CustomerRepositoryTests {

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void testInitialCustomersLoadedFromImportSql() {
        assertEquals(10, customerRepository.count());
    }

    @Test
    void testSaveCustomerAndAuditing() {
        Customer customer = new Customer("John", "Doe", "john.doe.test@example.com", "123456789", "123 Main St");

        Customer savedCustomer = customerRepository.saveAndFlush(customer);

        assertNotNull(savedCustomer.getId());
        assertNotNull(savedCustomer.getCreatedAt());
        assertNotNull(savedCustomer.getUpdatedAt());

        // Assert dates are set
        assertTrue(savedCustomer.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertTrue(savedCustomer.getCreatedAt().isAfter(LocalDateTime.now().minusSeconds(5)));
        assertEquals(savedCustomer.getCreatedAt(), savedCustomer.getUpdatedAt());
    }

    @Test
    void testUpdateCustomerAndAuditing() throws InterruptedException {
        Customer customer = new Customer("Jane", "Doe", "jane.doe.test@example.com", "987654321", "456 Oak Ave");
        Customer savedCustomer = customerRepository.saveAndFlush(customer);

        LocalDateTime initialCreatedAt = savedCustomer.getCreatedAt();
        LocalDateTime initialUpdatedAt = savedCustomer.getUpdatedAt();

        // Sleep briefly to ensure time difference
        Thread.sleep(50);

        savedCustomer.setFirstName("Janet");
        Customer updatedCustomer = customerRepository.saveAndFlush(savedCustomer);

        assertEquals(initialCreatedAt, updatedCustomer.getCreatedAt()); // Should not change
        assertNotEquals(initialUpdatedAt, updatedCustomer.getUpdatedAt()); // Should change
        assertTrue(updatedCustomer.getUpdatedAt().isAfter(initialUpdatedAt));
    }
}
