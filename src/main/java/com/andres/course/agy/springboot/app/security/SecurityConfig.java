package com.andres.course.agy.springboot.app.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Constructor que inyecta el filtro de autenticación JWT personalizado.
     *
     * @param jwtAuthenticationFilter Filtro que valida el token JWT en cada petición HTTP.
     */
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Configura la cadena de filtros de seguridad HTTP (stateless, reglas de autorización por rol,
     * manejo de errores 401/403 y registro del filtro JWT antes del filtro de autenticación estándar).
     *
     * @param http Objeto HttpSecurity para definir la configuración de seguridad web.
     * @return La cadena de filtros SecurityFilterChain construida.
     * @throws Exception Si ocurre algún error al construir la configuración de seguridad.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // para formularios, se desabilita
                .csrf(csfr -> csfr.disable())
                // para h2-console
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()))
                // para formularios, se desabilita
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> response
                                .sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized"))
                        .accessDeniedHandler((request, response, accessDeniedException) -> response
                                .sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden")))
                .authorizeHttpRequests(auth -> auth
                        // todos pueden autenticarse
                        .requestMatchers("/api/auth/**", "/h2-console", "/h2-console/**", "/actuator/**").permitAll()
                        // leer clientes, usuario o admin
                        .requestMatchers(HttpMethod.GET, "/api/customers", "/api/customers/**")
                        .hasAnyRole("USER", "ADMIN")
                        // solo admin
                        .requestMatchers(HttpMethod.POST, "/api/customers", "/api/customers/**").hasRole("ADMIN")
                        // solo admin
                        .requestMatchers(HttpMethod.PUT, "/api/customers", "/api/customers/**").hasRole("ADMIN")
                        // solo admin
                        .requestMatchers(HttpMethod.DELETE, "/api/customers", "/api/customers/**").hasRole("ADMIN")
                        // solo admin
                        .requestMatchers("/api/users", "/api/users/**").hasRole("ADMIN")
                        // cualquier otra peticion requiere autenticacion
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Define el bean encargado de encriptar y verificar las contraseñas utilizando el algoritmo BCrypt.
     *
     * @return Instancia de BCryptPasswordEncoder.
     */
    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Expone el AuthenticationManager de Spring Security como un bean para procesar las solicitudes de login.
     *
     * @param config Configuración de autenticación provista por Spring Security.
     * @return El AuthenticationManager configurado.
     * @throws Exception Si no se puede obtener el AuthenticationManager.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
