package com.proyecto.ColegioBackend.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import com.proyecto.ColegioBackend.ColegioAcademicoApplication;
import java.util.List;
import java.util.Map;

@SpringBootTest(classes = ColegioAcademicoApplication.class)
class DatabaseQueryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void inspectDatabaseAndTestInsert() {
        org.junit.jupiter.api.Assertions.assertNotNull(jdbcTemplate, "JdbcTemplate should be injected and not null");
        System.out.println("==================================================");
        System.out.println("   DIAGNOSTIC TEST: ACADEMICO_DB DATABASE STATUS   ");
        System.out.println("==================================================");

        // 1. Inspect tables schema and contents
        printTableInfo("cursos");
        printTableInfo("pruebas");
        printTableInfo("notas");
        printTableInfo("matriculas_cursos");

        // 2. Try inserting a mock grade to see if it triggers any constraint errors
        System.out.println("\n--- Testing Mock Grade Insert ---");
        try {
            // First check if a course exists, if not, create one
            List<Map<String, Object>> cursos = jdbcTemplate.queryForList("SELECT id FROM cursos LIMIT 1;");
            Long cursoId;
            if (cursos.isEmpty()) {
                jdbcTemplate.execute("INSERT INTO cursos (nombre, codigo, descripcion, profesor_id) VALUES ('Curso Test', 'TEST-101', 'Curso de prueba', 1);");
                cursoId = ((Number) jdbcTemplate.queryForList("SELECT id FROM cursos ORDER BY id DESC LIMIT 1;").get(0).get("id")).longValue();
                System.out.println("Created test course with ID: " + cursoId);
            } else {
                cursoId = ((Number) cursos.get(0).get("id")).longValue();
                System.out.println("Using existing course with ID: " + cursoId);
            }

            // Check if a test (prueba) exists, if not, create one
            List<Map<String, Object>> pruebas = jdbcTemplate.queryForList("SELECT id FROM pruebas LIMIT 1;");
            Long pruebaId;
            if (pruebas.isEmpty()) {
                jdbcTemplate.execute("INSERT INTO pruebas (titulo, descripcion, fecha, curso_id) VALUES ('Prueba Test', 'Prueba de diagnostico', '2026-06-03', " + cursoId + ");");
                pruebaId = ((Number) jdbcTemplate.queryForList("SELECT id FROM pruebas ORDER BY id DESC LIMIT 1;").get(0).get("id")).longValue();
                System.out.println("Created test prueba with ID: " + pruebaId);
            } else {
                pruebaId = ((Number) pruebas.get(0).get("id")).longValue();
                System.out.println("Using existing prueba with ID: " + pruebaId);
            }

            // Try to insert/update a grade (nota)
            jdbcTemplate.execute("INSERT INTO notas (prueba_id, alumno_id, valor, comentario) VALUES (" + pruebaId + ", 999, 6.8, 'Comentario de prueba') " +
                                 "ON CONFLICT DO NOTHING;");
            System.out.println("Successfully executed mock grade insert/no-conflict statement!");

            // Print grades currently in DB
            List<Map<String, Object>> notas = jdbcTemplate.queryForList("SELECT * FROM notas;");
            System.out.println("Grades (notas) in database: " + notas.size());
            for (Map<String, Object> n : notas) {
                System.out.println("  " + n);
            }

        } catch (Exception e) {
            System.err.println("CRITICAL ERROR during mock insert test:");
            e.printStackTrace();
        }
        System.out.println("==================================================");
    }

    private void printTableInfo(String tableName) {
        System.out.println("\n--- Table: " + tableName + " ---");
        try {
            // Columns
            List<Map<String, Object>> cols = jdbcTemplate.queryForList(
                "SELECT column_name, data_type, is_nullable FROM information_schema.columns WHERE table_name = '" + tableName + "';"
            );
            System.out.println("Columns:");
            for (Map<String, Object> col : cols) {
                System.out.printf("  %s (%s, Nullable: %s)%n", col.get("column_name"), col.get("data_type"), col.get("is_nullable"));
            }

            // Rows count
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT * FROM " + tableName + " LIMIT 5;");
            System.out.println("Rows count (preview first 5): " + rows.size());
            for (Map<String, Object> r : rows) {
                System.out.println("  " + r);
            }
        } catch (Exception e) {
            System.err.println("Error reading table " + tableName + ": " + e.getMessage());
        }
    }
}
