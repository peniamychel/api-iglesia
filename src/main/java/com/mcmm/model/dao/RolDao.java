package com.mcmm.model.dao;

import com.mcmm.model.entity.ERole;
import com.mcmm.model.entity.Rol;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository

public interface RolDao extends JpaRepository<Rol, Long> {
    Optional<Rol> findByName(ERole name);
    boolean existsByName(ERole name);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Rol r WHERE r.name = :name")
    Optional<Rol> findByNameWithLock(@Param("name") ERole name);

}