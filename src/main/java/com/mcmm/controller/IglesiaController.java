package com.mcmm.controller;

import com.mcmm.exception.BadRequestException;
import com.mcmm.exception.InternalServerErrorExceptionResource;
import com.mcmm.exception.NotFoundExceptionResource;
import com.mcmm.model.dto.iglesiaDto.IglesiaDto;
import com.mcmm.model.payload.ApiResponse;
import com.mcmm.service.IIglesia;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/iglesia/v1")
@PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO')")
public class IglesiaController {
    @Autowired
    private IIglesia iglesiaService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApiResponse<IglesiaDto>> create(@RequestBody @Valid IglesiaDto iglesiaDto) {
        ResponseEntity<ApiResponse<IglesiaDto>> responseEntity;
        try {
            // controlar que el nombre no se este insertando duplicado
            IglesiaDto existeIglesia = iglesiaService.buscarNombreIglesia(iglesiaDto.getNombre());
            if (existeIglesia != null) {
                throw new BadRequestException("El nombre de la iglesia ya existe.");
            }

            IglesiaDto iglesiaSave = iglesiaService.save(iglesiaDto);
            responseEntity = new ResponseEntity<>(
                    ApiResponse.<IglesiaDto>builder()
                            .message("Iglesia guardada exitosamente.")
                            .datos(iglesiaSave)
                            .nombreModelo("Iglesia")
                            .build(),
                    HttpStatus.CREATED);
        } catch (DataAccessException e) {
            throw new BadRequestException(e.getMessage());
        }
        return responseEntity;
    }

    @PutMapping("/update")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApiResponse<IglesiaDto>> update(@RequestBody @Valid IglesiaDto iglesiaDto) {
        ResponseEntity<ApiResponse<IglesiaDto>> responseEntity;
        try {
            IglesiaDto iglesiaUpdate = iglesiaService.save(iglesiaDto);
            responseEntity = new ResponseEntity<>(
                    ApiResponse.<IglesiaDto>builder()
                            .message("Iglesia actualizada exitosamente.")
                            .datos(iglesiaUpdate)
                            .nombreModelo("Iglesia")
                            .build(),
                    HttpStatus.OK);
        } catch (DataAccessException e) {
            throw new BadRequestException(e.getMessage());
        }
        return responseEntity;
    }

