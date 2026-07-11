package com.mcmm.controller;

import com.mcmm.model.dto.ActivoDto;
import com.mcmm.model.payload.ApiResponse;
import com.mcmm.service.IActivo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activo/v1")
@PreAuthorize("hasAnyRole('ADMIN', 'PASTOR', 'ENCARGADO_IGLESIA', 'DIACONO')")
@RequiredArgsConstructor
public class ActivoController {

    private final IActivo activoService;
    private final com.mcmm.service.IBitacora bitacoraService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApiResponse<ActivoDto>> create(@RequestBody @Valid ActivoDto dto) {
        ActivoDto saved = activoService.save(dto);
        bitacoraService.registrarAccion("ACTIVO", "CREAR", "Registró un nuevo activo: " + saved.getNombre() + " (ID: " + saved.getId() + ")");
        return new ResponseEntity<>(ApiResponse.<ActivoDto>builder()
                .message("Activo de iglesia registrado con éxito.")
                .datos(saved)
                .nombreModelo("Activo")
                .build(), HttpStatus.CREATED);
    }

    @GetMapping("/findall")
    public ResponseEntity<ApiResponse<List<ActivoDto>>> findAll() {
        List<ActivoDto> list = activoService.findAll();
        return ResponseEntity.ok(ApiResponse.<List<ActivoDto>>builder()
                .message("Listado de todos los activos.")
                .datos(list)
                .nombreModelo("Activo")
                .build());
    }

    @GetMapping("/showbyid/{id}")
    public ResponseEntity<ApiResponse<ActivoDto>> findById(@PathVariable Long id) {
        ActivoDto dto = activoService.findById(id);
        return ResponseEntity.ok(ApiResponse.<ActivoDto>builder()
                .message("Activo encontrado.")
                .datos(dto)
                .nombreModelo("Activo")
                .build());
    }

    @GetMapping("/iglesia/{iglesiaId}")
    public ResponseEntity<ApiResponse<List<ActivoDto>>> findByIglesia(@PathVariable Long iglesiaId) {
        List<ActivoDto> list = activoService.findByIglesia(iglesiaId);
        return ResponseEntity.ok(ApiResponse.<List<ActivoDto>>builder()
                .message("Listado de activos por iglesia.")
                .datos(list)
                .nombreModelo("Activo")
                .build());
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<ActivoDto>> update(@RequestBody @Valid ActivoDto dto) {
        ActivoDto updated = activoService.update(dto);
        bitacoraService.registrarAccion("ACTIVO", "MODIFICAR", "Actualizó el activo ID: " + updated.getId() + " - " + updated.getNombre());
        return ResponseEntity.ok(ApiResponse.<ActivoDto>builder()
                .message("Activo actualizado con éxito.")
                .datos(updated)
                .nombreModelo("Activo")
                .build());
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        activoService.delete(id);
        bitacoraService.registrarAccion("ACTIVO", "ELIMINAR", "Eliminó el activo ID: " + id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Activo eliminado con éxito.")
                .datos(null)
                .nombreModelo("Activo")
                .build());
    }

    @PostMapping("/{id}/foto")
    public ResponseEntity<ApiResponse<String>> uploadPhoto(
            @PathVariable Long id,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            String fileUrl = activoService.uploadPhoto(id, file);
            bitacoraService.registrarAccion("ACTIVO", "SUBIR_FOTO", "Actualizó foto del activo ID: " + id);
            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .message("Foto del activo actualizada con éxito.")
                    .datos(fileUrl)
                    .nombreModelo("Activo")
                    .build());
        } catch (java.io.IOException e) {
            throw new RuntimeException("Error al subir la foto: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}/foto")
    public ResponseEntity<ApiResponse<Void>> deletePhoto(@PathVariable Long id) {
        activoService.deletePhoto(id);
        bitacoraService.registrarAccion("ACTIVO", "ELIMINAR_FOTO", "Eliminó foto del activo ID: " + id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Foto del activo eliminada con éxito.")
                .datos(null)
                .nombreModelo("Activo")
                .build());
    }
}
