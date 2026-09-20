# Servicio Asistencia - Microservicio

Microservicio especializado en gestionar el registro, consulta y eliminación de la asistencia diaria de los estudiantes.

## Información General
- **Puerto local:** `8082`
- **Base de Datos:** PostgreSQL

## Stack Tecnológico
- Spring Boot 3.x
- Java 17+
- Spring Data JPA
- PostgreSQL
- **Resilience4j:** Circuit Breaker aplicado en la validación externa de usuarios.
- Build Tool: Maven

## Ejecución y Pruebas Locales

### Instalación y Ejecución
1. Verifica que la base de datos PostgreSQL esté activa y el `application.properties` tenga las credenciales correctas.
2. Abre una terminal en el directorio `servicio-asistencia`.
3. Inicia el microservicio:
   ```cmd
   .\mvnw.cmd spring-boot:run
   ```
4. La aplicación iniciará en el puerto `8082`.

### Pruebas de Endpoints
Puedes enviar peticiones a `http://localhost:8082` (o a través del Gateway `http://localhost:9090`).

**Gestión de Asistencia:**
- `GET /api/asistencia`: Listar todas las asistencias.
- `GET /api/asistencia/{id}`: Obtener una asistencia por su ID.
- `POST /api/asistencia`: Crear una asistencia manual (Body JSON requiere `usuarioId`, `nombreUsuario`, `fecha`).
- `POST /api/asistencia/registrar`: Registrar asistencia de manera automática. Valida que el usuario exista en el servicio de usuarios y registra la hora actual.
- `DELETE /api/asistencia/{id}`: Eliminar el registro de asistencia.

## Resiliencia
Este servicio depende del **Servicio de Usuarios** para validar la existencia de estudiantes. Se ha implementado un Circuit Breaker en el cliente HTTP para manejar caídas del servicio de identidad sin bloquear el registro de asistencia.
### Pruebas Unitarias
Para correr la batería de pruebas (con Mockito):
```cmd
.\mvnw.cmd test
```
