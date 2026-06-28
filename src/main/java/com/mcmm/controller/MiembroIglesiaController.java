package com.mcmm.controller;

import com.mcmm.exception.BadRequestException;
import com.mcmm.model.dto.GraficoDataDto;
import com.mcmm.model.dto.MiembroDto.MiembroDto;
import com.mcmm.model.dto.MiembroIglesiaDto;
import com.mcmm.model.payload.ApiResponse;
import com.mcmm.service.IMiembroIglesia;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/miembroiglesia/v1")
@PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO', 'PASTOR')")
@RequiredArgsConstructor
public class MiembroIglesiaController {

    private final IMiembroIglesia miembroIglesiaService;
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
            bitacoraService.registrar(null, username, accion, "TRASPASO", descripcion, clientIp);
        } catch (Exception e) {
            // Ignorar
        }
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO', 'PASTOR') AND hasAuthority('Escribir MiembroIglesia')")
    public ResponseEntity<ApiResponse<MiembroIglesiaDto>> create(@RequestBody @Valid MiembroIglesiaDto miembroDto) {
        MiembroIglesiaDto miembroIglesiaSave = miembroIglesiaService.save(miembroDto);
        return new ResponseEntity<>(
                ApiResponse.<MiembroIglesiaDto>builder()
                        .message("MiembroIglesia guardado exitosamente.")
                        .datos(miembroIglesiaSave)
                        .nombreModelo("MiembroIglesia")
                        .build(),
                HttpStatus.CREATED);
    }

    @PostMapping("/created")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO', 'PASTOR') AND hasAuthority('Escribir MiembroIglesia')")
    public ResponseEntity<ApiResponse<MiembroIglesiaDto>> created(@RequestBody @Valid MiembroIglesiaDto miembroIglesiaDto) {
        boolean result = miembroIglesiaService.findByIdMiembro(miembroIglesiaDto.getMiembroId());
        if (!result) {
            throw new BadRequestException("El miembro ya pertenece a una iglesia.");
        }
        MiembroIglesiaDto saved = miembroIglesiaService.save(miembroIglesiaDto);
        return new ResponseEntity<>(
                ApiResponse.<MiembroIglesiaDto>builder()
                        .message("MiembroIglesia guardado exitosamente.")
                        .datos(saved)
                        .nombreModelo("MiembroIglesia")
                        .build(),
                HttpStatus.CREATED);
    }

    @GetMapping("/findall")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO', 'PASTOR') AND hasAuthority('Ver MiembroIglesia')")
    public ResponseEntity<ApiResponse<List<MiembroIglesiaDto>>> findAll() {
        List<MiembroIglesiaDto> miembroIglesiaDtos = miembroIglesiaService.findAll();
        return ResponseEntity.ok(
                ApiResponse.<List<MiembroIglesiaDto>>builder()
                        .message("Listado de MiembrosIglesia")
                        .datos(miembroIglesiaDtos)
                        .nombreModelo("MiembroIglesia")
                        .build());
    }

    @GetMapping("/showbyid/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO', 'PASTOR') AND hasAuthority('Ver MiembroIglesia')")
    public ResponseEntity<ApiResponse<MiembroIglesiaDto>> findById(@PathVariable("id") Long id) {
        MiembroIglesiaDto miembroIglesiaDto = miembroIglesiaService.findById(id);
        return ResponseEntity.ok(
                ApiResponse.<MiembroIglesiaDto>builder()
                        .message("MiembroIglesia encontrado.")
                        .datos(miembroIglesiaDto)
                        .nombreModelo("MiembroIglesia")
                        .build());
    }

    @PutMapping("/update")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO', 'PASTOR') AND hasAuthority('Escribir MiembroIglesia')")
    public ResponseEntity<ApiResponse<MiembroIglesiaDto>> update(@RequestBody @Valid MiembroIglesiaDto miembroIglesiaDto) {
        MiembroIglesiaDto miembroIglesiaActualizado = miembroIglesiaService.update(miembroIglesiaDto);
        return ResponseEntity.ok(
                ApiResponse.<MiembroIglesiaDto>builder()
                        .message("MiembroIglesia actualizado exitosamente.")
                        .datos(miembroIglesiaActualizado)
                        .nombreModelo("MiembroIglesia")
                        .build());
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO', 'PASTOR') AND hasAuthority('Escribir MiembroIglesia')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        miembroIglesiaService.delete(id);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .message("MiembroIglesia eliminado exitosamente.")
                        .datos(null)
                        .nombreModelo("MiembroIglesia")
                        .build());
    }

    @PutMapping("/estado/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO', 'PASTOR') AND hasAuthority('Escribir MiembroIglesia')")
    public ResponseEntity<ApiResponse<MiembroIglesiaDto>> estado(@PathVariable Long id) {
        MiembroIglesiaDto miembroIglesiaDto = miembroIglesiaService.estado(id);
        return ResponseEntity.ok(
                ApiResponse.<MiembroIglesiaDto>builder()
                        .message("Se cambió el estado del MiembroIglesia exitosamente a: " + miembroIglesiaDto.getEstado())
                        .datos(miembroIglesiaDto)
                        .nombreModelo("MiembroIglesia")
                        .build());
    }

    @PutMapping("/traspaso")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO', 'PASTOR') AND hasAuthority('Escribir MiembroIglesia')")
    public ResponseEntity<ApiResponse<MiembroIglesiaDto>> solicitarTraspaso(@RequestBody @Valid MiembroIglesiaDto miembroIglesiaDto) {
        MiembroIglesiaDto miembroIglesiaActualizado = miembroIglesiaService.solicitarTraspaso(miembroIglesiaDto);
        registrarLog("SOLICITAR_TRASPASO", "Solicitó traspaso para el miembro ID: " + miembroIglesiaActualizado.getMiembroId() + " a la iglesia ID: " + miembroIglesiaActualizado.getIglesiaId());
        return ResponseEntity.ok(
                ApiResponse.<MiembroIglesiaDto>builder()
                        .message("Solicitud de traspaso registrada exitosamente.")
                        .datos(miembroIglesiaActualizado)
                        .nombreModelo("MiembroIglesia")
                        .build());
    }

    @PutMapping("/traspaso/{id}/aceptar")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO', 'PASTOR') AND hasAuthority('Escribir MiembroIglesia')")
    public ResponseEntity<ApiResponse<MiembroIglesiaDto>> aceptarTraspaso(@PathVariable Long id) {
        MiembroIglesiaDto miembroIglesiaActualizado = miembroIglesiaService.aceptarTraspaso(id);
        registrarLog("ACEPTAR_TRASPASO", "Aceptó solicitud de traspaso ID: " + id);
        return ResponseEntity.ok(
                ApiResponse.<MiembroIglesiaDto>builder()
                        .message("Traspaso aceptado exitosamente.")
                        .datos(miembroIglesiaActualizado)
                        .nombreModelo("MiembroIglesia")
                        .build());
    }

    @PutMapping("/traspaso/{id}/rechazar")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO', 'PASTOR') AND hasAuthority('Escribir MiembroIglesia')")
    public ResponseEntity<ApiResponse<MiembroIglesiaDto>> rechazarTraspaso(@PathVariable Long id) {
        MiembroIglesiaDto miembroIglesiaActualizado = miembroIglesiaService.rechazarTraspaso(id);
        registrarLog("RECHAZAR_TRASPASO", "Rechazó solicitud de traspaso ID: " + id);
        return ResponseEntity.ok(
                ApiResponse.<MiembroIglesiaDto>builder()
                        .message("Traspaso rechazado exitosamente.")
                        .datos(miembroIglesiaActualizado)
                        .nombreModelo("MiembroIglesia")
                        .build());
    }

    @GetMapping("/traspaso/pendientes/{iglesiaId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO', 'PASTOR') AND hasAuthority('Ver MiembroIglesia')")
    public ResponseEntity<ApiResponse<List<MiembroIglesiaDto>>> getSolicitudesPendientes(@PathVariable Long iglesiaId) {
        List<MiembroIglesiaDto> solicitudes = miembroIglesiaService.getSolicitudesPendientes(iglesiaId);
        return ResponseEntity.ok(
                ApiResponse.<List<MiembroIglesiaDto>>builder()
                        .message("Solicitudes de traspaso pendientes encontradas.")
                        .datos(solicitudes)
                        .nombreModelo("MiembroIglesia")
                        .build());
    }

    @GetMapping("/historial/{miembroId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO', 'PASTOR') AND hasAuthority('Ver MiembroIglesia')")
    public ResponseEntity<ApiResponse<List<MiembroIglesiaDto>>> obtenerHistorialMiembro(@PathVariable Long miembroId) {
        List<MiembroIglesiaDto> historial = miembroIglesiaService.obtenerHistorialMiembro(miembroId);
        return ResponseEntity.ok(
                ApiResponse.<List<MiembroIglesiaDto>>builder()
                        .message("Historial del miembro encontrado.")
                        .datos(historial)
                        .nombreModelo("MiembroIglesia")
                        .build());
    }

    @GetMapping("/listmiembrosiglesia/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO', 'PASTOR') AND hasAuthority('Ver MiembroIglesia')")
    public ResponseEntity<ApiResponse<List<MiembroDto>>> findMiembrosIglesia(@PathVariable("id") Long id) {
        List<MiembroDto> miembroDtos = miembroIglesiaService.findMiembrosIglesia(id);
        return ResponseEntity.ok(
                ApiResponse.<List<MiembroDto>>builder()
                        .message("Miembros de la iglesia encontrados.")
                        .datos(miembroDtos)
                        .nombreModelo("Miembro")
                        .build());
    }

    @GetMapping("/mis-miembros")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('PASTOR', 'ENCARGADO_IGLESIA', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<MiembroDto>>> findMisMiembros() {
        org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof UsernamePasswordAuthenticationToken)) {
            throw new BadRequestException("Usuario no autenticado correctamente.");
        }
        UsernamePasswordAuthenticationToken authToken = (UsernamePasswordAuthenticationToken) authentication;
        java.util.Map<String, Object> details = (java.util.Map<String, Object>) authToken.getDetails();
        if (details == null) {
            throw new BadRequestException("No se encontraron detalles de autenticación.");
        }
        Long iglesiaId = null;
        Object iglesiaIdObj = details.get("iglesiaId");
        if (iglesiaIdObj instanceof Long) {
            iglesiaId = (Long) iglesiaIdObj;
        } else if (iglesiaIdObj instanceof Integer) {
            iglesiaId = ((Integer) iglesiaIdObj).longValue();
        }
        if (iglesiaId == null) {
            throw new BadRequestException("El usuario no tiene una iglesia asociada en su sesión.");
        }
        List<MiembroDto> miembroDtos = miembroIglesiaService.findMiembrosIglesia(iglesiaId);
        return ResponseEntity.ok(
                ApiResponse.<List<MiembroDto>>builder()
                        .message("Miembros de la iglesia del usuario autenticado encontrados.")
                        .datos(miembroDtos)
                        .nombreModelo("Miembro")
                        .build());
    }

    @GetMapping("/graficomiembrosiglesia/{cant}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO', 'PASTOR') AND hasAuthority('Ver MiembroIglesia')")
    public ResponseEntity<ApiResponse<List<GraficoDataDto>>> graficoMiembrosIglesia(@PathVariable("cant") Long cant) {
        List<GraficoDataDto> miembroIglesia = miembroIglesiaService.graficoMiembrosIglesia(cant);
        return ResponseEntity.ok(
                ApiResponse.<List<GraficoDataDto>>builder()
                        .message("Gráfico para " + cant + " Iglesias.")
                        .datos(miembroIglesia)
                        .nombreModelo("MiembroIglesia")
                        .build());
    }

    @PostMapping("/{id}/carta-traspaso")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO', 'PASTOR') AND hasAuthority('Escribir MiembroIglesia')")
    public ResponseEntity<ApiResponse<String>> uploadCartaTraspaso(
            @PathVariable Long id,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            String fileUrl = miembroIglesiaService.subirCartaTraspaso(id, file);
            registrarLog("SUBIR_FOTO", "Subió carta de traspaso firmada para solicitud ID: " + id);
            return ResponseEntity.ok(
                    ApiResponse.<String>builder()
                            .message("Carta de traspaso subida exitosamente.")
                            .datos(fileUrl)
                            .nombreModelo("MiembroIglesia")
                            .build());
        } catch (java.io.IOException e) {
            throw new RuntimeException("Error al subir el archivo: " + e.getMessage());
        }
    }
}
