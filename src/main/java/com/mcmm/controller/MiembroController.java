package com.mcmm.controller;


import com.mcmm.model.dto.MiembroDto;
import com.mcmm.model.payload.MessageResponse;
import com.mcmm.service.IMiembro;
import com.mcmm.service.impl.MiembroImpl;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/miembro/v1")
@PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO')")
public class MiembroController {
    private static final Logger logger = LoggerFactory.getLogger(MiembroImpl.class);

    @Autowired
    private IMiembro miembroService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> create(@RequestBody MiembroDto miembroDto) {
        ResponseEntity<?> responseEntity;
        try {
            MiembroDto miembroSave = miembroService.create(miembroDto);
            responseEntity = new ResponseEntity<>(
                    MessageResponse.builder()
                            .message("Miembro guardada exitosamente.")
                            .datos(miembroSave)
                            .nombreModelo("Miembro")
                            .build(),
                    HttpStatus.CREATED
            );
        } catch (DataAccessException e) {
            responseEntity = new ResponseEntity<>(
                    MessageResponse.builder()
                            .message("Error al guardar la Miembro.")
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
        Iterable<MiembroDto> miembroDtos = miembroService.findAll();
        try {
            responseEntity = new ResponseEntity<>(
                    MessageResponse.builder()
                            .message("Listado de Miembros")
                            .datos(miembroDtos)
                            .nombreModelo("Miembro")
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

    // Método para encontrar un miembro por ID
    @GetMapping("/showbyid/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> findById(@PathVariable("id") Long id) {
        ResponseEntity<?> responseEntity;
        try {
            MiembroDto miembroDto = miembroService.findById(id);
            if (miembroDto == null) {
                responseEntity = new ResponseEntity<>(
                        MessageResponse.builder()
                                .message("Miembro no encontrado.")
                                .datos(null)
                                .build(),
                        HttpStatus.NOT_FOUND
                );
            } else {
                responseEntity = new ResponseEntity<>(
                        MessageResponse.builder()
                                .message("Miembro encontrado.")
                                .datos(miembroDto)
                                .nombreModelo("Miembro")
                                .build(),
                        HttpStatus.OK
                );
            }
        } catch (DataAccessException e) {
            responseEntity = new ResponseEntity<>(
                    MessageResponse.builder()
                            .message("Error al buscar el miembro.")
                            .datos(null)
                            .build(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
        return responseEntity;
    }


    @PutMapping("/update")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> update( @RequestBody MiembroDto miembroDto) {
        ResponseEntity<?> responseEntity;
        logger.debug("\n \n LOGS...........: {} \n \n", miembroDto);
        try {
            // Intentamos actualizar el miembro con los datos proporcionados

            MiembroDto miembroActualizado = miembroService.update(miembroDto);

            // Si el miembro no se encontró, retornamos un error 404
            if (miembroActualizado == null) {
                responseEntity = new ResponseEntity<>(
                        MessageResponse.builder()
                                .message("El miembro con ID " + miembroActualizado.getId() + " no existe.")
                                .datos(null)
                                .nombreModelo("Miembro")
                                .build(),
                        HttpStatus.NOT_FOUND
                );
            } else {
                // Si el miembro se actualizó correctamente, retornamos el miembro actualizado
                responseEntity = new ResponseEntity<>(
                        MessageResponse.builder()
                                .message("Miembro actualizado exitosamente.")
                                .datos(miembroActualizado)
                                .nombreModelo("Miembro")
                                .build(),
                        HttpStatus.OK
                );
            }
        } catch (DataAccessException e) {
            // Si ocurre un error en el acceso a datos, devolvemos un error 500
            responseEntity = new ResponseEntity<>(
                    MessageResponse.builder()
                            .message("Error al actualizar el miembro. Error Catch: "+ e.getMessage())
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
            // Llamamos al método delete del servicio para eliminar el miembro
            miembroService.delete(id);
            responseEntity = new ResponseEntity<>(
                    MessageResponse.builder()
                            .message("Miembro eliminado exitosamente.")
                            .datos(null)
                            .nombreModelo("Miembro")
                            .build(),
                    HttpStatus.OK
            );
        } catch (EntityNotFoundException e) {
            // Si el miembro no existe, retornamos un error 404
            responseEntity = new ResponseEntity<>(
                    MessageResponse.builder()
                            .message(e.getMessage())
                            .datos(null)
                            .nombreModelo("Miembro")
                            .build(),
                    HttpStatus.NOT_FOUND
            );
        } catch (DataAccessException e) {
            // Si hay un error en el acceso a datos, retornamos un error 500
            responseEntity = new ResponseEntity<>(
                    MessageResponse.builder()
                            .message("Error al eliminar el miembro.")
                            .datos(null)
                            .build(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
        return responseEntity;
    }

    @PutMapping("/estado/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> estado(@PathVariable Long id) {
        ResponseEntity<?> responseEntity;
        try {
            // Intentamos actualizar el miembro con los datos proporcionados
            MiembroDto miembroActualizado = miembroService.estado(id);

            // Si el miembro no se encontró, retornamos un error 404
            if (miembroActualizado == null) {
                responseEntity = new ResponseEntity<>(
                        MessageResponse.builder()
                                .message("El miembro con ID " + id + " no existe.")
                                .datos(null)
                                .nombreModelo("Miembro")
                                .build(),
                        HttpStatus.NOT_FOUND
                );
            } else {
                // Si el miembro se actualizó correctamente, retornamos el miembro actualizado
                responseEntity = new ResponseEntity<>(
                        MessageResponse.builder()
                                .message("Miembro se cabio el estado exitosamente a: "+ miembroActualizado.getEstado())
                                .datos(miembroActualizado)
                                .nombreModelo("Miembro")
                                .build(),
                        HttpStatus.OK
                );
            }
        } catch (DataAccessException e) {
            // Si ocurre un error en el acceso a datos, devolvemos un error 500
            responseEntity = new ResponseEntity<>(
                    MessageResponse.builder()
                            .message("Error al al cambiar el estado.")
                            .datos(null)
                            .build(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
        return responseEntity;
    }
}
