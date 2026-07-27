package com.mcmm.model.dao;

import com.mcmm.model.entity.EventoAceptacion;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventoAceptacionDao extends JpaRepository<EventoAceptacion, Long> {
    Optional<EventoAceptacion> findByEventoIdAndIglesiaId(Long eventoId, Long iglesiaId);
    List<EventoAceptacion> findByIglesiaId(Long iglesiaId);

    @Modifying
    @Transactional
    @Query("DELETE FROM EventoAceptacion ea WHERE ea.evento.id = :eventoId")
    void deleteByEventoId(@Param("eventoId") Long eventoId);

    @org.springframework.data.jpa.repository.Query(
        "SELECT ea.evento.id FROM EventoAceptacion ea WHERE ea.iglesiaId = :iglesiaId AND ea.evento.id IN :eventoIds")
    List<Long> findEventoIdsDecididos(@Param("iglesiaId") Long iglesiaId, @Param("eventoIds") List<Long> eventoIds);
}
