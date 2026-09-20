package com.colegio.apigateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Gateway - Colegio")
                        .version("1.0.0")
                        .description("API Gateway para la gestión integral del colegio. " +
                                "Proporciona acceso a todos los microservicios del sistema.")
                        .contact(new Contact()
                                .name("Soporte")
                                .email("soporte@colegio.com")
                                .url("https://colegio.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:9090")
                                .description("Servidor Local"),
                        new Server()
                                .url("https://api.colegio.com")
                                .description("Servidor Producción")
                ));
    }
}
