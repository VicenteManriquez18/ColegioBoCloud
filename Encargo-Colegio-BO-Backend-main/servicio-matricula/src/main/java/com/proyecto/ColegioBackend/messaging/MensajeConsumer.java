package com.proyecto.ColegioBackend.messaging;

import com.proyecto.ColegioBackend.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class MensajeConsumer {

    @RabbitListener(queues = RabbitMQConfig.CHAT_QUEUE)
    public void procesarMensajeChat(Map<String, Object> datos, @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {
        Long usuarioIdEmisor = Long.valueOf(datos.get("emisorId").toString());
        String contenido = (String) datos.get("contenido");
        String destino = routingKey.replace("chat.", "");

        System.out.println("\n--- NUEVO MENSAJE DE CHAT INTERNO ---");
        System.out.println("De Usuario ID: " + usuarioIdEmisor);
        System.out.println("Dirigido a: " + destino);
        System.out.println("Mensaje: " + contenido);
        System.out.println("-------------------------------------\n");
    }
}