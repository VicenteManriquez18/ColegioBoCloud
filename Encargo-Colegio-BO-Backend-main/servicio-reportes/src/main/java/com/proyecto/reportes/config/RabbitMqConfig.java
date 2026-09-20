package com.proyecto.reportes.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
    
    // ==================== EXCHANGES ====================
    
    @Bean
    public TopicExchange eventoExchange() {
        return new TopicExchange("eventos.exchange", true, false);
    }
    
    // ==================== QUEUES ====================
    
    @Bean
    public Queue matriculaQueue() {
        return new Queue("eventos.matricula.queue", true);
    }
    
    @Bean
    public Queue notaQueue() {
        return new Queue("eventos.nota.queue", true);
    }
    
    @Bean
    public Queue asistenciaQueue() {
        return new Queue("eventos.asistencia.queue", true);
    }
    
    // ==================== BINDINGS ====================
    
    @Bean
    public Binding matriculaBinding(Queue matriculaQueue, TopicExchange eventoExchange) {
        return BindingBuilder.bind(matriculaQueue)
                .to(eventoExchange)
                .with("matricula.registrada");
    }
    
    @Bean
    public Binding notaBinding(Queue notaQueue, TopicExchange eventoExchange) {
        return BindingBuilder.bind(notaQueue)
                .to(eventoExchange)
                .with("nota.generada");
    }
    
    @Bean
    public Binding asistenciaBinding(Queue asistenciaQueue, TopicExchange eventoExchange) {
        return BindingBuilder.bind(asistenciaQueue)
                .to(eventoExchange)
                .with("asistencia.registrada");
    }
}
