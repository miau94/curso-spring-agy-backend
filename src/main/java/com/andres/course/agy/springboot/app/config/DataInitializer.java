package com.andres.course.agy.springboot.app.config;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.andres.course.agy.springboot.app.models.Customer;
import com.andres.course.agy.springboot.app.models.Invoice;
import com.andres.course.agy.springboot.app.models.InvoiceItem;
import com.andres.course.agy.springboot.app.models.InvoiceStatus;
import com.andres.course.agy.springboot.app.models.Role;
import com.andres.course.agy.springboot.app.models.User;
import com.andres.course.agy.springboot.app.repositories.CustomerRepository;
import com.andres.course.agy.springboot.app.repositories.InvoiceRepository;
import com.andres.course.agy.springboot.app.repositories.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final InvoiceRepository invoiceRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, CustomerRepository customerRepository,
            InvoiceRepository invoiceRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.invoiceRepository = invoiceRepository;
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

        if (invoiceRepository.count() == 0) {
            List<Customer> customers = customerRepository.findAll();
            if (customers.size() >= 4) {
                Customer c1 = customers.get(0);
                Customer c2 = customers.get(1);
                Customer c3 = customers.get(2);
                Customer c4 = customers.get(3);

                Invoice inv1 = new Invoice(c1, "Desarrollo e implantación de API REST Spring Boot",
                        new BigDecimal("1850.00"), InvoiceStatus.PAID);
                inv1.addItem(new InvoiceItem("Diseño de arquitectura y seguridad JWT", 1, new BigDecimal("850.00")));
                inv1.addItem(new InvoiceItem("Desarrollo de endpoints CRUD y pruebas", 2, new BigDecimal("500.00")));
                invoiceRepository.save(inv1);

                Invoice inv2 = new Invoice(c1, "Mantenimiento mensual y soporte cloud",
                        new BigDecimal("450.00"), InvoiceStatus.PENDING);
                inv2.addItem(new InvoiceItem("Soporte técnico y monitorización 24/7", 1, new BigDecimal("300.00")));
                inv2.addItem(new InvoiceItem("Optimización de base de datos PostgreSQL", 1, new BigDecimal("150.00")));
                invoiceRepository.save(inv2);

                Invoice inv3 = new Invoice(c2, "Rediseño de aplicación web en React 19",
                        new BigDecimal("2400.00"), InvoiceStatus.PAID);
                inv3.addItem(new InvoiceItem("Diseño UI/UX y sistema de componentes", 1, new BigDecimal("900.00")));
                inv3.addItem(new InvoiceItem("Integración con estado global Zustand y Zod", 3, new BigDecimal("500.00")));
                invoiceRepository.save(inv3);

                Invoice inv4 = new Invoice(c3, "Auditoría de seguridad y rendimiento",
                        new BigDecimal("1200.00"), InvoiceStatus.OVERDUE);
                inv4.addItem(new InvoiceItem("Análisis de vulnerabilidades OWASP", 1, new BigDecimal("750.00")));
                inv4.addItem(new InvoiceItem("Pruebas de carga y optimización de índices", 1, new BigDecimal("450.00")));
                invoiceRepository.save(inv4);

                Invoice inv5 = new Invoice(c4, "Licencias de software y equipamiento de oficina",
                        new BigDecimal("980.00"), InvoiceStatus.CANCELLED);
                inv5.addItem(new InvoiceItem("Licencia anual entorno empresarial", 2, new BigDecimal("290.00")));
                inv5.addItem(new InvoiceItem("Monitor profesional 27 pulgadas 4K", 1, new BigDecimal("400.00")));
                invoiceRepository.save(inv5);

                System.out.println("5 invoices created");
            }
        }
    }

}
