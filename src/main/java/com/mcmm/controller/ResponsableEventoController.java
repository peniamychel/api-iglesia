package com.mcmm.controller;

import com.mcmm.exception.NotFoundExceptionResource;
import com.mcmm.model.dto.responsableEvento.ResponsableEventoDto;
import com.mcmm.model.payload.ApiResponse;
import com.mcmm.service.IResponsableEvento;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/responsable-evento/v1")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO', 'PASTOR')")
public class ResponsableEventoController {

    private final IResponsableEvento responsableEventoService;

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('Escribir Eventos')")
    public ResponseEntity<ApiResponse<ResponsableEventoDto>> create(@Valid @RequestBody ResponsableEventoDto responsableEventoDto) {
        ResponsableEventoDto saved = responsableEventoService.create(responsableEventoDto);
        return new ResponseEntity<>(ApiResponse.<ResponsableEventoDto>builder()
                .message("Responsable de evento creado exitosamente.")
                .datos(saved)
                .nombreModelo("ResponsableEvento")
                .build(), HttpStatus.CREATED);
    }

    @GetMapping("/findall")
    @PreAuthorize("hasAuthority('Ver Eventos')")
    public ResponseEntity<ApiResponse<List<ResponsableEventoDto>>> findAll() {
        List<ResponsableEventoDto> responsables = responsableEventoService.findAll();
        return ResponseEntity.ok(ApiResponse.<List<ResponsableEventoDto>>builder()
                .message("Listado de responsables de evento")
                .datos(responsables)
                .nombreModelo("ResponsableEvento")
                .build());
    }

    @GetMapping("/showbyid/{id}")
    @PreAuthorize("hasAuthority('Ver Eventos')")
    public ResponseEntity<ApiResponse<ResponsableEventoDto>> showById(@PathVariable Long id) {
        ResponsableEventoDto responsable = responsableEventoService.findById(id);
        if (responsable == null) throw new NotFoundExceptionResource("ResponsableEvento", "id", id);
        return ResponseEntity.ok(ApiResponse.<ResponsableEventoDto>builder()
                .message("Responsable de evento encontrado.")
                .datos(responsable)
                .nombreModelo("ResponsableEvento")
                .build());
    }

    @PutMapping("/update")
    @PreAuthorize("hasAuthority('Escribir Eventos')")
    public ResponseEntity<ApiResponse<ResponsableEventoDto>> update(@Valid @RequestBody ResponsableEventoDto responsableEventoDto) {
        ResponsableEventoDto updated = responsableEventoService.update(responsableEventoDto);
        return ResponseEntity.ok(ApiResponse.<ResponsableEventoDto>builder()
                .message("Responsable de evento actualizado exitosamente.")
                .datos(updated)
                .nombreModelo("ResponsableEvento")
                .build());
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('Escribir Eventos')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        responsableEventoService.delete(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Responsable de evento eliminado exitosamente.")
                .datos(null)
                .nombreModelo("ResponsableEvento")
                .build());
    }

    @PutMapping("/estado/{id}")
    @PreAuthorize("hasAuthority('Escribir Eventos')")
    public ResponseEntity<ApiResponse<Void>> estado(@PathVariable Long id) {
        responsableEventoService.estado(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Estado del responsable de evento actualizado exitosamente.")
                .datos(null)
                .nombreModelo("ResponsableEvento")
                .build());
    }
}