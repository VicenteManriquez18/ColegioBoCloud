package com.proyecto.ColegioBackend.messaging;

import com.proyecto.ColegioBackend.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class UserConsumer {

    @RabbitListener(queues = RabbitMQConfig.REGISTRO_QUEUE)
    public void recibirRegistroUsuario(Map<String, Object> data) {
        System.out.println("\n>>> [RabbitMQ] NUEVO USUARIO DETECTADO");
        System.out.println(">>> ID: " + data.get("id"));
        System.out.println(">>> Correo: " + data.get("correo"));
        System.out.println(">>> Rol: " + data.get("rol"));
        System.out.println(">>> Procesando creación de ficha interna en Matrícula...\n");
    }
}