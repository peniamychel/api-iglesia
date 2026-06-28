package com.mcmm.model.dao;

import com.mcmm.model.entity.Activo;
import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface ActivoDao extends CrudRepository<Activo, Long> {
    List<Activo> findByIglesiaId(Long iglesiaId);
}
