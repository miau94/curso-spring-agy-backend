# AGENTS.md - Resumen del Proyecto `3-spring-api`

## 1. Descripción General
API RESTful construida con **Spring Boot** para la gestión de clientes (`Customer`) y usuarios (`User`). Implementa una arquitectura en capas con separación estricta entre entidades JPA y DTOs, protegida mediante **Spring Security y JWT con cookies `HttpOnly`**.

## 2. Stack Tecnológico y Configuración
- **Java**: 25 | **Spring Boot**: 4.1.0 (`spring-boot-starter-parent`)
- **Gestor de Dependencias**: Maven (uso obligatorio de Maven Wrapper `./mvnw` / `./mvnw.cmd`)
- **Dependencias Principales**:
  - `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, `spring-boot-starter-actuator`
  - `spring-boot-starter-security` + `io.jsonwebtoken:jjwt-*` (0.12.6) para autenticación JWT stateless
  - `com.h2database:h2` (BD en memoria) y `spring-boot-devtools`
- **Base de Datos (H2) y Datos Iniciales**:
  - URL: `jdbc:h2:mem:testdb` | Usuario: `sa` | Contraseña: *(vacía)* | Consola H2: `true` | DDL: `update`
  - `src/main/resources/import.sql`: Carga 10 clientes y 2 usuarios iniciales (`admin@example.com` [`ADMIN`] y `user@example.com` [`USER`], contraseña: `password123`).

## 3. Estructura de Paquetes y Arquitectura
Paquete base: `com.andres.course.agy.springboot.app`

- **`controllers`**: Controladores REST (`@RestController`) que operan exclusivamente con DTOs y `@Valid`.
  - `CustomerController` (`/api/customers`): `GET` (`USER`/`ADMIN`), `POST`/`PUT`/`DELETE` (solo `ADMIN`).
  - `UserController` (`/api/users`): CRUD completo de usuarios (solo `ADMIN`).
  - `AuthController` (`/api/auth`): `POST /login`, `POST /register` y `POST /logout` (públicos, emiten/limpian cookie `HttpOnly` `jwt_token`).
- **`services`**: Lógica de negocio (`@Service`) con inyección por constructor (`CustomerService`/`Impl`, `UserService`/`Impl`).
- **`repositories`**: Interfaces `JpaRepository` (`CustomerRepository`, `UserRepository`).
- **`models`**: Entidades JPA con auditoría automática (`createdAt`, `updatedAt`).
  - `Customer` (tabla `customers`), `User` (tabla `users` con `email`, `password` BCrypt y `Role`), `Role` (`USER`, `ADMIN`).
- **`dto`**: Implementados como `record` de Java sin exponer contraseñas ni auditoría en respuestas (`CustomerDto`, `UserDto`, `UserRequestDto`, `AuthRequestDto`).
- **`mappers`**: Conversión explícita Entidad <-> DTO (`CustomerMapper`, `UserMapper`).
- **`security`**: Configuración JWT y Spring Security (`SecurityConfig`, `JwtService`, `JwtAuthenticationFilter`).
- **`exceptions`**: Manejo centralizado con `@RestControllerAdvice` (`GlobalExceptionHandler`, `ErrorResponse`, `CustomerNotFoundException`, `UserNotFoundException`, `EmailAlreadyExistsException`).

## 4. Reglas y Convenciones de Desarrollo
1. **Configuración**: Usar exclusivamente `application.properties` (no usar archivos `.yml` / `.yaml`).
2. **Desacoplamiento**: Los controladores solo interactúan con `dto`. La conversión DTO <-> Entidad se realiza en `services` mediante `mappers`.
3. **DTOs**: Definir siempre como `record`, excluyendo campos sensibles (`password` en respuestas) y de auditoría (`createdAt`, `updatedAt`).
4. **Inyección de Dependencias**: Utilizar siempre inyección por constructor (sin `@Autowired` en campos).
5. **Testing**: Pruebas unitarias y de integración en `src/test/java` (`CustomerRepositoryTests`, `CustomerServiceImplTests`, `UserServiceImplTests`, `SecurityIntegrationTests`, `DefaultUsersImportTests`).

## 5. Comandos Frecuentes (Maven Wrapper)
- **Ejecutar la aplicación**: `./mvnw -DskipTests spring-boot:run`
- **Ejecutar pruebas**: `./mvnw test`
- **Compilar y empaquetar**: `./mvnw clean package`
