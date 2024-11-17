package com.mcmm.controller;

import com.mcmm.model.dto.PersonaDto;
import com.mcmm.model.payload.MessageResponse;
import com.mcmm.service.IPersona;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/persona/v1")
@PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO')")
public class PersonaController{

    @Autowired
    private IPersona personaService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> create(@RequestBody PersonaDto personaDto){
        ResponseEntity<?> responseEntity;
        try {
            PersonaDto personaSave = personaService.save(personaDto);
            responseEntity = new ResponseEntity<>(
                    MessageResponse.builder()
                            .message("Persona guardada exitosamente.")
                            .datos(personaSave)
                            .nombreModelo("Persona")
                            .build(),
                    HttpStatus.CREATED
            );
        } catch (DataAccessException e) {
            responseEntity = new ResponseEntity<>(
                    MessageResponse.builder()
                            .message("Error al guardar la Persona.")
                            .datos(null)
                            .build(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
        return responseEntity;
    }

    @GetMapping("/findall")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> findAll(){
        ResponseEntity<?> responseEntity;
        Iterable<PersonaDto> personaDtos = personaService.findAll();
        try {
            responseEntity = new ResponseEntity<>(
                    MessageResponse.builder()
                            .message("Listado de Personas")
                            .datos(personaDtos)
                            .nombreModelo("Persona")
                            .build()
                    , HttpStatus.OK
            );
        } catch (Exception e) {
            responseEntity = new ResponseEntity<>(
                    MessageResponse.builder()
                            .message("No se encontro datos.")
                            .datos(null)
                            .build()
                    , HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
        return responseEntity;
    }

    @PutMapping("/update")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> update(@RequestBody PersonaDto personaDto) {
        ResponseEntity<?> responseEntity;
        try {
            PersonaDto personaUpdate = personaService.save(personaDto);
            responseEntity = new ResponseEntity<>(
                    MessageResponse.builder()
                            .message("Persona actualizada exitosamente.")
                            .datos(personaUpdate)
                            .nombreModelo("Persona")
                            .build(),
                    HttpStatus.OK
            );
        } catch (DataAccessException e) {
            responseEntity = new ResponseEntity<>(
                    MessageResponse.builder()
                            .message("Error al actualizar Persona.")
                            .datos(null)
                            .build(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
        return responseEntity;
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<?> delete(@PathVariable Long id) {
        ResponseEntity<?> responseEntity;
        try {
            PersonaDto personaDelete = personaService.findById(id);
            if (personaDelete != null) {
                personaService.delete(personaDelete);
                responseEntity = new ResponseEntity<>(
                        MessageResponse.builder()
                                .message("Persona eliminada exitosamente.")
                                .datos(personaDelete)
                                .nombreModelo("Persona")
                                .build(),
                        HttpStatus.OK
                );
            } else {
                responseEntity = new ResponseEntity<>(
                        MessageResponse.builder()
                                .message("La Persona con ID " + id + " no existe.")
                                .datos(null)
                                .nombreModelo("Persona")
                                .build(),
                        HttpStatus.NOT_FOUND
                );
            }
        } catch (DataAccessException exDta) {
            responseEntity = new ResponseEntity<>(
                    MessageResponse.builder()
                            .message("Error al eliminar la Persona.")
                            .datos(null)
                            .build(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
        return responseEntity;
    }

    @GetMapping("/showbyid/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> showById(@PathVariable("id") Long id) {
        ResponseEntity<?> responseEntity;
        try {
            PersonaDto personaFiedById = personaService.findById(id);
            if (personaFiedById == null) {
                responseEntity = new ResponseEntity<>(
                        MessageResponse.builder()
                                .message("Persona no encontrada.")
                                .datos(null)
                                .build(),
                        HttpStatus.NOT_FOUND
                );
            } else {
                responseEntity = new ResponseEntity<>(
                        MessageResponse.builder()
                                .message("Persona encontrada.")
                                .datos(personaFiedById)
                                .nombreModelo("Persona")
                                .build(),
                        HttpStatus.OK
                );
            }
        } catch (DataAccessException e) {
            responseEntity = new ResponseEntity<>(
                    MessageResponse.builder()
                            .message("Error al buscar la Persona.")
                            .datos(null)
                            .build(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
        return responseEntity;
    }


    /*buscara si la persona ya tiene datos de miembrro*/
    @GetMapping("/personanomiembro")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> personaNoMiembro() {
        ResponseEntity<?> responseEntity;
        Iterable<PersonaDto> personaDtos = personaService.personaNoMiembro();
        try {
            responseEntity = new ResponseEntity<>(
                    MessageResponse.builder()
                            .message("Listado de Personas")
                            .datos(personaDtos)
                            .nombreModelo("Persona")
                            .build()
                    , HttpStatus.OK
            );
        } catch (Exception e) {
            responseEntity = new ResponseEntity<>(
                    MessageResponse.builder()
                            .message("No se encontro datos.")
                            .datos(null)
                            .build()
                    , HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
        return responseEntity;
    }

    //busca segun cidula identidad
    @GetMapping("/showbyci/{ci}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> buscarCi(@PathVariable("ci") String ci) {
        ResponseEntity<?> responseEntity;
        try {
//            PersonaDto personaFiedById = personaService.buscarCi(ci);
            PersonaDto personaFiedById = personaService.buscarCi(ci);

            if (personaFiedById == null) {
                responseEntity = new ResponseEntity<>(
                        MessageResponse.builder()
                                .message("Persona no encontrada.")
                                .datos(null)
                                .build(),
                        HttpStatus.NOT_FOUND
                );
            } else {
                responseEntity = new ResponseEntity<>(
                        MessageResponse.builder()
                                .message("Persona encontrada.")
                                .datos(personaFiedById)
                                .nombreModelo("Persona")
                                .build(),
                        HttpStatus.OK
                );
            }
        } catch (DataAccessException e) {
            responseEntity = new ResponseEntity<>(
                    MessageResponse.builder()
                            .message("Error al buscar la Persona.")
                            .datos(null)
                            .build(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
        return responseEntity;
    }


}
