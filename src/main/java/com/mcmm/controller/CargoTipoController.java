package com.mcmm.controller;

import com.mcmm.model.dto.CargoTipoDto;
import com.mcmm.model.payload.MessageResponse;
import com.mcmm.service.ICargoTipo;
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
    public ResponseEntity<?> create(@RequestBody CargoTipoDto cargoTipoDto) {
        ResponseEntity<?> responseEntity;
        try {
            CargoTipoDto miembroSave = cargoTipoService.create(cargoTipoDto);
            responseEntity = new ResponseEntity<>(
                    MessageResponse.builder()
                            .message("Cargo guardada exitosamente.")
                            .datos(miembroSave)
                            .nombreModelo("Cargo")
                            .build(),
                    HttpStatus.CREATED
            );
        } catch (DataAccessException e) {
            responseEntity = new ResponseEntity<>(
                    MessageResponse.builder()
                            .message("Error al guardar la Cargo.")
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
        Iterable<CargoTipoDto> cargoDtos = cargoTipoService.findAll();
        try {
            responseEntity = new ResponseEntity<>(
                    MessageResponse.builder()
                            .message("Listado de Cargos")
                            .datos(cargoDtos)
                            .nombreModelo("Cargo")
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
    public ResponseEntity<?> update(@RequestBody CargoTipoDto cargoTipoDto) {
        ResponseEntity<?> responseEntity;
        try {
            CargoTipoDto cargoTipoUpdate = cargoTipoService.update(cargoTipoDto);
            responseEntity = new ResponseEntity<>(
                    MessageResponse.builder()
                            .message("CargoTipo actualizado exitosamente.")
                            .datos(cargoTipoUpdate)
                            .nombreModelo("CargoTipo")
                            .build(),
                    HttpStatus.OK
            );
        } catch (DataAccessException e) {
            responseEntity = new ResponseEntity<>(
                    MessageResponse.builder()
                            .message("Error al actualizar CargoTipo.")
                            .datos(null)
                            .build(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
        return responseEntity;
    }

}
