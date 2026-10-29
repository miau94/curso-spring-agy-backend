---
name: spring-boot-best-practices
description: Genera y gestiona proyectos con la estructura de Spring Boot con capas de controllers, services, repositories y models. Úsalo SIEMPRE que el usuario te pida crear una API básica de Spring Boot, un monolito de Spring Web, o cuando te pida crear, agregar o modificar un entity, repository, service, controller o mapper de Spring.
---

# Skill de Buenas Prácticas de Spring Boot

Esta skill guía la creación y evolución de aplicaciones de Spring Boot siguiendo estrictos estándares arquitectónicos, características modernas de Java y herramientas oficiales de inicialización.

## 1. Inicialización del Proyecto

Cuando se cree un nuevo proyecto de Spring Boot:
- **Herramienta**: Usa SIEMPRE la API oficial de Spring Initializr para generar el proyecto base. Ejecuta un comando `curl` para descargar el archivo zip y extraerlo:
  ```bash
  curl -G https://start.spring.io/starter.zip \
    -d type=maven-project \
    -d language=java \
    -d bootVersion=4.1.0 \
    -d javaVersion=25 \
    -d groupId=com.andres.course.agy.springboot \
    -d artifactId=<artifactId> \
    -d name=<artifactId> \
    -d baseDir=<artifactId> \
    -d packaging=jar \
    -d dependencies=web,validation,data-jpa,h2,devtools,actuator \
    -o project.zip
  ```
  *Nota*: El `<artifactId>` DEBE coincidir exactamente con el nombre del directorio workspace del proyecto.
- **Maven Wrapper**: Verifica que el zip generado contenga la estructura del Maven Wrapper:
  - Carpeta `.mvn/` con los binarios y configuración del wrapper.
  - Scripts ejecutables `./mvnw` (Linux/macOS) y `./mvnw.cmd` (Windows).
  - Si faltan, ejecuta `mvn wrapper:wrapper` para generarlos.
- **Paquete Base**: El paquete base de la aplicación DEBE ser:
  `com.andres.course.agy.springboot.<artifactId>.app`

## 2. Configuración y Propiedades

- **Formato de Archivo**: Usa configuración basada en properties (`src/main/resources/application.properties`). NO uses YAML (`.yaml` o `.yml`).
- **Base de Datos de Desarrollo**: Configura la base de datos en memoria H2 con las siguientes propiedades:
  ```properties
  spring.datasource.url=jdbc:h2:mem:testdb
  spring.datasource.driverClassName=org.h2.Driver
  spring.datasource.username=sa
  spring.datasource.password=
  spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
  spring.h2.console.enabled=true
  spring.jpa.hibernate.ddl-auto=update
  ```

## 3. Arquitectura en Capas

Estructura siempre los paquetes de la aplicación bajo el paquete base de la siguiente manera:
- `controllers`
- `services`
- `repositories`
- `models` (Entidades)
- `dto` (Objetos de Transferencia de Datos)
- `mappers`

Sigue estos requisitos específicos para cada capa:

### A. Models (Entidades)
- Anota con `@Entity` y `@Table`.
- Usa anotaciones de validación estándar de Jakarta (`@NotNull`, `@Size`, etc.).
- Incluye campos de auditoría estándar si es necesario: `createdAt`, `updatedAt` (o `createAt`, `updateAt`).

### B. Repositories
- Define los repositorios como interfaces.
- Extiende de `JpaRepository<NombreEntidad, TipoId>`.
- Colócalos en el paquete `repositories`.

### C. DTOs (Data Transfer Objects)
- Representa los DTOs como Java `record`s en lugar de clases (ejemplo: `public record UserDto(...) {}`).
- Excluye campos sensibles del DTO, tales como: `password`, `createdAt`, `updatedAt`, `createAt`, `updateAt`.
- Usa el mismo DTO para las peticiones (request) y las respuestas (response).
- Colócalos en el paquete `dto`.

### D. Mappers
- Implementa clases mapeadoras (ejemplo: `UserMapper`) en el paquete `mappers` para convertir explícitamente entre Entidades y DTOs.
- Proporciona métodos de mapeo explícitos:
  - `entityToDto(Entity entidad)` -> devuelve DTO
  - `dtoToEntity(Dto dto)` -> devuelve Entidad
- Ejemplo de formato:
  ```java
  @Component
  public class UserMapper {
      public UserDto entityToDto(User user) {
          return new UserDto(user.getId(), user.getUsername(), user.getEmail());
      }
      
      public User dtoToEntity(UserDto dto) {
          User user = new User();
          user.setId(dto.id());
          user.setUsername(dto.username());
          user.setEmail(dto.email());
          return user;
      }
  }
  ```

### E. Services
- Implementa la lógica de negocio en la capa de Servicio anotando con `@Service`.
- Inyecta repositorios y mapeadores usando inyección por constructor.
- Realiza todas las conversiones entre DTO y Entidad dentro de la capa de Servicio. Los servicios aceptan y devuelven DTOs, evitando que el controlador interactúe con entidades crudas.

### F. Controllers
- Anota los controladores REST con `@RestController` y `@RequestMapping`.
- Valida los cuerpos de las peticiones entrantes usando `@Valid`.
- Delega las operaciones a la capa de Servicio.
- Devuelve `ResponseEntity` conteniendo DTOs.

## 4. Capa de UI (Monolitos Spring Web con Thymeleaf)

Si se construyen interfaces de usuario o una aplicación web monolítica:
- **Motor de Plantillas**: Usa Thymeleaf para las plantillas HTML.
- **Estilos**: Integra Tailwind CSS.
- **Estructura del Diseño (Layout)**: Implementa un diseño base reutilizable con encabezado (header), barra de navegación (navbar), pie de página (footer) y contenido principal centrado. Utiliza etiquetas semánticas HTML: `<header>`, `<main>`, y `<footer>`.
- **Estructura de Carpetas de Plantillas**:
  - `src/main/resources/templates/layouts/base.html` (diseño principal)
  - `src/main/resources/templates/fragments/header.html`
  - `src/main/resources/templates/fragments/footer.html`
  - Vistas específicas del dominio en subcarpetas (ejemplo: `templates/users/list.html`).
- Usa la inserción básica de fragmentos de Thymeleaf (`th:replace`, `th:insert`) para reutilizar el diseño base.

## 5. Comando de Construcción y Ejecución

Cuando ejecutes la aplicación localmente mediante Maven, prefiere siempre el Maven Wrapper:
```bash
./mvnw -DskipTests spring-boot:run
```
Evita llamar directamente a comandos globales de `mvn`.
