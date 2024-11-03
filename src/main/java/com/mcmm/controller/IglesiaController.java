package com.mcmm.controller;

import com.mcmm.model.dto.IglesiaDto;
import com.mcmm.model.dto.PersonaDto;
import com.mcmm.model.entity.Iglesia;
import com.mcmm.model.payload.MessageResponse;
import com.mcmm.service.IIglesia;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/iglesia/v1")
@PreAuthorize("hasRole('ADMIN')")
public class IglesiaController {
    @Autowired
    private IIglesia iglesiaService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> create(@RequestBody IglesiaDto iglesiaDto) {
        ResponseEntity<?> responseEntity;
        try {
            IglesiaDto iglesiaSave = iglesiaService.save(iglesiaDto);
            responseEntity = new ResponseEntity<>(
                    MessageResponse.builder()
                            .message("Iglesia guardada exitosamente.")
                            .datos(iglesiaSave)
                            .nombreModelo("Iglesia")
                            .build(),
                    HttpStatus.CREATED
            );
        } catch (DataAccessException e) {
            responseEntity = new ResponseEntity<>(
                    MessageResponse.builder()
                            .message("Error al guardar la iglesia.")
                            .datos(null)
                            .build(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
        return responseEntity;
    }

    @PutMapping("/update")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> update(@RequestBody IglesiaDto iglesiaDto) {
        ResponseEntity<?> responseEntity;
        try {
            IglesiaDto iglesiaUpdate = iglesiaService.save(iglesiaDto);
            responseEntity = new ResponseEntity<>(
                    MessageResponse.builder()
                            .message("Iglesia actualizada exitosamente.")
                            .datos(iglesiaUpdate)
                            .nombreModelo("Iglesia")
                            .build(),
                    HttpStatus.OK
            );
        } catch (DataAccessException e) {
            responseEntity = new ResponseEntity<>(
                    MessageResponse.builder()
                            .message("Error al actualizar la iglesia.")
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
            IglesiaDto iglesiaDelete = iglesiaService.findById(id);
            if (iglesiaDelete != null) {
                iglesiaService.delete(iglesiaDelete);
                responseEntity = new ResponseEntity<>(
                        MessageResponse.builder()
                                .message("Iglesia eliminada exitosamente.")
                                .datos(iglesiaDelete)
                                .nombreModelo("Iglesia")
                                .build(),
                        HttpStatus.OK
                );
            } else {
                responseEntity = new ResponseEntity<>(
                        MessageResponse.builder()
                                .message("La iglesia con ID " + id + " no existe.")
                                .datos(null)
                                .nombreModelo("Iglesia")
                                .build(),
                        HttpStatus.NOT_FOUND
                );
            }
        } catch (DataAccessException exDta) {
            responseEntity = new ResponseEntity<>(
                    MessageResponse.builder()
                            .message("Error al eliminar la iglesia.")
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
            IglesiaDto iglesiaFiedById = iglesiaService.findById(id);
            if (iglesiaFiedById == null) {
                responseEntity = new ResponseEntity<>(
                        MessageResponse.builder()
                                .message("Iglesia no encontrada.")
                                .datos(null)
                                .build(),
                        HttpStatus.NOT_FOUND
                );
            } else {
                responseEntity = new ResponseEntity<>(
                        MessageResponse.builder()
                                .message("Iglesia encontrada.")
                                .datos(iglesiaFiedById)
                                .nombreModelo("Iglesia")
                                .build(),
                        HttpStatus.OK
                );
            }
        } catch (DataAccessException e) {
            responseEntity = new ResponseEntity<>(
                    MessageResponse.builder()
                            .message("Error al buscar la iglesia.")
                            .datos(null)
                            .build(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
        return responseEntity;
    }

    @GetMapping("/findall")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> findAll() {
        ResponseEntity<?> responseEntity;
        Iterable<IglesiaDto> iglesiaDtos = iglesiaService.findAll();
        try {
            responseEntity = new ResponseEntity<>(
                    MessageResponse.builder()
                            .message("Listado de iglesias")
                            .datos(iglesiaDtos)
                            .nombreModelo("iglesia")
                            .build()
                    , HttpStatus.OK
            );
        } catch (Exception e) {
            responseEntity = new ResponseEntity<>(
                    MessageResponse.builder()
                            .message("No se encontro.")
                            .datos(null)
                            .build()
                    , HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
        return responseEntity;
    }

    @PutMapping("/estado/{id}")
    @ResponseStatus(HttpStatus.OK)
    public boolean estado(@PathVariable("id") Long id) {
        IglesiaDto iglesiaDto = iglesiaService.findById(id);
        ResponseEntity<?> responseEntity;
        try {
//            iglesiaDto = iglesiaService.findById(id);
            iglesiaDto.setEstado(!iglesiaDto.getEstado());
            iglesiaService.save(iglesiaDto);
            IglesiaDto iglesiaUpdate = iglesiaService.save(iglesiaDto);
            responseEntity = new ResponseEntity<>(
                    MessageResponse.builder()
                            .message("Iglesia actualizada exitosamente.")
                            .datos(iglesiaUpdate)
                            .nombreModelo("Iglesia")
                            .build(),
                    HttpStatus.OK
            );
        } catch (DataAccessException e) {
            responseEntity = new ResponseEntity<>(
                    MessageResponse.builder()
                            .message("Error al actualizar la iglesia.")
                            .datos(null)
                            .build(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
        return iglesiaDto.getEstado();
    }

    //busca segun nombre de iglesia si ya existe
    @GetMapping("/showbynombreiglesia/{nameIglesia}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> buscarNombreIglesia(@PathVariable("nameIglesia") String nameIglesia) {
        ResponseEntity<?> responseEntity;
        try {
//            PersonaDto personaFiedById = personaService.buscarCi(ci);
            IglesiaDto buscarNombreIglesia = iglesiaService.buscarNombreIglesia(nameIglesia);

            if (buscarNombreIglesia == null) {
                responseEntity = new ResponseEntity<>(
                        MessageResponse.builder()
                                .message("Iglesia no encontrada.")
                                .datos(null)
                                .build(),
                        HttpStatus.NOT_FOUND
                );
            } else {
                responseEntity = new ResponseEntity<>(
                        MessageResponse.builder()
                                .message("Persona encontrada.")
                                .datos(buscarNombreIglesia)
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

