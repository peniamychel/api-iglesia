package com.mcmm.controller;

import com.mcmm.exception.BadRequestException;
import com.mcmm.exception.DataAccessException;
import com.mcmm.exception.NotFoundExceptionResource;
import com.mcmm.model.dto.CargoTipoDto;
import com.mcmm.model.payload.ApiResponse;
import com.mcmm.service.ICargoTipo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<?> create(@RequestBody @Valid CargoTipoDto cargoTipoDto) {
        ResponseEntity<?> responseEntity;
        try {
            CargoTipoDto miembroSave = cargoTipoService.create(cargoTipoDto);
            responseEntity = new ResponseEntity<>(
                    ApiResponse.builder()
                            .message("Cargo guardada exitosamente.")
                            .datos(miembroSave)
                            .nombreModelo("Cargo")
                            .build(),
                    HttpStatus.CREATED
            );
        } catch (DataAccessException e) {
            throw new BadRequestException(e.getMessage());
        }
        return responseEntity;
    }

    @GetMapping("/findall")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> findAll() {
        ResponseEntity<?> responseEntity;
        Iterable<CargoTipoDto> cargoDtos = cargoTipoService.findAll();
        try {
            responseEntity = new ResponseEntity<>(
                    ApiResponse.builder()
                            .message("Listado de Cargos")
                            .datos(cargoDtos)
                            .nombreModelo("Cargo")
                            .build()
                    , HttpStatus.OK
            );
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }
        return responseEntity;
    }

    @PutMapping("/update")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> update(@RequestBody CargoTipoDto cargoTipoDto) {
        ResponseEntity<?> responseEntity;
        if(cargoTipoService.findById(cargoTipoDto.getId()) == null){
            throw new NotFoundExceptionResource("CargoTipo no encontrado");
        }
        try {
        CargoTipoDto cargoTipoUpdate = cargoTipoService.update(cargoTipoDto);
            responseEntity = new ResponseEntity<>(
                    ApiResponse.builder()
                            .message("CargoTipo actualizado exitosamente.")
                            .datos(cargoTipoUpdate)
                            .nombreModelo("CargoTipo")
                            .build(),
                    HttpStatus.OK
            );
        } catch (DataAccessException e) {
            throw new BadRequestException(e.getMessage());
        }
        return responseEntity;
    }

    @GetMapping("/showbyid/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> showById(@PathVariable("id") Long id) {
        ResponseEntity<?> responseEntity;
        try {
            CargoTipoDto cargoTipoDtoFiedById = cargoTipoService.findById(id);
            if (cargoTipoDtoFiedById == null) {
                throw new NotFoundExceptionResource("CargoTipo","id",id);
            } else {
                responseEntity = new ResponseEntity<>(
                        ApiResponse.builder()
                                .message("CargoTipo actualizado.")
                                .datos(cargoTipoDtoFiedById)
                                .nombreModelo("CargoTipo")
                                .build(),
                        HttpStatus.OK
                );
            }
        } catch (DataAccessException e) {
            throw new BadRequestException(e.getMessage());
        }
        return responseEntity;
    }

}
