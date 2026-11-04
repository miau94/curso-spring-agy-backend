# AGENTS.md - Resumen del Proyecto `3-spring-api`

## 1. Descripción General
API RESTful construida con **Spring Boot** para la gestión de clientes (`Customer`), facturación (`Invoice` e `InvoiceItem`) y usuarios (`User`). Implementa una arquitectura en capas con separación estricta entre entidades JPA y DTOs, protegida mediante **Spring Security y JWT con cookies `HttpOnly`**.

## 2. Stack Tecnológico y Configuración
- **Java**: 25 | **Spring Boot**: 4.1.0 (`spring-boot-starter-parent`)
- **Gestor de Dependencias**: Maven (uso obligatorio de Maven Wrapper `./mvnw` / `./mvnw.cmd`)
- **Dependencias Principales**:
  - `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, `spring-boot-starter-actuator`
  - `spring-boot-starter-security` + `io.jsonwebtoken:jjwt-*` (0.12.6) para autenticación JWT stateless
  - `org.postgresql:postgresql` (`runtime`, BD principal) y `spring-boot-devtools`
  - `com.h2database:h2` (`test`, BD en memoria para pruebas), `spring-boot-starter-test`, `spring-security-test` (Testing)
- **Variables de Entorno (`.env`)**:
  - Importadas automáticamente en `src/main/resources/application.properties` mediante `spring.config.import=optional:file:.env[.properties]` (el archivo `.env` está excluido en `.gitignore`).
- **Base de Datos y Datos Iniciales**:
  - **PostgreSQL / Supabase (`src/main/resources/application.properties`)**:
    - URL: `${SUPABASE_DB_URL}` (Session Pooler IPv4 de Supabase `aws-1-eu-central-1.pooler.supabase.com:5432/postgres` o PostgreSQL en Docker local `localhost:5433/spring_backend`) | Usuario: `${SUPABASE_DB_USER}` | Contraseña: `${SUPABASE_DB_PASS}` | Dialect: `PostgreSQLDialect` | DDL: `update`
  - **H2 para Testing (`src/test/resources/application.properties`)**:
    - URL: `jdbc:h2:mem:testdb` | Usuario: `sa` | Contraseña: *(vacía)* | Dialect: `H2Dialect` | DDL: `create-drop`
  - **Inicialización de Datos (`DataInitializer`)**:
    - Componente `CommandLineRunner` que verifica si las tablas están vacías (`count() == 0`) al iniciar la aplicación y carga 10 clientes, 2 usuarios iniciales (`admin@example.com` [`ADMIN`] y `user@example.com` [`USER`], contraseña: `password123`) y 5 facturas con sus líneas de detalle (asignando 2 facturas al cliente #1).
- **Configuración JWT (`application.properties`)**:
  - `jwt.secret`: `${JWT_SECRET}` (Clave secreta hexadecimal HMAC-SHA)
  - `jwt.expiration`: `${JWT_EXPIRATION}` (ej. `86400000` ms / 24 horas)
  - `jwt.cookie-name`: `${JWT_COOKIE}` (ej. `jwt_token`, `HttpOnly`)

## 3. Estructura de Paquetes y Arquitectura
Paquete base: `com.andres.course.agy.springboot.app`

- **`controllers`**: Controladores REST (`@RestController`) que operan exclusivamente con DTOs y `@Valid` (`AuthController`, `CustomerController`, `InvoiceController`, `UserController`).
- **`services`**: Lógica de negocio (`@Service`) con inyección por constructor (`CustomerService`/`CustomerServiceImpl`, `InvoiceService`/`InvoiceServiceImpl`, `UserService`/`UserServiceImpl` que implementa también `UserDetailsService`).
- **`repositories`**: Interfaces `JpaRepository` (`CustomerRepository`, `InvoiceRepository` con `@EntityGraph(attributePaths = {"customer", "items"})`, `UserRepository`).
- **`models`**: Entidades JPA con auditoría automática (`createdAt`, `updatedAt`).
  - `Customer` (tabla `customers`: `id`, `firstName`, `lastName`, `email`, `phone`, `address`).
  - `Invoice` (tabla `invoices`: `id`, `customer` `@ManyToOne`, `description`, `amount`, `status` [`InvoiceStatus`], `items` `@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)`).
  - `InvoiceItem` (tabla `invoice_items`: `id`, `invoice` `@ManyToOne`, `description`, `quantity`, `unitPrice`, `subtotal`).
  - `InvoiceStatus` (enum: `PENDING`, `PAID`, `CANCELLED`, `OVERDUE`).
  - `User` (tabla `users`: `id`, `email`, `password` BCrypt y `role`).
  - `Role` (enum: `USER`, `ADMIN`).
- **`dto`**: Implementados como `record` de Java sin exponer contraseñas ni auditoría en respuestas (`CustomerDto`, `InvoiceDto`, `InvoiceItemDto`, `UserDto`, `UserRequestDto`, `AuthRequestDto`).
- **`mappers`**: Conversión explícita Entidad <-> DTO (`CustomerMapper`, `InvoiceMapper`, `UserMapper`).
- **`config`**: Configuración general de la aplicación, seguridad HTTP stateless (`SecurityConfig`) e inicialización programática de datos (`DataInitializer`).
- **`security`**: Utilidades y filtros para autenticación JWT (`JwtService`, `JwtAuthenticationFilter`).
- **`exceptions`**: Manejo centralizado con `@RestControllerAdvice` (`GlobalExceptionHandler`, `ErrorResponse`, `CustomerNotFoundException`, `InvoiceNotFoundException`, `UserNotFoundException`, `EmailAlreadyExistsException`).

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
| `GET` | `/api/customers/{id}/invoices` | `USER`, `ADMIN` | *(Ninguno)* | `200 OK` (`List<InvoiceDto>`) | Obtiene todas las facturas de un cliente por su ID (`404` si el cliente no existe). |
| `POST` | `/api/customers` | `ADMIN` | `CustomerDto` (`firstName`, `lastName`, `email`, `phone`, `address`) | `201 Created` (`CustomerDto`) | Crea un nuevo cliente (`400` si el email ya existe o datos inválidos). |
| `PUT` | `/api/customers/{id}` | `ADMIN` | `CustomerDto` (`firstName`, `lastName`, `email`, `phone`, `address`) | `200 OK` (`CustomerDto`) | Actualiza un cliente existente por su ID (`404` si no existe). |
| `DELETE` | `/api/customers/{id}` | `ADMIN` | *(Ninguno)* | `204 No Content` | Elimina un cliente por su ID (`404` si no existe). |

### 4.3. Facturas (`/api/invoices`) - `InvoiceController`
| Método | Ruta | Acceso | Request Body | Respuesta Exitosa | Descripción |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/invoices` | `USER`, `ADMIN` | *(Ninguno)* | `200 OK` (`List<InvoiceDto>`) | Lista todas las facturas o filtra por `?customerId={id}`. |
| `GET` | `/api/invoices/{id}` | `USER`, `ADMIN` | *(Ninguno)* | `200 OK` (`InvoiceDto`) | Obtiene una factura con sus líneas (`items`) por su ID (`404` si no existe). |
| `POST` | `/api/invoices` | `ADMIN` | `InvoiceDto` (`customerId`, `description`, `status`, `items`) | `201 Created` (`InvoiceDto`) | Crea una factura para un cliente calculando automáticamente `subtotal` por ítem y `amount` total. |
| `PUT` | `/api/invoices/{id}` | `ADMIN` | `InvoiceDto` (`customerId`, `description`, `status`, `items`) | `200 OK` (`InvoiceDto`) | Actualiza cliente, descripción, estado y líneas de una factura existente (`404` si no existe). |
| `DELETE` | `/api/invoices/{id}` | `ADMIN` | *(Ninguno)* | `204 No Content` | Elimina una factura y sus ítems por su ID (`404` si no existe). |

