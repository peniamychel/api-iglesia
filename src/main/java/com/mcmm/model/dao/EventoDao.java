package com.mcmm.model.dao;

import com.mcmm.model.entity.Evento;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface EventoDao extends JpaRepository<Evento, Long> {

    List<Evento> findByIglesiaId(Long iglesiaId);

    @Query(value = "SELECT * FROM evento e WHERE e.iglesia_id = :iglesiaId OR e.alcance = 'GENERAL' OR FIND_IN_SET(:iglesiaId, e.iglesias_invitadas) > 0", nativeQuery = true)
    List<Evento> findEventosParaIglesia(@Param("iglesiaId") Long iglesiaId);

    List<Evento> findByFechaInicioBetween(Date start, Date end);

    @Modifying
    @Transactional
    @Query("UPDATE Evento e SET e.estado = NOT e.estado, e.updatedAt = CURRENT_TIMESTAMP WHERE e.id = :id")
    void toggleEstado(@Param("id") Long id);

    // Solo los IDs de eventos con inscripciones habilitadas e invitacion a la iglesia dada.
    // Liviano a proposito: se usa para contar notificaciones pendientes, no para listar.
    @Query(value = "SELECT e.id FROM evento e WHERE e.habilitar_inscripciones = true AND FIND_IN_SET(:iglesiaId, e.iglesias_invitadas) > 0", nativeQuery = true)
    List<Long> findIdsEventosHabilitadosParaIglesia(@Param("iglesiaId") Long iglesiaId);
}