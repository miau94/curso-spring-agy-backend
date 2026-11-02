# AGENTS.md - Resumen del Proyecto `3-spring-api`

## 1. Descripción General
API RESTful construida con **Spring Boot** para la gestión de clientes (`Customer`) y usuarios (`User`). Implementa una arquitectura en capas con separación estricta entre entidades JPA y DTOs, protegida mediante **Spring Security y JWT con cookies `HttpOnly`**.

## 2. Stack Tecnológico y Configuración
- **Java**: 25 | **Spring Boot**: 4.1.0 (`spring-boot-starter-parent`)
- **Gestor de Dependencias**: Maven (uso obligatorio de Maven Wrapper `./mvnw` / `./mvnw.cmd`)
- **Dependencias Principales**:
  - `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, `spring-boot-starter-actuator`
  - `spring-boot-starter-security` + `io.jsonwebtoken:jjwt-*` (0.12.6) para autenticación JWT stateless
  - `com.h2database:h2`, `spring-boot-h2console` (BD en memoria) y `spring-boot-devtools`
  - `spring-boot-starter-test`, `spring-security-test` (Testing)
- **Base de Datos (H2) y Datos Iniciales**:
  - URL: `jdbc:h2:mem:testdb` | Usuario: `sa` | Contraseña: *(vacía)* | Consola H2: `/h2-console` (`true`) | DDL: `update`
  - `src/main/resources/import.sql`: Carga 10 clientes y 2 usuarios iniciales (`admin@example.com` [`ADMIN`] y `user@example.com` [`USER`], contraseña: `password123`).
- **Configuración JWT (`application.properties`)**:
  - `jwt.secret`: Clave secreta hexadecimal HMAC-SHA
  - `jwt.expiration`: `86400000` ms (24 horas)
  - `jwt.cookie-name`: `jwt_token` (`HttpOnly`)

## 3. Estructura de Paquetes y Arquitectura
Paquete base: `com.andres.course.agy.springboot.app`

- **`controllers`**: Controladores REST (`@RestController`) que operan exclusivamente con DTOs y `@Valid` (`AuthController`, `CustomerController`, `UserController`).
- **`services`**: Lógica de negocio (`@Service`) con inyección por constructor (`CustomerService`/`CustomerServiceImpl`, `UserService`/`UserServiceImpl` que implementa también `UserDetailsService`).
- **`repositories`**: Interfaces `JpaRepository` (`CustomerRepository`, `UserRepository`).
- **`models`**: Entidades JPA con auditoría automática (`createdAt`, `updatedAt`).
  - `Customer` (tabla `customers`: `id`, `firstName`, `lastName`, `email`, `phone`, `address`).
  - `User` (tabla `users`: `id`, `email`, `password` BCrypt y `role`).
  - `Role` (enum: `USER`, `ADMIN`).
- **`dto`**: Implementados como `record` de Java sin exponer contraseñas ni auditoría en respuestas (`CustomerDto`, `UserDto`, `UserRequestDto`, `AuthRequestDto`).
- **`mappers`**: Conversión explícita Entidad <-> DTO (`CustomerMapper`, `UserMapper`).
- **`security`**: Configuración JWT y Spring Security stateless (`SecurityConfig`, `JwtService`, `JwtAuthenticationFilter`).
- **`exceptions`**: Manejo centralizado con `@RestControllerAdvice` (`GlobalExceptionHandler`, `ErrorResponse`, `CustomerNotFoundException`, `UserNotFoundException`, `EmailAlreadyExistsException`).

## 4. Endpoints Disponibles

### 4.1. Autenticación (`/api/auth`) - `AuthController`
| Método | Ruta | Acceso | Request Body | Respuesta Exitosa | Descripción |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/login` | Público | `AuthRequestDto` (`email`, `password`) | `200 OK` (`UserDto` + Cookie `jwt_token`) | Autentica al usuario y emite el JWT en una cookie `HttpOnly`. |
| `POST` | `/api/auth/register` | Público | `AuthRequestDto` (`email`, `password`) | `201 Created` (`UserDto` + Cookie `jwt_token`) | Registra un usuario con rol `USER` y emite la cookie `jwt_token`. |
| `POST` | `/api/auth/logout` | Público | *(Ninguno)* | `200 OK` (`{"message": "Logged out successfully"}`) | Limpia la cookie `jwt_token` (`maxAge=0`). |

