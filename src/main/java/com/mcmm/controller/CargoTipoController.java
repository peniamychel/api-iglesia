package com.mcmm.controller;

import com.mcmm.model.dto.CargoTipoDto;
import com.mcmm.model.payload.ApiResponse;
import com.mcmm.service.ICargoTipo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tipocargo/v1")
@PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO')")
@RequiredArgsConstructor
public class CargoTipoController {

    private final ICargoTipo cargoTipoService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO') AND hasAuthority('Gestionar Tipos de Cargo')")
    public ResponseEntity<ApiResponse<CargoTipoDto>> create(@RequestBody @Valid CargoTipoDto cargoTipoDto) {
        CargoTipoDto cargoTipoSave = cargoTipoService.create(cargoTipoDto);
        return new ResponseEntity<>(
                ApiResponse.<CargoTipoDto>builder()
                        .message("CargoTipo guardado exitosamente.")
                        .datos(cargoTipoSave)
                        .nombreModelo("CargoTipo")
                        .build(),
                HttpStatus.CREATED);
    }

    @GetMapping("/findall")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApiResponse<List<CargoTipoDto>>> findAll() {
        List<CargoTipoDto> cargoTipoDtos = cargoTipoService.findAll();
        return ResponseEntity.ok(
                ApiResponse.<List<CargoTipoDto>>builder()
                        .message("Listado de tipos de cargo")
                        .datos(cargoTipoDtos)
                        .nombreModelo("CargoTipo")
                        .build());
    }

    @PutMapping("/update")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO') AND hasAuthority('Gestionar Tipos de Cargo')")
    public ResponseEntity<ApiResponse<CargoTipoDto>> update(@RequestBody @Valid CargoTipoDto cargoTipoDto) {
        CargoTipoDto cargoTipoUpdate = cargoTipoService.update(cargoTipoDto);
        return ResponseEntity.ok(
                ApiResponse.<CargoTipoDto>builder()
                        .message("CargoTipo actualizado exitosamente.")
                        .datos(cargoTipoUpdate)
                        .nombreModelo("CargoTipo")
                        .build());
    }

    @GetMapping("/showbyid/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApiResponse<CargoTipoDto>> showById(@PathVariable("id") Long id) {
        CargoTipoDto cargoTipoDtoFindById = cargoTipoService.findById(id);
        return ResponseEntity.ok(
                ApiResponse.<CargoTipoDto>builder()
                        .message("CargoTipo encontrado.")
                        .datos(cargoTipoDtoFindById)
                        .nombreModelo("CargoTipo")
                        .build());
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO') AND hasAuthority('Gestionar Tipos de Cargo')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        cargoTipoService.delete(id);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .message("CargoTipo eliminado exitosamente.")
                        .datos(null)
                        .nombreModelo("CargoTipo")
                        .build());
    }

    @PutMapping("/estado/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO') AND hasAuthority('Gestionar Tipos de Cargo')")
    public ResponseEntity<ApiResponse<CargoTipoDto>> estado(@PathVariable("id") Long id) {
        CargoTipoDto cargoTipoDto = cargoTipoService.estado(id);
        return ResponseEntity.ok(
                ApiResponse.<CargoTipoDto>builder()
                        .message("Se cambió el estado del tipo de cargo exitosamente a: " + cargoTipoDto.getEstado())
                        .datos(cargoTipoDto)
                        .nombreModelo("CargoTipo")
                        .build());
    }
}
