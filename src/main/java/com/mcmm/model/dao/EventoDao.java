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

    // Eventos NO archivados visibles para la iglesia (propios, generales o invitados).
    @Query(value = "SELECT * FROM evento e WHERE (e.archivado = false OR e.archivado IS NULL) AND (e.iglesia_id = :iglesiaId OR e.alcance = 'GENERAL' OR FIND_IN_SET(:iglesiaId, e.iglesias_invitadas) > 0)", nativeQuery = true)
    List<Evento> findEventosParaIglesia(@Param("iglesiaId") Long iglesiaId);

    // Eventos ARCHIVADOS visibles para la iglesia.
    @Query(value = "SELECT * FROM evento e WHERE e.archivado = true AND (e.iglesia_id = :iglesiaId OR e.alcance = 'GENERAL' OR FIND_IN_SET(:iglesiaId, e.iglesias_invitadas) > 0)", nativeQuery = true)
    List<Evento> findEventosArchivadosParaIglesia(@Param("iglesiaId") Long iglesiaId);

    // Admin (sin iglesia en el token): todos los eventos no archivados / archivados.
    @Query(value = "SELECT * FROM evento e WHERE e.archivado = false OR e.archivado IS NULL", nativeQuery = true)
    List<Evento> findNoArchivados();

    @Query(value = "SELECT * FROM evento e WHERE e.archivado = true", nativeQuery = true)
    List<Evento> findArchivadosTodos();

    List<Evento> findByFechaInicioBetween(Date start, Date end);

    @Modifying
    @Transactional
    @Query("UPDATE Evento e SET e.estado = NOT e.estado, e.updatedAt = CURRENT_TIMESTAMP WHERE e.id = :id")
    void toggleEstado(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE Evento e SET e.archivado = :archivado, e.updatedAt = CURRENT_TIMESTAMP WHERE e.id = :id")
    void setArchivado(@Param("id") Long id, @Param("archivado") boolean archivado);

    // Solo los IDs de eventos con inscripciones habilitadas e invitacion a la iglesia dada.
    // Liviano a proposito: se usa para contar notificaciones pendientes, no para listar.
    @Query(value = "SELECT e.id FROM evento e WHERE e.habilitar_inscripciones = true AND FIND_IN_SET(:iglesiaId, e.iglesias_invitadas) > 0", nativeQuery = true)
    List<Long> findIdsEventosHabilitadosParaIglesia(@Param("iglesiaId") Long iglesiaId);
}