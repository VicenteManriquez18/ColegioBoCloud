# API Gateway (BFF) - Colegio Backend

Este es el Backend For Frontend (BFF) o API Gateway del proyecto. Funciona como el punto de entrada único (enrutador) para todos los microservicios del sistema.

## Información General
- **Base URL (Gateway):** `http://localhost:9090`
- **Puerto de ejecución:** `9090`

## Stack Tecnológico
- Spring Boot 3.x
- Java 17+
- Spring Cloud Gateway
- Maven
- **Resilience4j:** Implementación de Circuit Breaker para tolerancia a fallos.
- **Actuator:** Monitoreo del estado de salud y métricas de los disyuntores.

## Resiliencia
El Gateway utiliza Circuit Breakers para todas las rutas. Si un microservicio falla:
1. El circuito se abre tras un 50% de errores en una ventana de 10 llamadas.
2. Se redirige la petición a un `FallbackController` interno que devuelve un error 503 amigable.

### Monitoreo del Circuit Breaker
Puedes verificar el estado de los circuitos accediendo a:
`GET http://localhost:9090/actuator/health`

## Instalación y Ejecución

### Prerrequisitos
- JDK instalado (versión 17 o superior).
- Maven instalado (opcional, el proyecto incluye el wrapper `mvnw`).

### Pasos para ejecutar localmente

1. Abre una terminal (CMD o PowerShell) y navega hasta la carpeta del API Gateway:
   ```cmd
   cd ruta/hacia/apigateway
   ```

2. Ejecuta el proyecto usando el wrapper de Maven:
   ```cmd
   .\mvnw.cmd spring-boot:run
   ```

3. El servicio se iniciará y estará escuchando peticiones en `http://localhost:9090`.

## Pruebas
Puedes comprobar que el BFF está levantado realizando cualquier petición hacia los microservicios usando el puerto `9090`. Por ejemplo:
- `GET http://localhost:9090/api/usuarios`
- `GET http://localhost:9090/api/asistencia`
