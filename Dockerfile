# Etapa 1: Compilación y empaquetado (Build Stage)
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Copiar el wrapper de Maven y el descriptor del proyecto
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Asegurar formato Unix (LF) y permisos de ejecución para mvnw
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

# Descargar dependencias para aprovechar la caché de capas de Docker
RUN ./mvnw dependency:resolve -B

# Copiar código fuente y compilar el artefacto (.jar)
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Etapa 2: Imagen de ejecución (Runtime Stage)
FROM eclipse-temurin:25-jdk
WORKDIR /app

# Crear un usuario sin privilegios por seguridad
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

# Copiar el JAR resultante desde la etapa de compilación
COPY --from=build /app/target/*.jar app.jar

# Puerto estándar de Spring Boot
EXPOSE 8080

# Ejecución del contenedor
ENTRYPOINT ["java", "-jar", "app.jar"]
