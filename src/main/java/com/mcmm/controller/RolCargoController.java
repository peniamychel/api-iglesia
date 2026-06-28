package com.mcmm.controller;

import com.mcmm.model.dto.RolCargoDto;
import com.mcmm.model.payload.ApiResponse;
import com.mcmm.service.IRolCargo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rol-cargo/v1")
@PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO', 'PASTOR')")
@RequiredArgsConstructor
public class RolCargoController {

    private final IRolCargo rolCargoService;

    @GetMapping("/findall")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('Ver Privilegios')")
    public ResponseEntity<ApiResponse<List<RolCargoDto>>> findAll() {
        List<RolCargoDto> list = rolCargoService.findAll();
        return ResponseEntity.ok(
                ApiResponse.<List<RolCargoDto>>builder()
                        .message("Listado de Roles y Cargos")
                        .datos(list)
                        .nombreModelo("RolCargo")
                        .build()
        );
    }

    @GetMapping("/findall-cargo")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApiResponse<List<RolCargoDto>>> findAllCargo() {
        List<RolCargoDto> list = rolCargoService.findAllCargo();
        return ResponseEntity.ok(
                ApiResponse.<List<RolCargoDto>>builder()
                        .message("Listado de Cargos para Colaboradores")
                        .datos(list)
                        .nombreModelo("RolCargo")
                        .build()
        );
    }

    @GetMapping("/showbyid/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('Ver Privilegios')")
    public ResponseEntity<ApiResponse<RolCargoDto>> findById(@PathVariable Long id) {
        RolCargoDto dto = rolCargoService.findById(id);
        return ResponseEntity.ok(
                ApiResponse.<RolCargoDto>builder()
                        .message("Rol/Cargo encontrado")
                        .datos(dto)
                        .nombreModelo("RolCargo")
                        .build()
        );
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RolCargoDto>> create(@RequestBody @Valid RolCargoDto rolCargoDto) {
        RolCargoDto created = rolCargoService.create(rolCargoDto);
        return new ResponseEntity<>(
                ApiResponse.<RolCargoDto>builder()
                        .message("Rol/Cargo creado exitosamente.")
                        .datos(created)
                        .nombreModelo("RolCargo")
                        .build(),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/update/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RolCargoDto>> update(@PathVariable Long id, @RequestBody @Valid RolCargoDto rolCargoDto) {
        rolCargoDto.setId(id);
        RolCargoDto updated = rolCargoService.update(rolCargoDto);
        return ResponseEntity.ok(
                ApiResponse.<RolCargoDto>builder()
                        .message("Rol/Cargo actualizado exitosamente.")
                        .datos(updated)
                        .nombreModelo("RolCargo")
                        .build()
        );
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        rolCargoService.delete(id);
        return new ResponseEntity<>(
                ApiResponse.<Void>builder()
                        .message("Rol/Cargo eliminado exitosamente.")
                        .nombreModelo("RolCargo")
                        .build(),
                HttpStatus.NO_CONTENT
        );
    }

    @PutMapping("/estado/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> estado(@PathVariable Long id) {
        rolCargoService.estado(id);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .message("Estado de Rol/Cargo modificado exitosamente.")
                        .nombreModelo("RolCargo")
                        .build()
        );
    }
}
