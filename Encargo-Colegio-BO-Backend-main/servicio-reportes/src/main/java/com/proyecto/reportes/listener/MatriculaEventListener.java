package com.proyecto.reportes.listener;

import com.proyecto.reportes.entity.ReporteMatricula;
import com.proyecto.reportes.factory.ReporteMatriculaFactory;
import com.proyecto.reportes.model.event.MatriculaRegistradaEvent;
import com.proyecto.reportes.repository.ReporteMatriculaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MatriculaEventListener {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MatriculaEventListener.class);
    
    @Autowired
    private ReporteMatriculaRepository reporteMatriculaRepository;

    @Autowired
    private ReporteMatriculaFactory reporteMatriculaFactory;
    
    @RabbitListener(queues = "eventos.matricula.queue")
    public void handleMatriculaRegistrada(MatriculaRegistradaEvent evento) {
        try {
            log.info("Recibido evento de matrícula registrada: {}", evento.getMatriculaId());
            
            ReporteMatricula reporte = reporteMatriculaFactory.buildReportEntity(evento);
            
            reporteMatriculaRepository.save(reporte);
            log.info("Reporte de matrícula guardado exitosamente");
            
        } catch (Exception e) {
            log.error("Error procesando evento de matrícula: ", e);
        }
    }
}
