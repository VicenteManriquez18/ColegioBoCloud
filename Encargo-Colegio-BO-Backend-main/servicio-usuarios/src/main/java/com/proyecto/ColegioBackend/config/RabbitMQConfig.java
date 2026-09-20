package com.proyecto.ColegioBackend.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Configuración para Registro de Usuarios
    public static final String REGISTRO_QUEUE = "usuario_registro_queue";
    public static final String EXCHANGE = "usuario_exchange";
    public static final String ROUTING_KEY = "usuario_routing_key";

    // Configuración para Chat entre Apoderados y Profesores
    public static final String CHAT_QUEUE = "chat_mensajes_queue";
    public static final String CHAT_EXCHANGE = "chat_exchange";
    public static final String CHAT_ROUTING_PATTERN = "chat.#";

    @Bean
    public Queue queue() {
        return new Queue(REGISTRO_QUEUE, true);
    }

    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Binding binding(Queue queue, TopicExchange userExchange) {
        return BindingBuilder.bind(queue).to(userExchange).with(ROUTING_KEY);
    }

    @Bean
    public Queue chatQueue() {
        return new Queue(CHAT_QUEUE, true);
    }

    @Bean
    public TopicExchange chatExchange() {
        return new TopicExchange(CHAT_EXCHANGE);
    }

    @Bean
    public Binding chatBinding(Queue chatQueue, TopicExchange chatExchange) {
        return BindingBuilder.bind(chatQueue).to(chatExchange).with(CHAT_ROUTING_PATTERN);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
