# 🏫 Colegio BO - Plataforma de Gestión Escolar (Backend)

Este repositorio contiene la arquitectura de microservicios para la plataforma de gestión escolar del **Colegio BO**. El sistema está diseñado bajo una arquitectura orientada a servicios (SOA) y desacoplada, utilizando **Spring Cloud Gateway**, **RabbitMQ** para mensajería asíncrona, **WebClient** para comunicación síncrona, y bases de datos **PostgreSQL** independientes para cada microservicio.

---

## 📋 Información General y Puertos

| Microservicio / Componente | Puerto | Base URL Directa |
| :--- | :---: | :--- |
| **API Gateway** (Punto de Entrada Único) | `9090` | `http://localhost:9090` |
| **Servicio Usuarios** (Cuentas y Auth) | `8081` | `http://localhost:8081` |
| **Servicio Asistencia** (Control Diario) | `8082` | `http://localhost:8082` |
| **Servicio Matrícula** (Estudiantes/Fichas) | `8083` | `http://localhost:8083` |
| **Servicio Académico** (Cursos/Notas) | `8084` | `http://localhost:8084` |
| **Servicio Reportes** (Consolidación/Conducta) | `8085` | `http://localhost:8085` |
| **Servicio Mensajería** (Chat RabbitMQ) | `8086` | `http://localhost:8086` |
| **SonarQube** (Calidad de Código) | `9000` | `http://localhost:9000` |

> [!IMPORTANT]
> Todas las peticiones del Frontend o clientes HTTP deben dirigirse a través del **API Gateway (puerto 9090)** utilizando el prefijo de ruta `/api/`.

---

## 🔐 Autenticación y Cuentas

### 1. Registrar Nuevo Usuario
Crea las credenciales de inicio de sesión de un usuario y le asigna un rol inicial.
* **Endpoint:** `POST /api/auth/register`
* **Cuerpo de la Petición (JSON):**
```json
{
  "correo": "usuario@ejemplo.com",
  "password": "Password123#",
  "rol": "Alumno",
  "telefono": "+56912345678"
}
```
> [!NOTE]
> La contraseña debe tener como mínimo 8 caracteres, incluyendo una mayúscula, una minúscula, un número y un carácter especial.

---

### 2. Login (Obtener Token JWT)
Autentica al usuario y retorna un token JWT firmado que autoriza las peticiones protegidas.
* **Endpoint:** `POST /api/auth/login`
* **Cuerpo de la Petición (JSON):**
```json
{
  "correo": "usuario@ejemplo.com",
  "password": "Password123#"
}
```

---

## 👥 Gestión de Usuarios (Rol: Admin)

### 1. Listar Todos los Usuarios
* **Endpoint:** `GET /api/usuarios`

### 2. Obtener Usuario por ID
* **Endpoint:** `GET /api/usuarios/{id}`
* **Ejemplo:** `GET /api/usuarios/1`

### 3. Crear Nuevo Usuario (Directo)
* **Endpoint:** `POST /api/usuarios`
* **Cuerpo de la Petición (JSON):**
```json
{
  "correo": "usuario@ejemplo.com",
  "password": "Password123#",
  "rol": "Profesor"
}
```

### 4. Actualizar Usuario
Modifica los datos del usuario, su rol y su vinculación académica.
* **Endpoint:** `PUT /api/usuarios/{id}`
* **Cuerpo de la Petición (JSON):**
```json
{
  "correo": "usuario@ejemplo.com",
  "rol": "Profesor",
  "telefono": "+56998765432",
  "cursoId": 2
}
```

### 5. Eliminar Usuario
Elimina al usuario del sistema y propaga la eliminación en cascada a las fichas de los demás servicios.
* **Endpoint:** `DELETE /api/usuarios/{id}`

---

## 📅 Gestión de Asistencia

### 1. Listar Todas las Asistencias
* **Endpoint:** `GET /api/asistencia`

### 2. Obtener Asistencia por ID
* **Endpoint:** `GET /api/asistencia/{id}`

### 3. Crear Asistencia Manual
* **Endpoint:** `POST /api/asistencia`
* **Cuerpo de la Petición (JSON):**
```json
{
  "usuarioId": 1,
  "nombreUsuario": "alumno@gmail.com",
  "fecha": "2026-06-15T09:00:00"
}
```

### 4. Registrar Asistencia (Automático)
Registra la marca del estudiante en la base de datos usando la hora actual del servidor.
* **Endpoint:** `POST /api/asistencia/registrar`
* **Cuerpo de la Petición (JSON):**
```json
{
  "usuarioId": 1
}
```

### 5. Eliminar Registro de Asistencia
* **Endpoint:** `DELETE /api/asistencia/{id}`

---

## 📝 Gestión de Matrícula y Estudiantes

### 1. Listar Todos los Estudiantes
* **Endpoint:** `GET /api/matricula/estudiantes`

### 2. Buscar Estudiante por RUT
* **Endpoint:** `GET /api/matricula/estudiantes/rut/{rut}`

