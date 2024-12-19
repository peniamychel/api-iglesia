package com.mcmm.controller;

import com.mcmm.model.dto.CargoDto;
import com.mcmm.model.payload.MessageResponse;
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
    public ResponseEntity<?> create(@RequestBody CargoDto cargoDto) {
        ResponseEntity<?> responseEntity;
        try {
            CargoDto miembroSave = cargoService.create(cargoDto);
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
        Iterable<CargoDto> cargoDtos = cargoService.findAll();
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
    public ResponseEntity<?> update(@RequestBody CargoDto cargoDto) {
        ResponseEntity<?> responseEntity;
        try {
            CargoDto cargoUpdate = cargoService.update(cargoDto);
            responseEntity = new ResponseEntity<>(
                    MessageResponse.builder()
                            .message("Cargo actualizado exitosamente.")
                            .datos(cargoUpdate)
                            .nombreModelo("Cargo")
                            .build(),
                    HttpStatus.OK
            );
        } catch (DataAccessException e) {
            responseEntity = new ResponseEntity<>(
                    MessageResponse.builder()
                            .message("Error al actualizar Cargo.")
                            .datos(null)
                            .build(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
        return responseEntity;
    }
}
