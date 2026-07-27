package com.mcmm.model.dao;

import com.mcmm.model.entity.Bitacora;
import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface BitacoraDao extends CrudRepository<Bitacora, Long> {
    List<Bitacora> findAllByOrderByFechaDesc();
    List<Bitacora> findByModuloOrderByFechaDesc(String modulo);
    List<Bitacora> findByUsernameOrderByFechaDesc(String username);
}
