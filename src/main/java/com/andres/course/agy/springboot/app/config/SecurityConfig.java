package com.andres.course.agy.springboot.app.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.andres.course.agy.springboot.app.security.JwtAuthenticationFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final String allowedOrigins;

    /**
     * Constructor que inyecta el filtro de autenticación JWT y los orígenes permitidos de CORS.
     *
     * @param jwtAuthenticationFilter Filtro que valida el token JWT en cada petición HTTP.
     * @param allowedOrigins          Lista de orígenes permitidos delimitados por coma.
     */
    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            @Value("${cors.allowed-origins}") String allowedOrigins) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.allowedOrigins = allowedOrigins;
    }

    /**
     * Configura la cadena de filtros de seguridad HTTP (CORS, stateless, reglas de
     * autorización por rol,
     * manejo de errores 401/403 y registro del filtro JWT antes del filtro de
     * autenticación estándar).
     *
     * @param http Objeto HttpSecurity para definir la configuración de seguridad
     *             web.
     * @return La cadena de filtros SecurityFilterChain construida.
     * @throws Exception Si ocurre algún error al construir la configuración de
     *                   seguridad.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // habilitar CORS con la configuración personalizada
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
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
                        // permitir preflight CORS OPTIONS
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
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
                        // leer facturas, usuario o admin
                        .requestMatchers(HttpMethod.GET, "/api/invoices", "/api/invoices/**")
                        .hasAnyRole("USER", "ADMIN")
                        // administrar facturas, solo admin
                        .requestMatchers(HttpMethod.POST, "/api/invoices", "/api/invoices/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/invoices", "/api/invoices/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/invoices", "/api/invoices/**").hasRole("ADMIN")
                        // solo admin
                        .requestMatchers("/api/users", "/api/users/**").hasRole("ADMIN")
                        // cualquier otra peticion requiere autenticacion
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configura el origen cruzado (CORS) permitiendo los dominios autorizados
     * configurados en las variables de entorno / application.properties.
     *
     * @return CorsConfigurationSource con los orígenes, métodos y cabeceras permitidos.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();

        configuration.setAllowedOriginPatterns(origins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Set-Cookie", "Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Define el bean encargado de encriptar y verificar las contraseñas utilizando
     * el algoritmo BCrypt.
     *
     * @return Instancia de BCryptPasswordEncoder.
     */
    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Expone el AuthenticationManager de Spring Security como un bean para procesar
     * las solicitudes de login.
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