### 3. Registrar Matrícula Completa
Crea el perfil del alumno, asocia o guarda los datos de su apoderado correspondiente y sincroniza la matrícula en el servicio académico.
* **Endpoint:** `POST /api/matricula/registrar-completo`
* **Cuerpo de la Petición (JSON):**
```json
{
  "nombre": "Diego Rodríguez",
  "rut": "12345678-9",
  "fechaNacimiento": "2012-05-15",
  "cursoId": 1,
  "usuarioId": 3,
  "estado": "Activo",
  "apoderado": {
    "rut": "9876543-2",
    "nombre": "María González",
    "telefono": "987654321",
    "correo": "maria.apoderado@gmail.com",
    "usuarioId": 4
  }
}
```

---

## 🎓 Servicio Académico (Cursos y Notas)

### 1. Listar Cursos Disponibles
* **Endpoint:** `GET /api/academico/cursos`

### 2. Crear Nuevo Curso
* **Endpoint:** `POST /api/academico/cursos`
* **Cuerpo de la Petición (JSON):**
```json
{
  "nombre": "1 Medio A",
  "codigo": "CUR-1MA",
  "descripcion": "Primero Medio A"
}
```
> [!TIP]
> Al crearse el curso, el sistema inicializa automáticamente las asignaturas de inglés, lenguaje, matemática e historia.

### 3. Asignar Profesor Jefe a un Curso
* **Endpoint:** `PUT /api/academico/cursos/{id}/profesor`
* **Cuerpo de la Petición (JSON):**
```json
{
  "profesorId": 2
}
```

### 4. Asignar Profesor a Asignatura del Curso
* **Endpoint:** `PUT /api/academico/cursos/{id}/asignaturas/profesor`
* **Cuerpo de la Petición (JSON):**
```json
{
  "asignatura": "ingles 1",
  "profesorId": 2
}
```

### 5. Crear una Evaluación (Pruebas)
* **Endpoint:** `POST /api/academico/pruebas/curso/{cursoId}`
* **Cuerpo de la Petición (JSON):**
```json
{
  "titulo": "Prueba 1 Álgebra",
  "asignatura": "matematica 1",
  "ponderacion": 25.0
}
```

### 6. Registrar Nota de Alumno
Registra la calificación de un alumno para una prueba específica. El valor debe estar entre `1.0` y `7.0`.
* **Endpoint:** `POST /api/academico/notas/prueba/{pruebaId}`
* **Cuerpo de la Petición (JSON):**
```json
{
  "alumnoId": 3,
  "valor": 6.5,
  "comentario": "Excelente rendimiento escolar."
}
```

---

## 📊 Servicio de Reportes (Consolidación y Conducta)

### 1. Registrar Reporte de Comportamiento (Profesor)
Crea anotaciones conductuales sobre la actitud de un alumno.
* **Endpoint:** `POST /api/reportes/comportamiento`
* **Cuerpo de la Petición (JSON):**
```json
{
  "alumnoId": 3,
  "tipo": "POSITIVO",
  "descripcion": "Participa activamente en clases y ayuda a sus compañeros.",
  "profesorId": 2
}
```

### 2. Obtener Reporte Consolidado Completo
Combina la información del alumno, recuperando síncronamente sus notas del *Servicio Académico*, su asistencia del *Servicio Asistencia*, y su historial de conducta local.
* **Endpoint:** `GET /api/reportes/completo/alumno/{alumnoId}`

---

## 💬 Servicio de Mensajería (RabbitMQ)

### 1. Enviar Mensaje (Profesor <-> Apoderado)
Valida la autorización de roles y encola un mensaje asíncrono en RabbitMQ.
* **Endpoint:** `POST /api/mensajes/enviar`
* **Cuerpo de la Petición (JSON):**
```json
{
  "remitenteId": 2,
  "destinatarioId": 4,
  "contenido": "Estimado, solicito reunión para conversar sobre el desempeño de su pupilo."
}
```

### 2. Obtener Historial de Chat
Recupera el historial de chat de forma cronológica entre dos usuarios.
* **Endpoint:** `GET /api/mensajes/historial?user1={id1}&user2={id2}`

### 3. Listar Contactos Permitidos
* **Endpoint:** `GET /api/mensajes/contactos/{userId}`

---

## 🛠️ Stack Tecnológico

- **Backend Core:** Java 21 & Spring Boot 3.x
- **Mensajería Asíncrona:** RabbitMQ (Colas AMQP)
- **Base de Datos:** PostgreSQL (Base de datos independiente por cada microservicio)
- **API Gateway:** Spring Cloud Gateway (con Resilience4j Circuit Breakers)
- **Calidad de Código y Pruebas:** SonarQube & JaCoCo (XML test coverage)
- **Herramienta de Construcción:** Maven

---

## 🚀 Guía Rápida de Ejecución Local

### 1. Levantar Servicios Auxiliares (BBDD y RabbitMQ)
Antes de ejecutar los microservicios, asegúrate de levantar PostgreSQL y RabbitMQ mediante Docker:
```cmd
docker compose up -d postgres-db rabbitmq
```

### 2. Compilar y Levantar Todo en Docker
Si prefieres correr toda la aplicación contenedorizada:
```cmd
docker compose build
docker compose up -d
```

### 3. Ejecutar Pruebas y Análisis SonarQube (CMD)
```cmd
set JAVA_HOME=C:\Users\vicho\.jdk\jdk-21.0.8
mvnw.cmd clean verify sonar:sonar -Dsonar.host.url=http://localhost:9000 -Dsonar.token=TU_TOKEN_DE_SONAR
```

---

**Última actualización:** Junio de 2026
