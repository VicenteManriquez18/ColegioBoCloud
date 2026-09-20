package com.proyecto.ColegioBackend.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String REGISTRO_QUEUE = "usuario_registro_queue";
    public static final String CHAT_QUEUE = "chat_mensajes_queue";

    @Bean
    public Queue registroQueue() {
        return new Queue(REGISTRO_QUEUE, true);
    }

    @Bean
    public Queue chatQueue() {
        return new Queue(CHAT_QUEUE, true);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
