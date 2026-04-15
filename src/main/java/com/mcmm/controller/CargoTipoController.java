package com.mcmm.controller;

import com.mcmm.exception.InternalServerErrorExceptionResource;
import com.mcmm.exception.NotFoundExceptionResource;
import com.mcmm.model.dto.CargoTipoDto;
import com.mcmm.model.payload.ApiResponse;
import com.mcmm.service.ICargoTipo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tipocargo/v1")
@PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO')")
public class CargoTipoController {

    @Autowired
    private ICargoTipo cargoTipoService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApiResponse<CargoTipoDto>> create(@RequestBody @Valid CargoTipoDto cargoTipoDto) {
        ResponseEntity<ApiResponse<CargoTipoDto>> responseEntity;
        try {
            CargoTipoDto cargoTipoSave = cargoTipoService.create(cargoTipoDto);
            responseEntity = new ResponseEntity<>(
                    ApiResponse.<CargoTipoDto>builder()
                            .message("CargoTipo guardado exitosamente.")
                            .datos(cargoTipoSave)
                            .nombreModelo("CargoTipo")
                            .build(),
                    HttpStatus.CREATED);
        } catch (DataAccessException e) {
            responseEntity = new ResponseEntity<>(
                    ApiResponse.<CargoTipoDto>builder()
                            .message("Error al guardar el CargoTipo.")
                            .datos(null)
                            .build(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return responseEntity;
    }

    @GetMapping("/findall")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApiResponse<Iterable<CargoTipoDto>>> findAll() {
        ResponseEntity<ApiResponse<Iterable<CargoTipoDto>>> responseEntity;
        Iterable<CargoTipoDto> cargoTipoDtos = cargoTipoService.findAll();
        try {
            responseEntity = new ResponseEntity<>(
                    ApiResponse.<Iterable<CargoTipoDto>>builder()
                            .message("Listado de CargoTipos")
                            .datos(cargoTipoDtos)
                            .nombreModelo("CargoTipo")
                            .build(),
                    HttpStatus.OK);
        } catch (Exception e) {
            responseEntity = new ResponseEntity<>(
                    ApiResponse.<Iterable<CargoTipoDto>>builder()
                            .message("No se encontraron datos.")
                            .datos(null)
                            .build(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return responseEntity;
    }

    @PutMapping("/update")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApiResponse<CargoTipoDto>> update(@RequestBody CargoTipoDto cargoTipoDto) {
        ResponseEntity<ApiResponse<CargoTipoDto>> responseEntity;
        try {
            CargoTipoDto cargoTipoUpdate = cargoTipoService.update(cargoTipoDto);
            responseEntity = new ResponseEntity<>(
                    ApiResponse.<CargoTipoDto>builder()
                            .message("CargoTipo actualizado exitosamente.")
                            .datos(cargoTipoUpdate)
                            .nombreModelo("CargoTipo")
                            .build(),
                    HttpStatus.OK);
        } catch (DataAccessException e) {
            responseEntity = new ResponseEntity<>(
                    ApiResponse.<CargoTipoDto>builder()
                            .message("Error al actualizar CargoTipo.")
                            .datos(null)
                            .build(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return responseEntity;
    }

    @GetMapping("/showbyid/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApiResponse<CargoTipoDto>> showById(@PathVariable("id") Long id) {
        ResponseEntity<ApiResponse<CargoTipoDto>> responseEntity;
        try {
            CargoTipoDto cargoTipoDtoFindById = cargoTipoService.findById(id);
            if (cargoTipoDtoFindById == null) {
                responseEntity = new ResponseEntity<>(
                        ApiResponse.<CargoTipoDto>builder()
                                .message("CargoTipo no encontrado.")
                                .datos(null)
                                .build(),
                        HttpStatus.NOT_FOUND);
            } else {
                responseEntity = new ResponseEntity<>(
                        ApiResponse.<CargoTipoDto>builder()
                                .message("CargoTipo encontrado.")
                                .datos(cargoTipoDtoFindById)
                                .nombreModelo("CargoTipo")
                                .build(),
                        HttpStatus.OK);
            }
        } catch (DataAccessException e) {
            responseEntity = new ResponseEntity<>(
                    ApiResponse.<CargoTipoDto>builder()
                            .message("Error al buscar CargoTipo.")
                            .datos(null)
                            .build(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return responseEntity;
    }

    @PutMapping("/estado/{id}")
    @ResponseStatus(HttpStatus.OK)
    public boolean estado(@PathVariable("id") Long id) {
        CargoTipoDto cargoTipoDto;
        try {
            cargoTipoDto = cargoTipoService.findById(id);
            if (cargoTipoDto == null) {
                throw new NotFoundExceptionResource("CargoTipo", "id", id);
            }
            cargoTipoService.estado(id);

        } catch (DataAccessException e) {
            throw new InternalServerErrorExceptionResource(e.getMessage());
        }
        return !cargoTipoDto.getEstado();
    }
}
