package com.mcmm.model.dao;

import com.mcmm.model.entity.Cargo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CargoDao extends JpaRepository<Cargo, Long> {

}