package com.mcmm.controller;

import com.mcmm.model.dto.CargoDto;
import com.mcmm.model.payload.ApiResponse;
import com.mcmm.service.ICargo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;

/**
 * Controlador REST para la gestión de Cargos de miembros en la Iglesia.
 * Proporciona endpoints para creación, obtención, actualización, desactivación y eliminación de Cargos.
 * 
 * @author Antigravity
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/cargo/v1")
@PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO', 'PASTOR')")
@RequiredArgsConstructor
public class CargoController {

    private final ICargo cargoService;

    /**
     * Crea un nuevo Cargo para un miembro en la base de datos.
     * 
     * @param cargoDto DTO con los datos del Cargo a registrar.
     * @return ResponseEntity conteniendo la respuesta de API con el DTO del Cargo creado.
     */
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO', 'PASTOR') AND hasAuthority('Escribir Cargos')")
    public ResponseEntity<ApiResponse<CargoDto>> create(@RequestBody @Valid CargoDto cargoDto) {
        CargoDto cargoSave = cargoService.create(cargoDto);
        return new ResponseEntity<>(
                ApiResponse.<CargoDto>builder()
                        .message("Cargo guardado exitosamente.")
                        .datos(cargoSave)
                        .nombreModelo("Cargo")
                        .build(),
                HttpStatus.CREATED);
    }

    /**
     * Obtiene todos los Cargos registrados.
     * 
     * @return ResponseEntity conteniendo la respuesta de API con el listado de Cargos.
     */
    @GetMapping("/findall")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO', 'PASTOR') AND hasAuthority('Ver Cargos')")
    public ResponseEntity<ApiResponse<Iterable<CargoDto>>> findAll() {
        Iterable<CargoDto> cargoDtos = cargoService.findAll();
        return ResponseEntity.ok(
                ApiResponse.<Iterable<CargoDto>>builder()
                        .message("Listado de Cargos")
                        .datos(cargoDtos)
                        .nombreModelo("Cargo")
                        .build());
    }

    /**
     * Obtiene los colaboradores de la iglesia activa del usuario autenticado,
     * con datos completos del miembro y rol embebidos.
     * Accesible para PASTOR, ADMIN, ENCARGADO_IGLESIA.
     *
     * @return Lista de cargos con información de miembro y rolCargo incluida.
     */
    @GetMapping("/mis-colaboradores")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO', 'PASTOR')")
    public ResponseEntity<ApiResponse<java.util.List<CargoDto>>> findMisColaboradores() {
        java.util.List<CargoDto> cargoDtos = cargoService.findMisColaboradores();
        return ResponseEntity.ok(
                ApiResponse.<java.util.List<CargoDto>>builder()
                        .message("Colaboradores de la iglesia")
                        .datos(cargoDtos)
                        .nombreModelo("Cargo")
                        .build());
    }

    /**
     * Actualiza la información de un Cargo existente.
     * 
     * @param cargoDto DTO con los datos actualizados del Cargo.
     * @return ResponseEntity conteniendo el Cargo actualizado.
     */
    @PutMapping("/update")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO', 'PASTOR') AND hasAuthority('Escribir Cargos')")
    public ResponseEntity<ApiResponse<CargoDto>> update(@RequestBody @Valid CargoDto cargoDto) {
        CargoDto cargoUpdate = cargoService.update(cargoDto);
        return ResponseEntity.ok(
                ApiResponse.<CargoDto>builder()
                        .message("Cargo actualizado exitosamente.")
                        .datos(cargoUpdate)
                        .nombreModelo("Cargo")
                        .build());
    }

    /**
     * Busca un Cargo por su identificador único ID.
     * 
     * @param id Identificador único del Cargo a buscar.
     * @return ResponseEntity conteniendo el Cargo encontrado.
     */
    @GetMapping("/showbyid/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO', 'PASTOR') AND hasAuthority('Ver Cargos')")
    public ResponseEntity<ApiResponse<CargoDto>> showById(@PathVariable("id") Long id) {
        CargoDto cargoFiedById = cargoService.findById(id);
        return ResponseEntity.ok(
                ApiResponse.<CargoDto>builder()
                        .message("Cargo encontrado.")
                        .datos(cargoFiedById)
                        .nombreModelo("Cargo")
                        .build());
    }

    /**
     * Elimina un Cargo por su ID de la base de datos.
     * 
     * @param id Identificador del Cargo a eliminar.
     * @return ResponseEntity confirmando la eliminación del Cargo.
     */
    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO', 'PASTOR') AND hasAuthority('Escribir Cargos')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        cargoService.delete(id);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .message("Cargo eliminado exitosamente.")
                        .datos(null)
                        .nombreModelo("Cargo")
                        .build());
    }

    /**
     * Alterna el estado (activo/inactivo) de un Cargo específico por su ID.
     * Opcionalmente acepta una fecha de fin para registrar el historial.
     * 
     * @param id Identificador del Cargo.
     * @param fechaFin Fecha de finalización opcional (formato yyyy-MM-dd).
     * @return true si el nuevo estado es activo, false en caso contrario.
     */
    @PutMapping("/estado/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO', 'PASTOR') AND hasAuthority('Escribir Cargos')")
    public boolean estado(
            @PathVariable("id") Long id,
            @RequestParam(value = "fechaFin", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaFin) {
        return cargoService.estadoConFecha(id, fechaFin);
    }

    /**
     * Sube el acta de asignación para un cargo específico.
     * 
     * @param id Identificador del Cargo.
     * @param file Archivo a subir (imagen o PDF).
     * @return ResponseEntity con la URL del archivo subido.
     */
    @PostMapping("/{id}/acta-asignacion")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO', 'PASTOR') AND hasAuthority('Escribir Cargos')")
    public ResponseEntity<ApiResponse<String>> uploadActaAsignacion(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        try {
            String fileUrl = cargoService.saveActaAsignacion(id, file);
            return ResponseEntity.ok(
                    ApiResponse.<String>builder()
                            .message("Acta de asignación cargada exitosamente.")
                            .datos(fileUrl)
                            .nombreModelo("Cargo")
                            .build());
        } catch (IOException e) {
            throw new RuntimeException("Error al subir el acta de asignación: " + e.getMessage());
        }
    }

    /**
     * Sube el acta de deslindación para un cargo específico.
     * 
     * @param id Identificador del Cargo.
     * @param file Archivo a subir (imagen o PDF).
     * @return ResponseEntity con la URL del archivo subido.
     */
    @PostMapping("/{id}/acta-deslindacion")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO', 'PASTOR') AND hasAuthority('Escribir Cargos')")
    public ResponseEntity<ApiResponse<String>> uploadActaDeslindacion(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        try {
            String fileUrl = cargoService.saveActaDeslindacion(id, file);
            return ResponseEntity.ok(
                    ApiResponse.<String>builder()
                            .message("Acta de deslindación cargada exitosamente.")
                            .datos(fileUrl)
                            .nombreModelo("Cargo")
                            .build());
        } catch (IOException e) {
            throw new RuntimeException("Error al subir el acta de deslindación: " + e.getMessage());
        }
    }
}
