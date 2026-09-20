package com.proyecto.mensajeria.repository;

import com.proyecto.mensajeria.entity.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    @Query("SELECT m FROM Mensaje m WHERE " +
           "(m.remitenteId = :u1 AND m.destinatarioId = :u2) OR " +
           "(m.remitenteId = :u2 AND m.destinatarioId = :u1) " +
           "ORDER BY m.fechaEnvio ASC")
    List<Mensaje> findChatHistory(@Param("u1") Long user1, @Param("u2") Long user2);

    @Query("SELECT DISTINCT CASE WHEN m.remitenteId = :userId THEN m.destinatarioId ELSE m.remitenteId END FROM Mensaje m WHERE m.remitenteId = :userId OR m.destinatarioId = :userId")
    List<Long> findConversationsForUser(@Param("userId") Long userId);
}
