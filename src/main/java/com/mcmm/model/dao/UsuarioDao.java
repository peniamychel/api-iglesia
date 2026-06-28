package com.mcmm.model.dao;


import com.mcmm.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioDao extends JpaRepository<Usuario, Long> {

    @Query("SELECT u FROM Usuario u " +
           "LEFT JOIN FETCH u.miembro m " +
           "LEFT JOIN FETCH m.cargos c " +
           "LEFT JOIN FETCH c.rolCargo rc " +
           "LEFT JOIN FETCH c.iglesia i " +
           "WHERE u.username = ?1")
    Optional<Usuario> findByUsername(String username);

    Boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM Usuario u WHERE u.username = ?1")
    Optional<Usuario> getName(String username);

    @Query("SELECT COUNT(u) > 0 FROM Usuario u WHERE u.miembro.id = :miembroId AND u.estado = true")
    boolean existsByMiembroIdAndEstadoTrue(@org.springframework.data.repository.query.Param("miembroId") Long miembroId);
}
