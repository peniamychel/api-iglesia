package com.mcmm.controller;

import com.mcmm.model.dto.certificado.CertificadoDto;
import com.mcmm.model.payload.ApiResponse;
import com.mcmm.service.ICertificado;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/certificado/v1")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO', 'PASTOR')")
public class CertificadoController {

    private final ICertificado certificadoService;
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
            bitacoraService.registrar(null, username, accion, "CERTIFICADO", descripcion, clientIp);
        } catch (Exception e) {
            // Ignorar
        }
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('Escribir Certificados')")
    public ResponseEntity<ApiResponse<CertificadoDto>> create(@Valid @RequestBody CertificadoDto certificadoDto) {
        CertificadoDto saved = certificadoService.create(certificadoDto);
        registrarLog("CREAR", "Creó el certificado ID: " + saved.getId() + " para el evento ID: " + saved.getEventoId());
        return new ResponseEntity<>(ApiResponse.<CertificadoDto>builder()
                .message("Certificado creado exitosamente.")
                .datos(saved)
                .nombreModelo("Certificado")
                .build(), HttpStatus.CREATED);
    }

    @GetMapping("/findall")
    @PreAuthorize("hasAuthority('Ver Certificados')")
    public ResponseEntity<ApiResponse<List<CertificadoDto>>> findAll() {
        List<CertificadoDto> certificados = certificadoService.findAll();
        return ResponseEntity.ok(ApiResponse.<List<CertificadoDto>>builder()
                .message("Listado de certificados")
                .datos(certificados)
                .nombreModelo("Certificado")
                .build());
    }

    @GetMapping("/showbyid/{id}")
    @PreAuthorize("hasAuthority('Ver Certificados')")
    public ResponseEntity<ApiResponse<CertificadoDto>> showById(@PathVariable Long id) {
        CertificadoDto certificado = certificadoService.findById(id);
        return ResponseEntity.ok(ApiResponse.<CertificadoDto>builder()
                .message("Certificado encontrado.")
                .datos(certificado)
                .nombreModelo("Certificado")
                .build());
    }

    @PutMapping("/update")
    @PreAuthorize("hasAuthority('Escribir Certificados')")
    public ResponseEntity<ApiResponse<CertificadoDto>> update(@Valid @RequestBody CertificadoDto certificadoDto) {
        CertificadoDto updated = certificadoService.update(certificadoDto);
        registrarLog("MODIFICAR", "Actualizó el certificado ID: " + updated.getId());
        return ResponseEntity.ok(ApiResponse.<CertificadoDto>builder()
                .message("Certificado actualizado exitosamente.")
                .datos(updated)
                .nombreModelo("Certificado")
                .build());
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('Escribir Certificados')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        certificadoService.delete(id);
        registrarLog("ELIMINAR", "Eliminó el certificado ID: " + id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Certificado eliminado exitosamente.")
                .datos(null)
                .nombreModelo("Certificado")
                .build());
    }

    @PutMapping("/estado/{id}")
    @PreAuthorize("hasAuthority('Escribir Certificados')")
    public ResponseEntity<ApiResponse<Void>> estado(@PathVariable Long id) {
        certificadoService.estado(id);
        registrarLog("MODIFICAR_ESTADO", "Modificó el estado del certificado ID: " + id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Estado del certificado actualizado exitosamente.")
                .datos(null)
                .nombreModelo("Certificado")
                .build());
    }

    @PostMapping(value = "/{id}/foto", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('Escribir Certificados')")
    public ResponseEntity<ApiResponse<String>> uploadProfilePhoto(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file) {
        try {
            String fileUrl = certificadoService.uploadProfilePhoto(id, file);
            registrarLog("SUBIR_FOTO", "Actualizó foto del certificado ID: " + id);
            return ResponseEntity.ok(
                    ApiResponse.<String>builder()
                            .message("Foto del certificado actualizada exitosamente.")
                            .datos(fileUrl)
                            .nombreModelo("Certificado")
                            .build());
        } catch (IOException e) {
            throw new RuntimeException("Error al subir la foto: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}/foto")
    @PreAuthorize("hasAuthority('Escribir Certificados')")
    public ResponseEntity<ApiResponse<Void>> deleteProfilePhoto(@PathVariable Long id) {
        certificadoService.deleteProfilePhoto(id);
        registrarLog("ELIMINAR_FOTO", "Eliminó la foto/carta del certificado ID: " + id);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .message("Foto del certificado eliminada exitosamente.")
                        .datos(null)
                        .nombreModelo("Certificado")
                        .build());
    }
}
