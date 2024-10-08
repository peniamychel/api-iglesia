package com.mcmm.controller;

import com.mcmm.model.dto.PersonaDto;
import com.mcmm.model.payload.MessageResponse;
import com.mcmm.service.IPersona;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class PersonaController {
    @Autowired
    private IPersona personaService;

    @PostMapping("/persona")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> create(@RequestBody PersonaDto personaDto) {
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

    @PutMapping("/persona")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> update(@RequestBody PersonaDto personaDto) {
        ResponseEntity<?> responseEntity;
        try {
            PersonaDto iglesiaUpdate = personaService.save(personaDto);
            responseEntity = new ResponseEntity<>(
                    MessageResponse.builder()
                            .message("Persona actualizada exitosamente.")
                            .datos(iglesiaUpdate)
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

    @DeleteMapping("/persona/{id}")
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

    @GetMapping("/persona/{id}")
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

    @GetMapping("/personas")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> findAll() {
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
}