### 4.2. Clientes (`/api/customers`) - `CustomerController`
| Método | Ruta | Acceso | Request Body | Respuesta Exitosa | Descripción |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/customers` | `USER`, `ADMIN` | *(Ninguno)* | `200 OK` (`List<CustomerDto>`) | Obtiene el listado completo de clientes. |
| `GET` | `/api/customers/{id}` | `USER`, `ADMIN` | *(Ninguno)* | `200 OK` (`CustomerDto`) | Obtiene un cliente por su ID (`404` si no existe). |
| `POST` | `/api/customers` | `ADMIN` | `CustomerDto` (`firstName`, `lastName`, `email`, `phone`, `address`) | `201 Created` (`CustomerDto`) | Crea un nuevo cliente (`400` si el email ya existe o datos inválidos). |
| `PUT` | `/api/customers/{id}` | `ADMIN` | `CustomerDto` (`firstName`, `lastName`, `email`, `phone`, `address`) | `200 OK` (`CustomerDto`) | Actualiza un cliente existente por su ID (`404` si no existe). |
| `DELETE` | `/api/customers/{id}` | `ADMIN` | *(Ninguno)* | `204 No Content` | Elimina un cliente por su ID (`404` si no existe). |

### 4.3. Usuarios (`/api/users`) - `UserController`
| Método | Ruta | Acceso | Request Body | Respuesta Exitosa | Descripción |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/users` | `ADMIN` | *(Ninguno)* | `200 OK` (`List<UserDto>`) | Lista todos los usuarios registrados (sin exponer contraseñas). |
| `GET` | `/api/users/{id}` | `ADMIN` | *(Ninguno)* | `200 OK` (`UserDto`) | Obtiene un usuario por su ID (`404` si no existe). |
| `POST` | `/api/users` | `ADMIN` | `UserRequestDto` (`email`, `password`, `role`) | `201 Created` (`UserDto`) | Crea un usuario con contraseña encriptada en BCrypt. |
| `PUT` | `/api/users/{id}` | `ADMIN` | `UserRequestDto` (`email`, `password`, `role`) | `200 OK` (`UserDto`) | Actualiza email, contraseña y rol de un usuario existente. |
| `DELETE` | `/api/users/{id}` | `ADMIN` | *(Ninguno)* | `204 No Content` | Elimina un usuario por su ID (`404` si no existe). |

### 4.4. Endpoints Técnicos y de Desarrollo
| Método | Ruta | Acceso | Descripción |
| :--- | :--- | :--- | :--- |
| `GET`/`POST` | `/h2-console/**` | Público | Consola web de la base de datos en memoria H2. |
| `GET` | `/actuator/**` | Público | Endpoints de monitorización y estado de Spring Boot Actuator (ej. `/actuator/health`). |

## 5. Reglas y Convenciones de Desarrollo
1. **Configuración**: Usar exclusivamente `application.properties` (no usar archivos `.yml` / `.yaml`).
2. **Desacoplamiento**: Los controladores solo interactúan con `dto`. La conversión DTO <-> Entidad se realiza en `services` mediante `mappers`.
3. **DTOs**: Definir siempre como `record`, excluyendo campos sensibles (`password` en respuestas) y de auditoría (`createdAt`, `updatedAt`).
4. **Inyección de Dependencias**: Utilizar siempre inyección por constructor (sin `@Autowired` en campos).
5. **Testing**: Pruebas unitarias y de integración en `src/test/java` (`ApplicationTests`, `CustomerRepositoryTests`, `CustomerServiceImplTests`, `UserServiceImplTests`, `SecurityIntegrationTests`, `DefaultUsersImportTests`).

## 6. Comandos Frecuentes (Maven Wrapper)
- **Ejecutar la aplicación**: `./mvnw -DskipTests spring-boot:run`
- **Ejecutar pruebas**: `./mvnw test`
- **Compilar y empaquetar**: `./mvnw clean package`
