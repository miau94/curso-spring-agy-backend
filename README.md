# Curso Spring Boot + Antigravity AI — Backend (`3-spring-api`)

API RESTful empresarial desarrollada con **Java 25** y **Spring Boot 4.1.0** para la gestión integral de **Clientes (`Customer`)**, **Facturación (`Invoice` e `InvoiceItem`)** y **Usuarios (`User`)**. Implementa una arquitectura en capas desacoplada mediante DTOs (`record`), validación declarativa (`@Valid`), persistencia en **PostgreSQL / Supabase** con **Spring Data JPA** y seguridad stateless con **Spring Security + JWT en cookies `HttpOnly`**.

---

## Características Principales

- **Autenticación y Autorización Stateless (RBAC)**:
  - Emisión y validación de tokens **JWT** firmados con HMAC-SHA almacenados en cookies `HttpOnly` (`jwt_token`).
  - Control de acceso basado en roles (`ADMIN` y `USER`).
  - Encriptación de contraseñas con **BCrypt**.
- **Módulo de Clientes (`/api/customers`)**:
  - Directorio completo de clientes con validación de unicidad de email y consulta de facturas asociadas (`GET /api/customers/{id}/invoices`).
- **Sistema de Facturación (`/api/invoices`)**:
  - Gestión de facturas asociadas a clientes con estado (`PENDING`, `PAID`, `OVERDUE`, `CANCELLED`) y líneas de detalle (`InvoiceItem`).
  - Cálculo automático en servidor del `subtotal` por línea (`quantity * unitPrice`) y del importe total (`amount`) de la factura.
  - Consultas optimizadas mediante `@EntityGraph(attributePaths = {"customer", "items"})` para evitar el problema *N+1*.
- **Administración de Usuarios (`/api/users`)**:
  - Gestión completa de cuentas y roles restringida exclusivamente al rol `ADMIN`.
- **Inicialización Automática de Datos (`DataInitializer`)**:
  - Carga automática de 10 clientes, 2 usuarios de prueba (`ADMIN` y `USER`) y 5 facturas con sus líneas de detalle si la base de datos está vacía al arrancar.

---

## Stack Tecnológico

| Tecnología | Versión / Detalle |
| :--- | :--- |
| **Java** | 25 |
| **Spring Boot** | 4.1.0 (`spring-boot-starter-parent`) |
| **Spring Data JPA** | Hibernate / Jakarta Persistence (`PostgreSQLDialect` y `H2Dialect` en tests) |
| **Spring Security** | Autenticación JWT Stateless + `BCryptPasswordEncoder` |
| **JJWT** | `io.jsonwebtoken:jjwt-*` `0.12.6` |
| **Base de Datos Principal** | PostgreSQL / Supabase (Session Pooler IPv4) |
| **Base de Datos de Testing** | H2 Database en memoria (`jdbc:h2:mem:testdb`) |
| **Monitorización** | Spring Boot Actuator (`/actuator/health`) |
| **Construcción** | Maven Wrapper (`./mvnw` / `.\mvnw.cmd`) |

---

## Arquitectura del Proyecto

Paquete raíz: `com.andres.course.agy.springboot.app`

```text
src/main/java/com/andres/course/agy/springboot/app/
├── config/          # SecurityConfig, DataInitializer
├── controllers/     # AuthController, CustomerController, InvoiceController, UserController
├── dto/             # Java records (AuthRequestDto, CustomerDto, InvoiceDto, InvoiceItemDto, UserDto, UserRequestDto)
├── exceptions/      # GlobalExceptionHandler (@RestControllerAdvice) y excepciones de dominio
├── mappers/         # Conversión explícita Entidad <-> DTO (CustomerMapper, InvoiceMapper, UserMapper)
├── models/          # Entidades JPA (Customer, Invoice, InvoiceItem, InvoiceStatus, User, Role)
├── repositories/    # Interfaces JpaRepository (CustomerRepository, InvoiceRepository, UserRepository)
├── security/        # JwtService, JwtAuthenticationFilter
└── services/        # Interfaces e implementaciones de negocio (*Service, *ServiceImpl)
```

