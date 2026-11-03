package com.andres.course.agy.springboot.app.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.andres.course.agy.springboot.app.models.Customer;
import com.andres.course.agy.springboot.app.models.Role;
import com.andres.course.agy.springboot.app.models.User;
import com.andres.course.agy.springboot.app.repositories.CustomerRepository;
import com.andres.course.agy.springboot.app.repositories.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, CustomerRepository customerRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            // Create admin user
            User admin = new User();
            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode("password123"));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);

            // Create standard user
            User user = new User();
            user.setEmail("user@example.com");
            user.setPassword(passwordEncoder.encode("password123"));
            user.setRole(Role.USER);
            userRepository.save(user);

            System.out.println("Admin and user created");
        }

        if (customerRepository.count() == 0) {
            customerRepository.save(new Customer("Carlos", "García", "carlos.garcia@example.com", "+34 600 111 222",
                    "Calle Mayor 10, Madrid"));
            customerRepository.save(new Customer("María", "Rodríguez", "maria.rodriguez@example.com", "+34 600 222 333",
                    "Avenida Diagonal 45, Barcelona"));
            customerRepository.save(new Customer("Alejandro", "Martínez", "alejandro.martinez@example.com",
                    "+34 600 333 444", "Calle Sierpes 12, Sevilla"));
            customerRepository.save(new Customer("Lucía", "Hernández", "lucia.hernandez@example.com", "+34 600 444 555",
                    "Gran Vía 28, Bilbao"));
            customerRepository.save(new Customer("David", "López", "david.lopez@example.com", "+34 600 555 666",
                    "Calle Colón 18, Valencia"));
            customerRepository.save(new Customer("Elena", "González", "elena.gonzalez@example.com", "+34 600 666 777",
                    "Paseo Independencia 8, Zaragoza"));
            customerRepository.save(new Customer("Javier", "Pérez", "javier.perez@example.com", "+34 600 777 888",
                    "Calle Larios 5, Málaga"));
            customerRepository.save(new Customer("Sofía", "Sánchez", "sofia.sanchez@example.com", "+34 600 888 999",
                    "Calle Uría 30, Oviedo"));
            customerRepository.save(new Customer("Daniel", "Ramírez", "daniel.ramirez@example.com", "+34 600 999 000",
                    "Calle Reyes Católicos 15, Granada"));
            customerRepository.save(new Customer("Paula", "Torres", "paula.torres@example.com", "+34 601 000 111",
                    "Calle San Miguel 22, Palma"));

            System.out.println("10 customers created");
        }
    }

}
