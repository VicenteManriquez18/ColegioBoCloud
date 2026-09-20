package com.proyecto.reportes.config;

import java.util.Map;
import java.util.Base64;

public class JwtUtil {

    public static Map<String, Object> parseToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                String[] parts = token.split("\\.");
                if (parts.length == 3) {
                    String payload = new String(Base64.getUrlDecoder().decode(parts[1]), "UTF-8");
                    Long id = null;
                    String rol = null;
                    String sub = null;
                    
                    // extract "id":
                    int idIndex = payload.indexOf("\"id\":");
                    if (idIndex != -1) {
                        int start = idIndex + 5;
                        int end = payload.indexOf(",", start);
                        if (end == -1) end = payload.indexOf("}", start);
                        id = Long.parseLong(payload.substring(start, end).trim());
                    }
                    
                    // extract "rol":
                    int rolIndex = payload.indexOf("\"rol\":\"");
                    if (rolIndex != -1) {
                        int start = rolIndex + 7;
                        int end = payload.indexOf("\"", start);
                        rol = payload.substring(start, end);
                    }

                    // extract "sub":
                    int subIndex = payload.indexOf("\"sub\":\"");
                    if (subIndex != -1) {
                        int start = subIndex + 7;
                        int end = payload.indexOf("\"", start);
                        sub = payload.substring(start, end);
                    }
                    
                    return Map.of("id", id, "rol", rol, "sub", sub);
                }
            } catch (Exception e) {
                // Ignore
            }
        }
        return null;
    }
}
