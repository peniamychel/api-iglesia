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
@PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO')")
public class CertificadoController {

    private final ICertificado certificadoService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApiResponse<CertificadoDto>> create(@Valid @RequestBody CertificadoDto certificadoDto) {
        CertificadoDto saved = certificadoService.create(certificadoDto);
        return new ResponseEntity<>(ApiResponse.<CertificadoDto>builder()
                .message("Certificado creado exitosamente.")
                .datos(saved)
                .nombreModelo("Certificado")
                .build(), HttpStatus.CREATED);
    }

    @GetMapping("/findall")
    public ResponseEntity<ApiResponse<List<CertificadoDto>>> findAll() {
        List<CertificadoDto> certificados = certificadoService.findAll();
        return ResponseEntity.ok(ApiResponse.<List<CertificadoDto>>builder()
                .message("Listado de certificados")
                .datos(certificados)
                .nombreModelo("Certificado")
                .build());
    }

    @GetMapping("/showbyid/{id}")
    public ResponseEntity<ApiResponse<CertificadoDto>> showById(@PathVariable Long id) {
        CertificadoDto certificado = certificadoService.findById(id);
        return ResponseEntity.ok(ApiResponse.<CertificadoDto>builder()
                .message("Certificado encontrado.")
                .datos(certificado)
                .nombreModelo("Certificado")
                .build());
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<CertificadoDto>> update(@Valid @RequestBody CertificadoDto certificadoDto) {
        CertificadoDto updated = certificadoService.update(certificadoDto);
        return ResponseEntity.ok(ApiResponse.<CertificadoDto>builder()
                .message("Certificado actualizado exitosamente.")
                .datos(updated)
                .nombreModelo("Certificado")
                .build());
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        certificadoService.delete(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Certificado eliminado exitosamente.")
                .datos(null)
                .nombreModelo("Certificado")
                .build());
    }

    @PutMapping("/estado/{id}")
    public ResponseEntity<ApiResponse<Void>> estado(@PathVariable Long id) {
        certificadoService.estado(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Estado del certificado actualizado exitosamente.")
                .datos(null)
                .nombreModelo("Certificado")
                .build());
    }

    @PostMapping(value = "/{id}/foto", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> uploadProfilePhoto(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file) {
        try {
            String fileUrl = certificadoService.uploadProfilePhoto(id, file);
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
    public ResponseEntity<ApiResponse<Void>> deleteProfilePhoto(@PathVariable Long id) {
        certificadoService.deleteProfilePhoto(id);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .message("Foto del certificado eliminada exitosamente.")
                        .datos(null)
                        .nombreModelo("Certificado")
                        .build());
    }
}
