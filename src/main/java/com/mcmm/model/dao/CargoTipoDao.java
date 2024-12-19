package com.mcmm.model.dao;

import com.mcmm.model.entity.CargoTipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CargoTipoDao extends JpaRepository<CargoTipo, Long> {

}
