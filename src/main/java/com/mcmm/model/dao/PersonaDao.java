package com.mcmm.model.dao;

import com.mcmm.model.entity.Persona;
import com.mcmm.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonaDao extends JpaRepository<Persona, Long> {

//    @Query("SELECT p.nombre  FROM Persona p LEFT JOIN Miembro m ON p.id = m.persona_id WHERE m.persona_id IS NULL")
//    Optional<Persona> personaNoMiembro(String username);


    /*busca si la persona ya tiene datos de miembro*/
    @Query("SELECT p FROM Persona p LEFT JOIN Miembro m ON p.id = m.persona.id WHERE m.persona.id IS NULL")
    List<Persona> findPersonasWithoutMiembro();

    @Query(value = "CALL searchPersonByCI(:ci)", nativeQuery = true)
    Persona findByCiNative(@Param("ci") Integer ci);

    /*busca ci si ya existe*/
    @Query("SELECT p FROM Persona p WHERE p.ci = :ci")
    Persona findByCi(@Param("ci") String ci);

}
