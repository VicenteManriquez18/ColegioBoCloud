# Servicio Usuarios - Microservicio

Microservicio encargado de la gestión de usuarios y la autenticación (seguridad basada en JWT).

## Información General
- **Puerto local:** `8081`
- **Base de Datos:** PostgreSQL

## Stack Tecnológico
- Spring Boot 3.x
- Java 17+
- Spring Data JPA
- PostgreSQL
- **RabbitMQ:** Productor de eventos para sincronización interna con Matrícula.
- Autenticación: JWT
- Build Tool: Maven

## Ejecución y Pruebas Locales

### Instalación y Ejecución
1. Asegúrate de tener tu servidor de base de datos PostgreSQL corriendo y configurado correctamente en el archivo `application.properties`.
2. Abre una terminal (CMD/PowerShell) en la carpeta `servicio-usuarios`.
3. Ejecuta el comando para levantar el servicio:
   ```cmd
   .\mvnw.cmd spring-boot:run
   ```
4. El servicio levantará de manera independiente en el puerto `8081`.

### Pruebas de Endpoints
Para probar el microservicio de manera local (usando herramientas como Postman), puedes apuntar a `http://localhost:8081` (o a través del API Gateway en el puerto `9090`).

**Autenticación:**
- `POST /api/auth/register`: Registrar nuevo usuario (Requiere Body JSON: `correo`, `password`, `rol`).
- `POST /api/auth/login`: Login para obtener el token JWT.

**Gestión de Usuarios:**
- `GET /api/usuarios`: Listar todos los usuarios.
- `GET /api/usuarios/{id}`: Obtener usuario por ID.
- `POST /api/usuarios`: Crear nuevo usuario.
- `PUT /api/usuarios/{id}`: Actualizar usuario.
- `DELETE /api/usuarios/{id}`: Eliminar usuario.

### Pruebas Unitarias
Para ejecutar la suite de pruebas unitarias usando JUnit y Mockito:
```cmd
.\mvnw.cmd test
```
