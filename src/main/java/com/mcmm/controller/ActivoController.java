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
@PreAuthorize("hasAnyRole('ADMIN', 'PASTOR', 'ENCARGADO_IGLESIA')")
@RequiredArgsConstructor
public class ActivoController {

    private final IActivo activoService;
    private final com.mcmm.service.IBitacora bitacoraService;

    @org.springframework.beans.factory.annotation.Autowired
    private jakarta.servlet.http.HttpServletRequest request;

    private void registrarLog(String accion, String descripcion) {
        try {
            org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            String username = authentication != null ? authentication.getName() : "Sistema";
            String clientIp = request.getHeader("X-Forwarded-For");
            if (clientIp == null || clientIp.isEmpty()) {
                clientIp = request.getRemoteAddr();
            } else {
                clientIp = clientIp.split(",")[0].trim();
            }
            bitacoraService.registrar(null, username, accion, "ACTIVO", descripcion, clientIp);
        } catch (Exception e) {
            // Ignorar
        }
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApiResponse<ActivoDto>> create(@RequestBody @Valid ActivoDto dto) {
        ActivoDto saved = activoService.save(dto);
        registrarLog("CREAR", "Registró un nuevo activo: " + saved.getNombre() + " (ID: " + saved.getId() + ")");
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
        registrarLog("MODIFICAR", "Actualizó el activo ID: " + updated.getId() + " - " + updated.getNombre());
        return ResponseEntity.ok(ApiResponse.<ActivoDto>builder()
                .message("Activo actualizado con éxito.")
                .datos(updated)
                .nombreModelo("Activo")
                .build());
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        activoService.delete(id);
        registrarLog("ELIMINAR", "Eliminó el activo ID: " + id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Activo eliminado con éxito.")
                .datos(null)
                .nombreModelo("Activo")
                .build());
    }
}