### 4.4. Usuarios (`/api/users`) - `UserController`
| Método | Ruta | Acceso | Request Body | Respuesta Exitosa | Descripción |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/users` | `ADMIN` | *(Ninguno)* | `200 OK` (`List<UserDto>`) | Lista todos los usuarios registrados (sin exponer contraseñas). |
| `GET` | `/api/users/{id}` | `ADMIN` | *(Ninguno)* | `200 OK` (`UserDto`) | Obtiene un usuario por su ID (`404` si no existe). |
| `POST` | `/api/users` | `ADMIN` | `UserRequestDto` (`email`, `password`, `role`) | `201 Created` (`UserDto`) | Crea un usuario con contraseña encriptada en BCrypt. |
| `PUT` | `/api/users/{id}` | `ADMIN` | `UserRequestDto` (`email`, `password`, `role`) | `200 OK` (`UserDto`) | Actualiza email, contraseña y rol de un usuario existente. |
| `DELETE` | `/api/users/{id}` | `ADMIN` | *(Ninguno)* | `204 No Content` | Elimina un usuario por su ID (`404` si no existe). |

### 4.5. Endpoints Técnicos y de Desarrollo
| Método | Ruta | Acceso | Descripción |
| :--- | :--- | :--- | :--- |
| `GET`/`POST` | `/h2-console/**` | Público | Consola web de la base de datos en memoria H2. |
| `GET` | `/actuator/**` | Público | Endpoints de monitorización y estado de Spring Boot Actuator (ej. `/actuator/health`). |

## 5. Reglas y Convenciones de Desarrollo
1. **Configuración**: Usar exclusivamente `application.properties` (no usar archivos `.yml` / `.yaml`) y externalizar credenciales/secretos en `.env` (ignorado en Git).
2. **Desacoplamiento**: Los controladores solo interactúan con `dto`. La conversión DTO <-> Entidad se realiza en `services` mediante `mappers`.
3. **DTOs**: Definir siempre como `record`, excluyendo campos sensibles (`password` en respuestas) y de auditoría (`createdAt`, `updatedAt`).
4. **Inyección de Dependencias**: Utilizar siempre inyección por constructor (sin `@Autowired` en campos).
5. **Testing**: Pruebas unitarias y de integración en `src/test/java` (`ApplicationTests`, `CustomerRepositoryTests`, `CustomerServiceImplTests`, `InvoiceServiceImplTests`, `UserServiceImplTests`, `SecurityIntegrationTests`, `DefaultUsersImportTests`).

## 6. Skills y Servidores MCP del Proyecto
- **Skills (`.agents/skills/` y `skills-lock.json`)**:
  - **`spring-boot-best-practices`** (`.agents/skills/spring-boot-best-practices/SKILL.md`): Estándares de arquitectura en capas para Spring Boot (`controllers`, `services`, `repositories`, `models`, `dto`, `mappers`), uso de `record` en DTOs, inyección por constructor y configuración en `application.properties`.
  - **`supabase`** (`.agents/skills/supabase/SKILL.md`): Soporte integral para el ecosistema Supabase (Database, Auth, sesiones/JWT, CLI/MCP, configuración de conexión y resolución de errores).
  - **`supabase-postgres-best-practices`** (`.agents/skills/supabase-postgres-best-practices/SKILL.md`): Buenas prácticas de PostgreSQL mantenidas por Supabase (diseño de esquemas y tipos de datos, índices, optimización de consultas, gestión de conexiones/pooling, migraciones y políticas de seguridad RLS).
  - **`skill-creator`** (`.agents/skills/skill-creator/SKILL.md`): Creación, mejora y evaluación de nuevas skills para el agente.
- **Servidores MCP (`mcp_config.json`)**:
  - **`supabase`**: Gestión de tablas, migraciones, consultas SQL y diagnóstico del proyecto Supabase (`jjqmxfexxbxtenmofjmu`).
  - **`chrome-devtools`**: Pruebas end-to-end e inspección automatizada del navegador (`http://127.0.0.1:9222`).

## 7. Comandos Frecuentes (Maven Wrapper)
- **Ejecutar la aplicación**: `./mvnw -DskipTests spring-boot:run`
- **Ejecutar pruebas**: `./mvnw test`
- **Compilar y empaquetar**: `./mvnw clean package`
