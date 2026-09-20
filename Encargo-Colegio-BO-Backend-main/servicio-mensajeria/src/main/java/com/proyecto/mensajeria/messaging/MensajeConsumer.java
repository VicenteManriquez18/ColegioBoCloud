package com.proyecto.mensajeria.messaging;

import com.proyecto.mensajeria.config.RabbitMQConfig;
import com.proyecto.mensajeria.entity.Mensaje;
import com.proyecto.mensajeria.repository.MensajeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MensajeConsumer {

    private static final Logger log = LoggerFactory.getLogger(MensajeConsumer.class);

    @Autowired
    private MensajeRepository mensajeRepository;

    @RabbitListener(queues = RabbitMQConfig.CHAT_QUEUE)
    public void procesarMensajeChat(Mensaje mensaje) {
        try {
            log.info(">>> [RabbitMQ Consumer] Recibido mensaje desde la cola: de {} para {}", mensaje.getRemitenteNombre(), mensaje.getDestinatarioNombre());
            
            // Persistir en la base de datos
            Mensaje guardado = mensajeRepository.save(mensaje);
            log.info(">>> [RabbitMQ Consumer] Mensaje persistido en la base de datos con ID: {}", guardado.getId());
        } catch (Exception e) {
            log.error(">>> [RabbitMQ Consumer] Error al procesar y guardar el mensaje: {}", e.getMessage());
        }
    }
}
