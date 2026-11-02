# AGENTS.md - Resumen del Proyecto `3-spring-api`

## 1. Descripción General
API RESTful construida con **Spring Boot** para la gestión integral de clientes (`Customer`). Implementa una arquitectura en capas limpia con separación estricta entre entidades de persistencia y objetos de transferencia de datos (DTOs).

## 2. Stack Tecnológico y Configuración
- **Java**: 25
- **Spring Boot**: 4.1.0 (`spring-boot-starter-parent`)
- **Gestor de Dependencias**: Maven (uso obligatorio de Maven Wrapper `./mvnw` / `./mvnw.cmd`)
- **Dependencias Principales**:
  - `spring-boot-starter-web` (API REST)
  - `spring-boot-starter-data-jpa` (Persistencia con Hibernate)
  - `spring-boot-starter-validation` (Validación con Jakarta Validation)
  - `spring-boot-starter-actuator` (Monitorización)
  - `com.h2database:h2` (Base de datos en memoria para desarrollo/pruebas)
  - `spring-boot-devtools` (Recarga en caliente)
- **Base de Datos (H2)**:
  - URL: `jdbc:h2:mem:testdb` | Usuario: `sa` | Contraseña: *(vacía)*
  - Consola H2 habilitada (`spring.h2.console.enabled=true`)
  - Estrategia DDL: `update`

## 3. Estructura de Paquetes y Arquitectura
Paquete base: `com.andres.course.agy.springboot.app`

- **`controllers`**: Controladores REST (`@RestController`). Reciben y devuelven únicamente DTOs envueltos en `ResponseEntity`, validando entradas con `@Valid`.
  - `CustomerController`: Expone el CRUD en `/api/customers` (`GET`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}`).
- **`services`**: Lógica de negocio (`@Service`) con inyección por constructor. Orquesta repositorios y mappers.
  - `CustomerService` (interfaz) y `CustomerServiceImpl` (implementación).
- **`repositories`**: Interfaces de acceso a datos que extienden `JpaRepository`.
  - `CustomerRepository`: Incluye búsqueda por email y validación de duplicados.
- **`models`**: Entidades JPA (`@Entity`, `@Table`).
  - `Customer`: Tabla `customers` con campos `id`, `firstName`, `lastName`, `email` (único), `phone`, `address` y auditoría automática (`createdAt`, `updatedAt` con `@PrePersist` y `@PreUpdate`).
- **`dto`**: Objetos de transferencia implementados como `record` de Java.
  - `CustomerDto`: Incluye validaciones Jakarta (`@NotBlank`, `@Size`, `@Email`) y excluye campos de auditoría.
- **`mappers`**: Componentes Spring (`@Component`) para conversión explícita entre capas.
  - `CustomerMapper`: Métodos `entityToDto` y `dtoToEntity`.
- **`exceptions`**: Manejo centralizado de errores con `@RestControllerAdvice`.
  - `GlobalExceptionHandler`, `ErrorResponse`, `CustomerNotFoundException` (404), `EmailAlreadyExistsException` (400).

## 4. Reglas y Convenciones de Desarrollo
1. **Configuración**: Usar exclusivamente `application.properties` (no usar archivos `.yml` / `.yaml`).
2. **Desacoplamiento**: Los controladores nunca deben interactuar con entidades JPA (`models`), solo con `dto`. La conversión DTO <-> Entidad se realiza en la capa `services` mediante `mappers`.
3. **DTOs**: Definir siempre como `record`, sin campos sensibles ni de auditoría (`createdAt`, `updatedAt`).
4. **Inyección de Dependencias**: Utilizar siempre inyección por constructor (sin `@Autowired` en campos).
5. **Testing**: Pruebas unitarias y de integración ubicadas en `src/test/java` (`CustomerRepositoryTests`, `CustomerServiceImplTests`, `ApplicationTests`).

## 5. Comandos Frecuentes (Maven Wrapper)
- **Ejecutar la aplicación**: `./mvnw -DskipTests spring-boot:run`
- **Ejecutar pruebas**: `./mvnw test`
- **Compilar y empaquetar**: `./mvnw clean package`
