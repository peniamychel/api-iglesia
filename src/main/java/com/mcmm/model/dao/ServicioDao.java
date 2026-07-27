package com.mcmm.model.dao;

import com.mcmm.model.entity.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServicioDao extends JpaRepository<Servicio, Long> {
    Optional<Servicio> findByCodigo(String codigo);
    List<Servicio> findByActivoTrueOrderByOrdenAsc();
}
