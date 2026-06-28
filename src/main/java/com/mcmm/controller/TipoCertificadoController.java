package com.mcmm.controller;

import com.mcmm.exception.NotFoundExceptionResource;
import com.mcmm.model.dto.tipoCertificado.TipoCertificadoDto;
import com.mcmm.model.payload.ApiResponse;
import com.mcmm.service.ITipoCertificado;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tipo-certificado/v1")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO', 'PASTOR')")
public class TipoCertificadoController {

    private final ITipoCertificado tipoCertificadoService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TipoCertificadoDto>> create(@Valid @RequestBody TipoCertificadoDto tipoCertificadoDto) {
        TipoCertificadoDto saved = tipoCertificadoService.create(tipoCertificadoDto);
        return new ResponseEntity<>(ApiResponse.<TipoCertificadoDto>builder()
                .message("Tipo de certificado creado exitosamente.")
                .datos(saved)
                .nombreModelo("TipoCertificado")
                .build(), HttpStatus.CREATED);
    }

    @GetMapping("/findall")
    @PreAuthorize("hasAuthority('Ver Certificados')")
    public ResponseEntity<ApiResponse<List<TipoCertificadoDto>>> findAll() {
        List<TipoCertificadoDto> tipoCertificados = tipoCertificadoService.findAll();
        return ResponseEntity.ok(ApiResponse.<List<TipoCertificadoDto>>builder()
                .message("Listado de tipos de certificado")
                .datos(tipoCertificados)
                .nombreModelo("TipoCertificado")
                .build());
    }

    @GetMapping("/showbyid/{id}")
    @PreAuthorize("hasAuthority('Ver Certificados')")
    public ResponseEntity<ApiResponse<TipoCertificadoDto>> showById(@PathVariable Long id) {
        TipoCertificadoDto tipoCertificado = tipoCertificadoService.findById(id);
        if (tipoCertificado == null) throw new NotFoundExceptionResource("TipoCertificado", "id", id);
        return ResponseEntity.ok(ApiResponse.<TipoCertificadoDto>builder()
                .message("Tipo de certificado encontrado.")
                .datos(tipoCertificado)
                .nombreModelo("TipoCertificado")
                .build());
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TipoCertificadoDto>> update(@Valid @RequestBody TipoCertificadoDto tipoCertificadoDto) {
        TipoCertificadoDto updated = tipoCertificadoService.update(tipoCertificadoDto);
        return ResponseEntity.ok(ApiResponse.<TipoCertificadoDto>builder()
                .message("Tipo de certificado actualizado exitosamente.")
                .datos(updated)
                .nombreModelo("TipoCertificado")
                .build());
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        tipoCertificadoService.delete(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Tipo de certificado eliminado exitosamente.")
                .datos(null)
                .nombreModelo("TipoCertificado")
                .build());
    }

    @PutMapping("/estado/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> estado(@PathVariable Long id) {
        tipoCertificadoService.estado(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Estado del tipo de certificado actualizado exitosamente.")
                .datos(null)
                .nombreModelo("TipoCertificado")
                .build());
    }
}