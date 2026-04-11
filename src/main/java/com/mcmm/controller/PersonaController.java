package com.mcmm.controller;

import com.mcmm.model.dto.personaDto.PersonaDto;
import com.mcmm.model.payload.ApiResponse;
import com.mcmm.service.IPersona;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

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
                    ApiResponse.builder()
                            .message("Persona guardada exitosamente.")
                            .datos(personaSave)
                            .build(),
                    HttpStatus.CREATED
            );
        } catch (DataAccessException e) {
            responseEntity = new ResponseEntity<>(
                    ApiResponse.builder()
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
                    ApiResponse.builder()
                            .message("Listado de Personas")
                            .datos(personaDtos)
                            .build()
                    , HttpStatus.OK
            );
        } catch (Exception e) {
            responseEntity = new ResponseEntity<>(
                    ApiResponse.builder()
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
                    ApiResponse.builder()
                            .message("Persona actualizada exitosamente.")
                            .datos(personaUpdate)
                            .build(),
                    HttpStatus.OK
            );
        } catch (DataAccessException e) {
            responseEntity = new ResponseEntity<>(
                    ApiResponse.builder()
                            .message("Error al actualizar Persona.")
                            .datos(null)
                            .build(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
        return responseEntity;
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> partialUpdate(@PathVariable Long id, @RequestBody PersonaDto partialDto) {
        ResponseEntity<?> responseEntity;
        try {
            // Valida que al menos un campo no sea null (opcional, para evitar PATCH vacío)
            if (partialDto.getNombre() == null && partialDto.getApellido() == null &&
                    partialDto.getCi() == null && partialDto.getFechaNac() == null &&
                    partialDto.getCelular() == null && partialDto.getSexo() == null &&
                    partialDto.getDireccion() == null && partialDto.getEstado() == null) {
                return ResponseEntity.badRequest().body(
                        ApiResponse.builder()
                                .message("Debe proporcionar al menos un campo para actualizar.")
                                .datos(null)
                                .build()
                );
            }

            PersonaDto personaUpdated = personaService.partialUpdate(id, partialDto);
            responseEntity = new ResponseEntity<>(
                    ApiResponse.builder()
                            .message("Persona actualizada parcialmente exitosamente.")
                            .datos(personaUpdated)
                            .build(),
                    HttpStatus.OK
            );
        } catch (EntityNotFoundException e) {
            responseEntity = new ResponseEntity<>(
                    ApiResponse.builder()
                            .message(e.getMessage())
                            .datos(null)
                            .build(),
                    HttpStatus.NOT_FOUND
            );
        } catch (DataAccessException e) {
            responseEntity = new ResponseEntity<>(
                    ApiResponse.builder()
                            .message("Error al actualizar parcialmente la Persona.")
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
                        ApiResponse.builder()
                                .message("Persona eliminada exitosamente.")
                                .datos(personaDelete)
                                .build(),
                        HttpStatus.OK
                );
            } else {
                responseEntity = new ResponseEntity<>(
                        ApiResponse.builder()
                                .message("La Persona con ID " + id + " no existe.")
                                .datos(null)
                                .build(),
                        HttpStatus.NOT_FOUND
                );
            }
        } catch (DataAccessException exDta) {
            responseEntity = new ResponseEntity<>(
                    ApiResponse.builder()
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
                        ApiResponse.builder()
                                .message("Persona no encontrada.")
                                .datos(null)
                                .build(),
                        HttpStatus.NOT_FOUND
                );
            } else {
                responseEntity = new ResponseEntity<>(
                        ApiResponse.builder()
                                .message("Persona encontrada.")
                                .datos(personaFiedById)
                                .build(),
                        HttpStatus.OK
                );
            }
        } catch (DataAccessException e) {
            responseEntity = new ResponseEntity<>(
                    ApiResponse.builder()
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
                    ApiResponse.builder()
                            .message("Listado de Personas")
                            .datos(personaDtos)
                            .build()
                    , HttpStatus.OK
            );
        } catch (Exception e) {
            responseEntity = new ResponseEntity<>(
                    ApiResponse.builder()
                            .message("No se encontro datos.")
                            .datos(null)
                            .build()
                    , HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
        return responseEntity;
    }

    //busca segun cedula identidad
    @GetMapping("/showbyci/{ci}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> buscarCi(@PathVariable("ci") String ci) {
        ResponseEntity<?> responseEntity;
        try {
//            PersonaDto personaFiedById = personaService.buscarCi(ci);
            PersonaDto personaFiedById = personaService.buscarCi(ci);

            if (personaFiedById == null) {
                responseEntity = new ResponseEntity<>(
                        ApiResponse.builder()
                                .message("Persona no encontrada.")
                                .datos(null)
                                .build(),
                        HttpStatus.NOT_FOUND
                );
            } else {
                responseEntity = new ResponseEntity<>(
                        ApiResponse.builder()
                                .message("Persona encontrada.")
                                .datos(personaFiedById)
                                .build(),
                        HttpStatus.OK
                );
            }
        } catch (DataAccessException e) {
            responseEntity = new ResponseEntity<>(
                    ApiResponse.builder()
                            .message("Error al buscar la Persona.")
                            .datos(null)
                            .build(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
        return responseEntity;
    }
    @PostMapping("/{id}/foto")
    public ResponseEntity<?> uploadProfilePhoto(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        try {
            String fileUrl = personaService.updateProfilePhoto(id, file);
            Map<String, String> response = new HashMap<>();
            response.put("uriFoto", fileUrl);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
