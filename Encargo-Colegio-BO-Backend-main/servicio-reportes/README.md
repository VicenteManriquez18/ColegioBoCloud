# Servicio de Reportes

## Descripción

Microservicio que implementa una **arquitectura event-driven** para generar reportes en tiempo real consumiendo eventos de otros microservicios del sistema.

## Funcionalidades

### 📊 Reportes Disponibles

1. **Reportes de Matrícula**
   - Matrículas por alumno
   - Matrículas por curso
   - Contar alumnos activos por curso
   - Matrículas de alumno en curso específico

2. **Reportes de Notas**
   - Notas por alumno
   - Notas por prueba
   - Promedio de notas por alumno
   - Promedio de notas por prueba
   - Notas ordenadas

3. **Reportes de Asistencia**
   - Asistencias por alumno
   - Asistencias por curso
   - Total de asistencias/inasistencias
   - Porcentaje de asistencia

## Eventos que Consume

El servicio escucha los siguientes eventos desde RabbitMQ:

| Evento | Routing Key | Source |
|--------|------------|--------|
| `MatriculaRegistradaEvent` | `matricula.registrada` | servicio-matricula |
| `NotaGeneradaEvent` | `nota.generada` | servicio-academico |
| `AsistenciaRegistradaEvent` | `asistencia.registrada` | servicio-asistencia |

## Configuración

### Puerto
- **8085**

### Base de Datos
- PostgreSQL
- Base de datos: `reportes_db`

### RabbitMQ
- Host: localhost:5672
- Usuario: guest
- Contraseña: guest

## Endpoints

### Health Check
```
GET http://localhost:8085/api/reportes/health
GET http://localhost:8085/api/reportes/info
```

### Reportes de Matrícula
```
GET /api/reportes/matriculas/alumno/{alumnoId}
GET /api/reportes/matriculas/curso/{cursoId}
GET /api/reportes/matriculas/curso/{cursoId}/count-activos
GET /api/reportes/matriculas/alumno/{alumnoId}/curso/{cursoId}
```

### Reportes de Notas
```
GET /api/reportes/notas/alumno/{alumnoId}
GET /api/reportes/notas/prueba/{pruebaId}
GET /api/reportes/notas/alumno/{alumnoId}/promedio
GET /api/reportes/notas/prueba/{pruebaId}/promedio
GET /api/reportes/notas/alumno/{alumnoId}/ordenadas
```

### Reportes de Asistencia
```
GET /api/reportes/asistencias/alumno/{alumnoId}
GET /api/reportes/asistencias/curso/{cursoId}
GET /api/reportes/asistencias/alumno/{alumnoId}/total-asistencias
GET /api/reportes/asistencias/alumno/{alumnoId}/total-inasistencias
GET /api/reportes/asistencias/alumno/{alumnoId}/porcentaje-asistencia
```

## Swagger/OpenAPI

Acceder a la documentación interactiva:
```
http://localhost:8085/swagger-ui.html
```

## Estructura del Proyecto

```
servicio-reportes/
├── src/main/java/com/proyecto/reportes/
│   ├── config/
│   │   └── RabbitMqConfig.java
│   ├── controller/
│   │   ├── ReportesController.java
│   │   ├── ReporteMatriculaController.java
│   │   ├── ReporteNotaController.java
│   │   └── ReporteAsistenciaController.java
│   ├── entity/
│   │   ├── ReporteMatricula.java
│   │   ├── ReporteNota.java
│   │   └── ReporteAsistencia.java
│   ├── listener/
│   │   ├── MatriculaEventListener.java
│   │   ├── NotaEventListener.java
│   │   └── AsistenciaEventListener.java
│   ├── model/event/
│   │   ├── MatriculaRegistradaEvent.java
│   │   ├── NotaGeneradaEvent.java
│   │   └── AsistenciaRegistradaEvent.java
│   ├── repository/
│   │   ├── ReporteMatriculaRepository.java
│   │   ├── ReporteNotaRepository.java
│   │   └── ReporteAsistenciaRepository.java
│   ├── service/
│   │   ├── ReporteMatriculaService.java
│   │   ├── ReporteNotaService.java
│   │   └── ReporteAsistenciaService.java
│   └── ReportesApplication.java
├── src/main/resources/
│   └── application.properties
└── pom.xml
```

## Flujo de Eventos

```
┌─────────────────────────────────────────────────────────────┐
│ Otros Microservicios publican eventos en RabbitMQ           │
└────────────┬──────────────────────────────────────┬─────────┘
             │                                      │
      [matricula.registrada]              [nota.generada]
             │                                      │
      ┌──────▼──────────────────────────────────────▼───┐
      │ Servicio de Reportes (Event-Driven)             │
      │ ┌─────────────────────────────────────────────┐ │
      │ │ Listeners (consume eventos)                 │ │
      │ │ MatriculaEventListener                      │ │
      │ │ NotaEventListener                           │ │
      │ │ AsistenciaEventListener                     │ │
      │ └──────────┬──────────────────────────────────┘ │
      │            │                                     │
      │ ┌──────────▼──────────────────────────────────┐ │
      │ │ BD PostgreSQL (reportes_db)                 │ │
      │ │ - reporte_matriculas                        │ │
      │ │ - reporte_notas                             │ │
      │ │ - reporte_asistencias                       │ │
      │ └──────────────────────────────────────────────┘ │
      │                                                    │
      │ ┌──────────────────────────────────────────────┐ │
      │ │ APIs REST (queries)                          │ │
      │ │ GET /api/reportes/matriculas/alumno/1       │ │
      │ │ GET /api/reportes/notas/alumno/1/promedio   │ │
      │ └──────────────────────────────────────────────┘ │
      └────────────────────────────────────────────────────┘
```

## Próximos Pasos

1. ✅ Crear BD `reportes_db` en PostgreSQL
2. ⏳ Configurar publicadores de eventos en otros servicios
3. ⏳ Ejecutar servicio de reportes
4. ⏳ Generar eventos y verificar reportes

## Requisitos Previos

- Java 21+
- PostgreSQL 12+
- RabbitMQ 3.8+
- Maven 3.8+

## Construcción y Ejecución

```bash
# Construir
mvn clean package

# Ejecutar
java -jar target/servicio-reportes-1.0.0.jar

# O con Maven
mvn spring-boot:run
```
