package com.andres.course.agy.springboot.app.security;

import com.andres.course.agy.springboot.app.models.Role;
import com.andres.course.agy.springboot.app.models.User;
import com.andres.course.agy.springboot.app.repositories.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class SecurityIntegrationTests {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        if (userRepository.findByEmail("testadmin@example.com").isEmpty()) {
            userRepository.save(new User("testadmin@example.com", passwordEncoder.encode("password123"), Role.ADMIN));
        }
        if (userRepository.findByEmail("testuser@example.com").isEmpty()) {
            userRepository.save(new User("testuser@example.com", passwordEncoder.encode("password123"), Role.USER));
        }
    }

    @Test
    void testUnauthenticatedAccessToProtectedEndpointsReturns401() throws Exception {
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/invoices"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUserRoleCanOnlyReadCustomersAndCannotManageCustomersOrUsers() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"testuser@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("jwt_token"))
                .andExpect(cookie().httpOnly("jwt_token", true))
                .andReturn();

        Cookie jwtCookie = loginResult.getResponse().getCookie("jwt_token");
        assertNotNull(jwtCookie);

        // USER can GET /api/customers
        mockMvc.perform(get("/api/customers").cookie(jwtCookie))
                .andExpect(status().isOk());

        // USER can GET /api/invoices and /api/invoices/customer/{id}
        mockMvc.perform(get("/api/invoices").cookie(jwtCookie))
                .andExpect(status().isOk());

        // USER cannot POST /api/customers (403 Forbidden)
        mockMvc.perform(post("/api/customers")
                        .cookie(jwtCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"New\",\"lastName\":\"Customer\",\"email\":\"new.cust@example.com\"}"))
                .andExpect(status().isForbidden());

        // USER cannot POST /api/invoices (403 Forbidden)
        mockMvc.perform(post("/api/invoices")
                        .cookie(jwtCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":1,\"description\":\"Test Invoice\",\"amount\":100.00,\"status\":\"PENDING\"}"))
                .andExpect(status().isForbidden());

        // USER cannot access /api/users (403 Forbidden)
        mockMvc.perform(get("/api/users").cookie(jwtCookie))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAdminRoleCanManageBothUsersAndCustomers() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"testadmin@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("jwt_token"))
                .andExpect(cookie().httpOnly("jwt_token", true))
                .andReturn();

        Cookie jwtCookie = loginResult.getResponse().getCookie("jwt_token");
        assertNotNull(jwtCookie);

        // ADMIN can GET and POST /api/customers
        mockMvc.perform(get("/api/customers").cookie(jwtCookie))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/customers")
                        .cookie(jwtCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"AdminCreated\",\"lastName\":\"Customer\",\"email\":\"admin.cust@example.com\"}"))
                .andExpect(status().isCreated());

        // ADMIN can GET and POST /api/invoices
        mockMvc.perform(get("/api/invoices").cookie(jwtCookie))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/invoices")
                        .cookie(jwtCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":1,\"description\":\"Factura Admin\",\"amount\":500.00,\"status\":\"PAID\",\"items\":[{\"description\":\"Servicio\",\"quantity\":1,\"unitPrice\":500.00}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("Factura Admin"))
                .andExpect(jsonPath("$.status").value("PAID"));

        // ADMIN can GET and POST /api/users
        mockMvc.perform(get("/api/users").cookie(jwtCookie))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/users")
                        .cookie(jwtCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"createdbyadmin@example.com\",\"password\":\"password123\",\"role\":\"USER\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("createdbyadmin@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void testCorsPreflightAllowedForConfiguredOrigin() throws Exception {
        String testOrigin = allowedOrigins.split(",")[0].trim();
        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", testOrigin)
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", testOrigin))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    void testCorsPreflightBlockedForUnconfiguredOrigin() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", "https://malicious-origin.com")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }
}
