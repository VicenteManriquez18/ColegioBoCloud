package com.proyecto.reportes.listener;

import com.proyecto.reportes.entity.ReporteNota;
import com.proyecto.reportes.factory.ReporteNotaFactory;
import com.proyecto.reportes.model.event.NotaGeneradaEvent;
import com.proyecto.reportes.repository.ReporteNotaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NotaEventListener {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NotaEventListener.class);
    
    @Autowired
    private ReporteNotaRepository reporteNotaRepository;

    @Autowired
    private ReporteNotaFactory reporteNotaFactory;
    
    @RabbitListener(queues = "eventos.nota.queue")
    public void handleNotaGenerada(NotaGeneradaEvent evento) {
        try {
            log.info("Recibido evento de nota generada: {}", evento.getNotaId());
            
            ReporteNota reporte = reporteNotaFactory.buildReportEntity(evento);
            
            reporteNotaRepository.save(reporte);
            log.info("Reporte de nota guardado exitosamente");
            
        } catch (Exception e) {
            log.error("Error procesando evento de nota: ", e);
        }
    }
}
