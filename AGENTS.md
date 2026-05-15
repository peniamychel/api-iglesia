# AGENTS.md — web-iglesia (API Iglesia)

## Stack
- **Spring Boot 3.0.6**, Java 17, Maven
- **MariaDB** (principal), driver `mariadb-java-client:3.1.4`
- **JPA/Hibernate** con `spring-boot-starter-data-jpa`
- **Spring Security** + **JJWT 0.11.5** para autenticación JWT
- **Lombok**, **ModelMapper 3.0.0**, **Springdoc OpenAPI 2.6.0** (Swagger)
- **Spring Dotenv** para `.env`

## Comandos
```bash
# Build (sin tests)
mvnw.cmd clean package -DskipTests

# Build con tests
mvnw.cmd clean package

# Ejecutar en dev
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev

# Ejecutar en prod
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=prod

# Tests
mvnw.cmd test

# Docker
docker-compose up --build
```

## Proyecto
- **Puerto:** 8092
- **Swagger:** http://localhost:8092/swagger-ui.html
- **DB:** `jdbc:mariadb://localhost:3307/iglev3` (root/root)
- **Uploads:** `./uploads/` (máx 10 MB)
- **Zona horaria:** America/La_Paz
- **Token JWT:** expira en 24h

## Estructura
```
controller/    → REST endpoints
service/       → interfaces + impl/
model/
  dao/         → interfaces JPA Repository
  dto/         → DTOs agrupados por entidad
  entity/      → Entidades JPA
  payload/     → ApiResponse, etc.
security/
  configuration/ → SecurityConfig, CORS, Swagger, FileConfig
  filters/       → JwtAuthenticationFilter, JwtAuthorizationFilter
  jwt/           → JwtUtils
exception/       → GlobalExceptionHandler + excepciones personalizadas
```

## Convenciones
- **DTOs por entidad:** cada carpeta en `dto/` agrupa DTOs de creación, respuesta, edición
- **Servicios:** interfaz en `service/`, implementación en `service/impl/`
- **Repositorios:** interfaz que extiende `JpaRepository` en `model/dao/`
- **Entidades:** anotaciones JPA + Lombok en `model/entity/`
- **Controladores:** anotados con `@RestController`, `@RequestMapping("/api/v1/...")`
- **Validación:** `jakarta.validation` anotaciones en DTOs
- **Manejo de errores:** `GlobalExceptionHandler` con `@ControllerAdvice`
- **Respuestas:** `ApiResponse<T>` genérico desde `model/payload/`
- **Mapeo:** ModelMapper para convertir Entity ↔ DTO
- **Logging:** SLF4J vía Lombok `@Slf4j`
- **Perfiles:** `dev` (propiedades locales), `prod` (variables de entorno)
