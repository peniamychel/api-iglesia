package com.mcmm.controller;

import com.mcmm.model.dto.MiembroDto.MiembroDto;
import com.mcmm.model.payload.ApiResponse;
import com.mcmm.service.IMiembro;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la gestión de Miembros de la iglesia.
 * Proporciona endpoints para creación, consulta, actualización, desactivación y eliminación de miembros.
 * 
 * @author Antigravity
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/miembro/v1")
@PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO')")
@RequiredArgsConstructor
public class MiembroController {

    private final IMiembro miembroService;

    /**
     * Crea un nuevo miembro en el sistema.
     * 
     * @param miembroDto DTO con la información básica y la persona asociada a registrar.
     * @return ResponseEntity conteniendo el miembro registrado.
     */
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO') AND hasAuthority('Gestionar Miembros')")
    public ResponseEntity<ApiResponse<MiembroDto>> create(@RequestBody @Valid MiembroDto miembroDto) {
        MiembroDto miembroSave = miembroService.create(miembroDto);
        return new ResponseEntity<>(
                ApiResponse.<MiembroDto>builder()
                        .message("Miembro guardado exitosamente.")
                        .datos(miembroSave)
                        .nombreModelo("Miembro")
                        .build(),
                HttpStatus.CREATED
        );
    }

    /**
     * Obtiene el listado completo de todos los miembros de la iglesia.
     * 
     * @return ResponseEntity con la lista de miembros.
     */
    @GetMapping("/findall")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApiResponse<Iterable<MiembroDto>>> findAll() {
        Iterable<MiembroDto> miembroDtos = miembroService.findAll();
        return ResponseEntity.ok(
                ApiResponse.<Iterable<MiembroDto>>builder()
                        .message("Listado de Miembros")
                        .datos(miembroDtos)
                        .nombreModelo("Miembro")
                        .build()
        );
    }

    /**
     * Busca un miembro por su identificador único ID.
     * 
     * @param id Identificador único del miembro.
     * @return ResponseEntity con el miembro correspondiente.
     */
    @GetMapping("/showbyid/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApiResponse<MiembroDto>> findById(@PathVariable("id") Long id) {
        MiembroDto miembroDto = miembroService.findById(id);
        return ResponseEntity.ok(
                ApiResponse.<MiembroDto>builder()
                        .message("Miembro encontrado.")
                        .datos(miembroDto)
                        .nombreModelo("Miembro")
                        .build()
        );
    }

    /**
     * Actualiza la información de un miembro existente.
     * 
     * @param miembroDto DTO con los datos actualizados del miembro.
     * @return ResponseEntity con el miembro actualizado.
     */
    @PutMapping("/update")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO') AND hasAuthority('Gestionar Miembros')")
    public ResponseEntity<ApiResponse<MiembroDto>> update(@RequestBody @Valid MiembroDto miembroDto) {
        MiembroDto miembroActualizado = miembroService.update(miembroDto);
        return ResponseEntity.ok(
                ApiResponse.<MiembroDto>builder()
                        .message("Miembro actualizado exitosamente.")
                        .datos(miembroActualizado)
                        .nombreModelo("Miembro")
                        .build()
        );
    }

    /**
     * Elimina a un miembro del sistema por su identificador único ID.
     * 
     * @param id Identificador único del miembro a eliminar.
     * @return ResponseEntity confirmando la eliminación.
     */
    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO') AND hasAuthority('Gestionar Miembros')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        miembroService.delete(id);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .message("Miembro eliminado exitosamente.")
                        .datos(null)
                        .nombreModelo("Miembro")
                        .build()
        );
    }

    /**
     * Alterna el estado (activo/inactivo) de un miembro por su identificador único ID.
     * 
     * @param id Identificador único del miembro.
     * @return ResponseEntity conteniendo la información actualizada con el nuevo estado del miembro.
     */
    @PutMapping("/estado/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO') AND hasAuthority('Gestionar Miembros')")
    public ResponseEntity<ApiResponse<MiembroDto>> estado(@PathVariable Long id) {
        MiembroDto miembroActualizado = miembroService.estado(id);
        return ResponseEntity.ok(
                ApiResponse.<MiembroDto>builder()
                        .message("Se cambió el estado del miembro exitosamente a: " + miembroActualizado.getEstado())
                        .datos(miembroActualizado)
                        .nombreModelo("Miembro")
                        .build()
        );
    }
}
