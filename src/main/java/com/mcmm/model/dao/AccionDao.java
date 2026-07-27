package com.mcmm.model.dao;

import com.mcmm.model.entity.Accion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccionDao extends JpaRepository<Accion, Long> {
    List<Accion> findByServicioIdAndActivoTrue(Long servicioId);
    Optional<Accion> findByServicioCodigoAndCodigo(String servicioCodigo, String codigo);
}
