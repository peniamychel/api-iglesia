package com.mcmm.controller;

import com.mcmm.exception.InternalServerErrorExceptionResource;
import com.mcmm.exception.NotFoundExceptionResource;
import com.mcmm.model.dto.CargoDto;
import com.mcmm.model.payload.ApiResponse;
import com.mcmm.service.ICargo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cargo/v1")
@PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO')")
public class CargoController {
    @Autowired
    private ICargo cargoService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApiResponse<CargoDto>> create(@RequestBody CargoDto cargoDto) {
        ResponseEntity<ApiResponse<CargoDto>> responseEntity;
        try {
            CargoDto cargoSave = cargoService.create(cargoDto);
            responseEntity = new ResponseEntity<>(
                    ApiResponse.<CargoDto>builder()
                            .message("Cargo guardada exitosamente.")
                            .datos(cargoSave)
                            .nombreModelo("Cargo")
                            .build(),
                    HttpStatus.CREATED);
        } catch (DataAccessException e) {
            responseEntity = new ResponseEntity<>(
                    ApiResponse.<CargoDto>builder()
                            .message("Error al guardar la Cargo.")
                            .datos(null)
                            .build(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return responseEntity;
    }

    @GetMapping("/findall")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApiResponse<Iterable<CargoDto>>> findAll() {
        ResponseEntity<ApiResponse<Iterable<CargoDto>>> responseEntity;
        Iterable<CargoDto> cargoDtos = cargoService.findAll();
        try {
            responseEntity = new ResponseEntity<>(
                    ApiResponse.<Iterable<CargoDto>>builder()
                            .message("Listado de Cargos")
                            .datos(cargoDtos)
                            .nombreModelo("Cargo")
                            .build(),
                    HttpStatus.OK);
        } catch (Exception e) {
            responseEntity = new ResponseEntity<>(
                    ApiResponse.<Iterable<CargoDto>>builder()
                            .message("No se encontro datos.")
                            .datos(null)
                            .build(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return responseEntity;
    }

    @PutMapping("/update")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApiResponse<CargoDto>> update(@RequestBody CargoDto cargoDto) {
        ResponseEntity<ApiResponse<CargoDto>> responseEntity;
        try {
            CargoDto cargoUpdate = cargoService.update(cargoDto);
            responseEntity = new ResponseEntity<>(
                    ApiResponse.<CargoDto>builder()
                            .message("Cargo actualizado exitosamente.")
                            .datos(cargoUpdate)
                            .nombreModelo("Cargo")
                            .build(),
                    HttpStatus.OK);
        } catch (DataAccessException e) {
            responseEntity = new ResponseEntity<>(
                    ApiResponse.<CargoDto>builder()
                            .message("Error al actualizar Cargo.")
                            .datos(null)
                            .build(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return responseEntity;
    }

    @GetMapping("/showbyid/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApiResponse<CargoDto>> showById(@PathVariable("id") Long id) {
        ResponseEntity<ApiResponse<CargoDto>> responseEntity;
        try {
            CargoDto cargoFiedById = cargoService.findById(id);
            if (cargoFiedById == null) {
                responseEntity = new ResponseEntity<>(
                        ApiResponse.<CargoDto>builder()
                                .message("Cargo no encontrado.")
                                .datos(null)
                                .build(),
                        HttpStatus.NOT_FOUND);
            } else {
                responseEntity = new ResponseEntity<>(
                        ApiResponse.<CargoDto>builder()
                                .message("Cargo encontrado.")
                                .datos(cargoFiedById)
                                .nombreModelo("Cargo")
                                .build(),
                        HttpStatus.OK);
            }
        } catch (DataAccessException e) {
            responseEntity = new ResponseEntity<>(
                    ApiResponse.<CargoDto>builder()
                            .message("Error al buscar Cargo.")
                            .datos(null)
                            .build(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return responseEntity;
    }

    @PutMapping("/estado/{id}")
    @ResponseStatus(HttpStatus.OK)
    public boolean estado(@PathVariable("id") Long id) {
        CargoDto cargoDto;
        try {
            cargoDto = cargoService.findById(id);
            if (cargoDto == null) {
                throw new NotFoundExceptionResource("Cargo", "id", id);
            }
            cargoService.estado(id);

        } catch (DataAccessException e) {
            throw new InternalServerErrorExceptionResource(e.getMessage());
        }
        return !cargoDto.getEstado();
    }
}
