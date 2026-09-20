package com.proyecto.mensajeria;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

@SpringBootApplication
public class ColegioMensajeriaApplication {

    public static void main(String[] args) {
        crearBaseDeDatosSiNoExiste();
        SpringApplication.run(ColegioMensajeriaApplication.class, args);
    }

    private static void crearBaseDeDatosSiNoExiste() {
        String jdbcUrl = "jdbc:postgresql://localhost:5432/postgres";
        String username = "postgres";
        String password = System.getenv().getOrDefault("SPRING_DATASOURCE_PASSWORD", "1234");
        try {
            // Cargar clase del driver para evitar problemas en algunos entornos de ejecución
            Class.forName("org.postgresql.Driver");
            try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
                 Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("CREATE DATABASE mensajeria_db");
                System.out.println(">>> Base de datos 'mensajeria_db' creada con éxito.");
            }
        } catch (Exception e) {
            System.out.println(">>> Base de datos 'mensajeria_db' ya existe o no se pudo crear (se continuará la ejecución). Detalle: " + e.getMessage());
        }
    }
}
