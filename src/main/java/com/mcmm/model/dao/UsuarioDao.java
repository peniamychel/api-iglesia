package com.mcmm.model.dao;


import com.mcmm.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioDao extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    Boolean existsByUsername(String username);

    @Query("SELECT u FROM Usuario u WHERE u.username = ?1")
    Optional<Usuario> getName(String username);

}