    @PutMapping("/update2/{id}") // Usamos el ID en la ruta
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApiResponse<IglesiaDto>> update(
            @PathVariable Long id,
            @RequestBody @Valid IglesiaDto iglesiaDto) {
        // Es buena práctica asegurar que el ID de la URL coincida con el del DTO
        iglesiaDto.setId(id);
        try {
            IglesiaDto iglesiaUpdate = iglesiaService.save(iglesiaDto);

            return ResponseEntity.ok(
                    ApiResponse.<IglesiaDto>builder()
                            .message("Iglesia actualizada exitosamente.")
                            .datos(iglesiaUpdate)
                            .nombreModelo("Iglesia")
                            .build());
        } catch (DataAccessException e) {
            throw new BadRequestException("Error al acceder a la base de datos: " + e.getMessage());
        }
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
                        ApiResponse.builder()
                                .message("Iglesia eliminada exitosamente.")
                                .datos(iglesiaDelete)
                                .nombreModelo("Iglesia")
                                .build(),
                        HttpStatus.OK);
            } else {
                throw new NotFoundExceptionResource("Iglesia", "id", id);
            }
        } catch (DataAccessException exDta) {
            throw new InternalServerErrorExceptionResource(exDta.getMessage());
        }
        return responseEntity;
    }

    @GetMapping("/showbyid/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApiResponse<IglesiaDto>> showById(@PathVariable("id") Long id) {
        ResponseEntity<ApiResponse<IglesiaDto>> responseEntity;
        try {
            IglesiaDto iglesiaFiedById = iglesiaService.findById(id);
            if (iglesiaFiedById == null) {
                throw new NotFoundExceptionResource("Iglesia", "id", id);
            } else {
                responseEntity = new ResponseEntity<>(
                        ApiResponse.<IglesiaDto>builder()
                                .message("Iglesia encontrada.")
                                .datos(iglesiaFiedById)
                                .nombreModelo("Iglesia")
                                .build(),
                        HttpStatus.OK);
            }
        } catch (DataAccessException e) {
            throw new BadRequestException(e.getMessage());
        }
        return responseEntity;
    }

    @GetMapping("/findall")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApiResponse<Iterable<IglesiaDto>>> findAll() {
        ResponseEntity<ApiResponse<Iterable<IglesiaDto>>> responseEntity;
        try {
            Iterable<IglesiaDto> iglesiaDtos = iglesiaService.findAll();
            responseEntity = new ResponseEntity<>(
                    ApiResponse.<Iterable<IglesiaDto>>builder()
                            .message("Listado de iglesias")
                            .datos(iglesiaDtos)
                            .nombreModelo("iglesia")
                            .build(),
                    HttpStatus.OK);
        } catch (Exception e) {
            throw new InternalServerErrorExceptionResource(e.getMessage());
        }
        return responseEntity;
    }

    @PutMapping("/estado/{id}")
    @ResponseStatus(HttpStatus.OK)
    public boolean estado(@PathVariable("id") Long id) {
        IglesiaDto iglesiaDto;
        try {
            iglesiaDto = iglesiaService.findById(id);
            if (iglesiaDto == null) {
                throw new NotFoundExceptionResource("Iglesia", "id", id);
            }
            iglesiaDto.setEstado(!iglesiaDto.getEstado());
            iglesiaService.save(iglesiaDto);

        } catch (DataAccessException e) {
            throw new InternalServerErrorExceptionResource(e.getMessage());
        }
        return iglesiaDto.getEstado();
    }

    // busca segun nombre de iglesia si ya existe
    @GetMapping("/showbynombreiglesia/{nameIglesia}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApiResponse<IglesiaDto>> buscarNombreIglesia(
            @PathVariable("nameIglesia") String nameIglesia) {
        ResponseEntity<ApiResponse<IglesiaDto>> responseEntity;
        try {
            IglesiaDto buscarNombreIglesia = iglesiaService.buscarNombreIglesia(nameIglesia);

            if (buscarNombreIglesia == null) {
                throw new NotFoundExceptionResource("Iglesia", "nameIglesia", nameIglesia);
            } else {
                responseEntity = new ResponseEntity<>(
                        ApiResponse.<IglesiaDto>builder()
                                .message("Persona encontrada.")
                                .datos(buscarNombreIglesia)
                                .nombreModelo("Persona")
                                .build(),
                        HttpStatus.OK);
            }
        } catch (DataAccessException e) {
            throw new InternalServerErrorExceptionResource(e.getMessage());
        }
        return responseEntity;
    }

    // busca segun nombre de iglesia si ya existe
    @GetMapping("/showbynombreiglesiaexceptoid/{nameIglesia}/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApiResponse<IglesiaDto>> buscarNombreIglesiaExceptoId(
            @PathVariable("nameIglesia") String nameIglesia, @PathVariable("id") Long id) {
        ResponseEntity<ApiResponse<IglesiaDto>> responseEntity;
        try {
            IglesiaDto buscarNombreIglesia = iglesiaService.buscarNombreIglesiaExceptoId(id, nameIglesia);

            if (buscarNombreIglesia == null) {
                throw new NotFoundExceptionResource("Iglesia", "nameIglesia", nameIglesia);
            } else {
                responseEntity = new ResponseEntity<>(
                        ApiResponse.<IglesiaDto>builder()
                                .message("Persona encontrada.")
                                .datos(buscarNombreIglesia)
                                .nombreModelo("Persona")
                                .build(),
                        HttpStatus.OK);
            }
        } catch (DataAccessException e) {
            throw new InternalServerErrorExceptionResource(e.getMessage());
        }
        return responseEntity;
    }
}
