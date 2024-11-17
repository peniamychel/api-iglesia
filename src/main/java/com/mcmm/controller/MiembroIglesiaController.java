package com.mcmm.controller;


import com.mcmm.model.dto.GraficoDataDto;
import com.mcmm.model.dto.IglesiaDto;
import com.mcmm.model.dto.MiembroDto;
import com.mcmm.model.dto.MiembroIglesiaDto;
import com.mcmm.model.payload.MessageResponse;
import com.mcmm.service.IIglesia;
import com.mcmm.service.IMiembro;
import com.mcmm.service.IMiembroIglesia;
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

import java.util.List;

@RestController
@RequestMapping("/api/miembroiglesia/v1")
@PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO')")
public class MiembroIglesiaController {


    private static final Logger logger = LoggerFactory.getLogger(MiembroImpl.class); //uso para debuggear

    @Autowired
    private IMiembroIglesia miembroIglesiaService;
    @Autowired
    private IMiembro miembroService;
    @Autowired
    private IIglesia iglesiaService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> create(@RequestBody MiembroIglesiaDto miembroDto) {
        ResponseEntity<?> responseEntity;
        try {
            MiembroIglesiaDto miembroIglesiaSave = miembroIglesiaService.save(miembroDto);
            responseEntity = new ResponseEntity<>(
                    MessageResponse.builder()
                            .message("Miembro guardada exitosamente.")
                            .datos(miembroIglesiaSave)
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

    @PostMapping("/created")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> created(@RequestBody MiembroIglesiaDto miembroIglesiaDto) {
        ResponseEntity<?> responseEntity = null;
        try {
            if(miembroIglesiaDto != null) {

                IglesiaDto iglesiaDto;
                MiembroDto miembroDto;
                miembroDto = miembroService.findById(miembroIglesiaDto.getIglesiaId());
                iglesiaDto = iglesiaService.findById(miembroIglesiaDto.getMiembroId());

                if(miembroDto != null && iglesiaDto != null) {
                    boolean result = miembroIglesiaService.findByIdMiembro(miembroDto.getId());
                    if(result){
                        MiembroIglesiaDto miembrosIglesiaSave = miembroIglesiaService.save(miembroIglesiaDto);
                        if(miembrosIglesiaSave != null) {
                            responseEntity = new ResponseEntity<>(
                                    MessageResponse.builder()
                                            .message("Miembros guardada exitosamente.")
                                            .datos(miembrosIglesiaSave)
                                            .nombreModelo("MiembroIglesia")
                                            .build(),
                                    HttpStatus.CREATED
                            );
                        }else{
                            responseEntity = new ResponseEntity<>(
                                    MessageResponse.builder()
                                            .message("Error al guardar la MiembroIglesia.")
                                            .datos(null)
                                            .build(),
                                    HttpStatus.INTERNAL_SERVER_ERROR
                            );
                        }
                    }
                    else{
                        responseEntity = new ResponseEntity<>(
                                MessageResponse.builder()
                                        .message("Miembro ya pertenece a una la iglesia.")
                                        .datos(null)
                                        .build(),
                                HttpStatus.INTERNAL_SERVER_ERROR
                        );
                    }
                }
                else{
                    responseEntity = new ResponseEntity<>(
                            MessageResponse.builder()
                                    .message("Miembro o iglesia no encontrados.")
                                    .datos(null)
                                    .build(),
                            HttpStatus.INTERNAL_SERVER_ERROR
                    );
                }

            }
        } catch (DataAccessException e) {
            responseEntity = new ResponseEntity<>(
                    MessageResponse.builder()
                            .message("Error : " + e.getMessage() + ".")
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
        Iterable<MiembroIglesiaDto> miembroIglesiaDtos = miembroIglesiaService.findAll();
        try {
            responseEntity = new ResponseEntity<>(
                    MessageResponse.builder()
                            .message("Listado de MiembrosIglesia")
                            .datos(miembroIglesiaDtos)
                            .nombreModelo("MiembroIglesia")
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
            MiembroIglesiaDto miembroIglesiaDto = miembroIglesiaService.findById(id);
            if (miembroIglesiaDto == null) {
                responseEntity = new ResponseEntity<>(
                        MessageResponse.builder()
                                .message("MiembroIglesia no encontrado.")
                                .datos(null)
                                .build(),
                        HttpStatus.NOT_FOUND
                );
            } else {
                responseEntity = new ResponseEntity<>(
                        MessageResponse.builder()
                                .message("Miembro encontrado.")
                                .datos(miembroIglesiaDto)
                                .nombreModelo("Miembro")
                                .build(),
                        HttpStatus.OK
                );
            }
        } catch (DataAccessException e) {
            responseEntity = new ResponseEntity<>(
                    MessageResponse.builder()
                            .message("Error al buscar el miembro iglesia.")
                            .datos(null)
                            .build(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
        return responseEntity;
    }

    @PutMapping("/update")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> update( @RequestBody MiembroIglesiaDto miembroIglesiaDto) {
        ResponseEntity<?> responseEntity;
        try {
            // Intentamos actualizar el miembro con los datos proporcionados

            MiembroIglesiaDto miembroIglesiaActualizado = miembroIglesiaService.update(miembroIglesiaDto);

            // Si el miembro no se encontró, retornamos un error 404
            if (miembroIglesiaActualizado == null) {
                responseEntity = new ResponseEntity<>(
                        MessageResponse.builder()
                                .message("El miembro iglesia con ID " + miembroIglesiaActualizado.getId() + " no existe.")
                                .datos(null)
                                .nombreModelo("MiembroIglesia")
                                .build(),
                        HttpStatus.NOT_FOUND
                );
            } else {
                // Si el miembro se actualizó correctamente, retornamos el miembro actualizado
                responseEntity = new ResponseEntity<>(
                        MessageResponse.builder()
                                .message("Miembro iglesia actualizado exitosamente.")
                                .datos(miembroIglesiaActualizado)
                                .nombreModelo("Miembro")
                                .build(),
                        HttpStatus.OK
                );
            }
        } catch (DataAccessException e) {
            // Si ocurre un error en el acceso a datos, devolvemos un error 500
            responseEntity = new ResponseEntity<>(
                    MessageResponse.builder()
                            .message("Error al actualizar el miembro iglesia. Error Catch: "+ e.getMessage())
                            .datos(null)
                            .build(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
        return responseEntity;
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<?> delete(@PathVariable MiembroIglesiaDto miembroIglesiaDto) {
        ResponseEntity<?> responseEntity;
        try {
            // Llamamos al método delete del servicio para eliminar el miembro
            miembroIglesiaService.delete(miembroIglesiaDto);
            responseEntity = new ResponseEntity<>(
                    MessageResponse.builder()
                            .message("MiembroIglesia eliminado exitosamente.")
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
                            .nombreModelo("MiembroIglesia")
                            .build(),
                    HttpStatus.NOT_FOUND
            );
        } catch (DataAccessException e) {
            // Si hay un error en el acceso a datos, retornamos un error 500
            responseEntity = new ResponseEntity<>(
                    MessageResponse.builder()
                            .message("Error al eliminar el miembroIglesia.")
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
            boolean miembroIglesiaEstado = miembroIglesiaService.estado(id);

            // Si el miembro no se encontró, retornamos un error 404
            if (miembroIglesiaEstado) {
                responseEntity = new ResponseEntity<>(
                        MessageResponse.builder()
                                .message("El miembro con ID " + id + " no existe.")
                                .datos(true)
                                .nombreModelo("Miembro")
                                .build(),
                        HttpStatus.NOT_FOUND
                );
            } else {
                // Si el miembro se actualizó correctamente, retornamos el miembro actualizado
                responseEntity = new ResponseEntity<>(
                        MessageResponse.builder()
                                .message("Miembro se cabio el estado exitosamente")
                                .datos(false)
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

    @PutMapping("/traspaso")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> traspaso( @RequestBody MiembroIglesiaDto miembroIglesiaDto) {
        ResponseEntity<?> responseEntity;
        logger.debug("\n \n LOGS...........: {} \n \n", miembroIglesiaDto);
        try {
            // Intentamos actualizar el miembro con los datos proporcionados

            MiembroIglesiaDto miembroIglesiaActualizado = miembroIglesiaService.update(miembroIglesiaDto);

            // Si el miembro no se encontró, retornamos un error 404
            if (miembroIglesiaActualizado == null) {
                responseEntity = new ResponseEntity<>(
                        MessageResponse.builder()
                                .message("El miembro iglesia con ID " + miembroIglesiaActualizado.getId() + " no existe.")
                                .datos(null)
                                .nombreModelo("MiembroIglesia")
                                .build(),
                        HttpStatus.NOT_FOUND
                );
            } else {
                // Si el miembro se actualizó correctamente, retornamos el miembro actualizado
                responseEntity = new ResponseEntity<>(
                        MessageResponse.builder()
                                .message("Miembro iglesia actualizado exitosamente.")
                                .datos(miembroIglesiaActualizado)
                                .nombreModelo("Miembro")
                                .build(),
                        HttpStatus.OK
                );
            }
        } catch (DataAccessException e) {
            // Si ocurre un error en el acceso a datos, devolvemos un error 500
            responseEntity = new ResponseEntity<>(
                    MessageResponse.builder()
                            .message("Error al actualizar el miembro iglesia. Error Catch: "+ e.getMessage())
                            .datos(null)
                            .build(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
        return responseEntity;
    }


    //Da una lista de Miembros que estan en una iglesia dado el id de la iglesia
    @GetMapping("/listmiembrosiglesia/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> findMiembrosIglesia(@PathVariable("id") Long id) {
        ResponseEntity<?> responseEntity;
        try {

            Iterable<MiembroDto> miembroDtos = miembroIglesiaService.findMiembrosIglesia(id);
            if (miembroDtos == null) {
                responseEntity = new ResponseEntity<>(
                        MessageResponse.builder()
                                .message("Miembros de la iglesia de id: " + id + " no encontrado.")
                                .datos(null)
                                .build(),
                        HttpStatus.NOT_FOUND
                );
            } else {
                responseEntity = new ResponseEntity<>(
                        MessageResponse.builder()
                                .message("Miembros del a la iglesa de id: " + id + " encontrados.")
                                .datos(miembroDtos)
                                .nombreModelo("Miembro")
                                .build(),
                        HttpStatus.OK
                );
            }
        } catch (DataAccessException e) {
            responseEntity = new ResponseEntity<>(
                    MessageResponse.builder()
                            .message("Error al buscar el miembro iglesia.")
                            .datos(null)
                            .build(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
        return responseEntity;
    }

    //graficos para miembros iglesia
    @GetMapping("/graficomiembrosiglesia/{cant}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> graficoMiembrosIglesia(@PathVariable("cant") Long cant) {
        ResponseEntity<?> responseEntity;
        try {

            List<GraficoDataDto> miembroIglesia = miembroIglesiaService.graficoMiembrosIglesia(cant);
            if (miembroIglesia == null) {
                responseEntity = new ResponseEntity<>(
                        MessageResponse.builder()
                                .message("Sin grafico")
                                .datos(null)
                                .build(),
                        HttpStatus.NOT_FOUND
                );
            } else {
                responseEntity = new ResponseEntity<>(
                        MessageResponse.builder()
                                .message("Grafico para " + cant + " Iglesias.")
                                .datos(miembroIglesia)
                                .nombreModelo("Miembro")
                                .build(),
                        HttpStatus.OK
                );
            }
        } catch (DataAccessException e) {
            responseEntity = new ResponseEntity<>(
                    MessageResponse.builder()
                            .message("Error al generar los datos para el grafico."+e.getMessage())
                            .datos(null)
                            .build(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
        return responseEntity;
    }

}
