package com.mcmm.model.dao;

import com.mcmm.model.entity.Miembro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MiembroDao extends JpaRepository<Miembro, Long> {

}
