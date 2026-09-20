# Servicio Matrícula - Microservicio

Microservicio encargado de la administración de estudiantes, su información personal y los cursos en los que están matriculados.

## Información General
- **Puerto local:** `8083`
- **Base de Datos:** PostgreSQL

## Stack Tecnológico
- Spring Boot 3.x
- Java 17+
- Spring Data JPA
- **RabbitMQ:** Mensajería asíncrona para recibir eventos de nuevos usuarios.
- PostgreSQL
- Build Tool: Maven

## Ejecución y Pruebas Locales

### Instalación y Ejecución
1. Comprueba la conexión a la base de datos PostgreSQL en tu `application.properties`.
2. Desde una terminal, sitúate en la carpeta `servicio-matricula`.
3. Levanta la aplicación ejecutando:
   ```cmd
   .\mvnw.cmd spring-boot:run
   ```
4. El servicio quedará expuesto en el puerto `8083`.

### Pruebas de Endpoints
Las llamadas pueden hacerse al puerto `8083` directamente o al Gateway `9090`.

**Gestión de Matrícula/Estudiantes:**
- `GET /api/matricula/estudiantes`: Listar todos los estudiantes del colegio.
- `GET /api/matricula/estudiantes/curso/{curso}`: Listar estudiantes filtrados por curso (Ej: `1A`).
- `POST /api/matricula/registrar-completo`: Registrar una matrícula completa (Body JSON requiere `nombre`, `rut`, `fechaNacimiento`, `curso`, `usuarioId`, `estado`).

### Pruebas Unitarias
Para ejecutar las pruebas del proyecto:
```cmd
.\mvnw.cmd test
```
