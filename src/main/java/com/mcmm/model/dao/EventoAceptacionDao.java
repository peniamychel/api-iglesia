package com.mcmm.model.dao;

import com.mcmm.model.entity.EventoAceptacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventoAceptacionDao extends JpaRepository<EventoAceptacion, Long> {
    Optional<EventoAceptacion> findByEventoIdAndIglesiaId(Long eventoId, Long iglesiaId);
    List<EventoAceptacion> findByIglesiaId(Long iglesiaId);
}