---

## Configuración de Variables de Entorno (`.env`)

El proyecto importa automáticamente el archivo `.env` ubicado en la raíz del proyecto mediante `spring.config.import=optional:file:.env[.properties]`.

Crea un archivo `.env` en la raíz con las siguientes variables:

```properties
# Conexión PostgreSQL / Supabase
SUPABASE_DB_URL=jdbc:postgresql://aws-1-eu-central-1.pooler.supabase.com:5432/postgres?sslmode=require
SUPABASE_DB_USER=postgres.tu_project_ref
SUPABASE_DB_PASS=tu_password_seguro

# Configuración JWT
JWT_SECRET=tu_clave_secreta_hexadecimal_de_al_menos_256_bits
JWT_EXPIRATION=86400000
JWT_COOKIE=jwt_token
```

---

## Puesta en Marcha y Comandos Maven

Usa siempre el **Maven Wrapper** incluido en el repositorio:

```bash
# Ejecutar la aplicación en modo desarrollo (http://localhost:8080)
./mvnw -DskipTests spring-boot:run

# Ejecutar la suite completa de pruebas unitarias y de integración (H2 en memoria)
./mvnw test

# Compilar y generar el artefacto ejecutable .jar
./mvnw clean package
```

*(En Windows PowerShell utiliza `.\mvnw.cmd` en lugar de `./mvnw`).*

---

## Credenciales de Prueba por Defecto

Al iniciar la aplicación con una base de datos vacía, `DataInitializer` crea los siguientes usuarios:

| Rol | Email | Contraseña | Permisos |
| :--- | :--- | :--- | :--- |
| **`ADMIN`** | `admin@example.com` | `password123` | CRUD completo en Clientes, Facturas y Usuarios |
| **`USER`** | `user@example.com` | `password123` | Lectura de Clientes y Facturas (`GET`) |

---

## Endpoints Principales

### Autenticación (`/api/auth`)
- `POST /api/auth/login` — Inicia sesión y emite la cookie `jwt_token` (`HttpOnly`).
- `POST /api/auth/register` — Registra un nuevo usuario con rol `USER`.
- `POST /api/auth/logout` — Elimina la cookie de sesión.

### Clientes (`/api/customers`)
- `GET /api/customers` — Lista todos los clientes (`USER`, `ADMIN`).
- `GET /api/customers/{id}` — Obtiene el detalle de un cliente (`USER`, `ADMIN`).
- `GET /api/customers/{id}/invoices` — Lista las facturas de un cliente específico (`USER`, `ADMIN`).
- `POST /api/customers` — Crea un cliente (`ADMIN`).
- `PUT /api/customers/{id}` — Actualiza un cliente (`ADMIN`).
- `DELETE /api/customers/{id}` — Elimina un cliente (`ADMIN`).

### Facturas (`/api/invoices`)
- `GET /api/invoices` — Lista todas las facturas o filtra por `?customerId={id}` (`USER`, `ADMIN`).
- `GET /api/invoices/{id}` — Obtiene una factura con sus líneas de detalle (`USER`, `ADMIN`).
- `POST /api/invoices` — Crea una nueva factura y calcula sus importes (`ADMIN`).
- `PUT /api/invoices/{id}` — Modifica una factura y sus líneas (`ADMIN`).
- `DELETE /api/invoices/{id}` — Elimina una factura y sus ítems asociados (`ADMIN`).

### Usuarios (`/api/users`)
- `GET /api/users` — Lista todos los usuarios (`ADMIN`).
- `GET /api/users/{id}` — Obtiene un usuario por ID (`ADMIN`).
- `POST /api/users` — Crea un usuario con rol específico (`ADMIN`).
- `PUT /api/users/{id}` — Actualiza email, contraseña o rol de un usuario (`ADMIN`).
- `DELETE /api/users/{id}` — Elimina un usuario (`ADMIN`).
