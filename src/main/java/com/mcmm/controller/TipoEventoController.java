package com.mcmm.controller;

import com.mcmm.exception.NotFoundExceptionResource;
import com.mcmm.model.dto.tipoEvento.TipoEventoDto;
import com.mcmm.model.payload.ApiResponse;
import com.mcmm.service.ITipoEvento;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tipo-evento/v1")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO', 'PASTOR')")
public class TipoEventoController {

    private final ITipoEvento tipoEventoService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TipoEventoDto>> create(@Valid @RequestBody TipoEventoDto tipoEventoDto) {
        TipoEventoDto saved = tipoEventoService.create(tipoEventoDto);
        return new ResponseEntity<>(ApiResponse.<TipoEventoDto>builder()
                .message("Tipo de evento creado exitosamente.")
                .datos(saved)
                .nombreModelo("TipoEvento")
                .build(), HttpStatus.CREATED);
    }

    @GetMapping("/findall")
    public ResponseEntity<ApiResponse<List<TipoEventoDto>>> findAll() {
        List<TipoEventoDto> tipoEventos = tipoEventoService.findAll();
        return ResponseEntity.ok(ApiResponse.<List<TipoEventoDto>>builder()
                .message("Listado de tipos de evento")
                .datos(tipoEventos)
                .nombreModelo("TipoEvento")
                .build());
    }

    @GetMapping("/showbyid/{id}")
    public ResponseEntity<ApiResponse<TipoEventoDto>> showById(@PathVariable Long id) {
        TipoEventoDto tipoEvento = tipoEventoService.findById(id);
        if (tipoEvento == null) throw new NotFoundExceptionResource("TipoEvento", "id", id);
        return ResponseEntity.ok(ApiResponse.<TipoEventoDto>builder()
                .message("Tipo de evento encontrado.")
                .datos(tipoEvento)
                .nombreModelo("TipoEvento")
                .build());
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TipoEventoDto>> update(@Valid @RequestBody TipoEventoDto tipoEventoDto) {
        TipoEventoDto updated = tipoEventoService.update(tipoEventoDto);
        return ResponseEntity.ok(ApiResponse.<TipoEventoDto>builder()
                .message("Tipo de evento actualizado exitosamente.")
                .datos(updated)
                .nombreModelo("TipoEvento")
                .build());
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        tipoEventoService.delete(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Tipo de evento eliminado exitosamente.")
                .datos(null)
                .nombreModelo("TipoEvento")
                .build());
    }

    @PutMapping("/estado/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> estado(@PathVariable Long id) {
        tipoEventoService.estado(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Estado del tipo de evento actualizado exitosamente.")
                .datos(null)
                .nombreModelo("TipoEvento")
                .build());
    }
}