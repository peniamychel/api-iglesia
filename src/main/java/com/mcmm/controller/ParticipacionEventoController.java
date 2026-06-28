package com.mcmm.controller;

import com.mcmm.model.dto.participacionEvento.ParticipacionEventoDto;
import com.mcmm.model.payload.ApiResponse;
import com.mcmm.service.IParticipacionEvento;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/participacion-evento/v1")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO', 'PASTOR')")
public class ParticipacionEventoController {

    private final IParticipacionEvento participacionEventoService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('Escribir Eventos')")
    public ResponseEntity<ApiResponse<ParticipacionEventoDto>> create(@Valid @RequestBody ParticipacionEventoDto participacionEventoDto) {
        ParticipacionEventoDto saved = participacionEventoService.create(participacionEventoDto);
        return new ResponseEntity<>(ApiResponse.<ParticipacionEventoDto>builder()
                .message("Participación en evento creada exitosamente.")
                .datos(saved)
                .nombreModelo("ParticipacionEvento")
                .build(), HttpStatus.CREATED);
    }

    @GetMapping("/findall")
    @PreAuthorize("hasAuthority('Ver Eventos')")
    public ResponseEntity<ApiResponse<List<ParticipacionEventoDto>>> findAll() {
        List<ParticipacionEventoDto> participaciones = participacionEventoService.findAll();
        return ResponseEntity.ok(ApiResponse.<List<ParticipacionEventoDto>>builder()
                .message("Listado de participaciones en evento")
                .datos(participaciones)
                .nombreModelo("ParticipacionEvento")
                .build());
    }

    @GetMapping("/showbyid/{id}")
    @PreAuthorize("hasAuthority('Ver Eventos')")
    public ResponseEntity<ApiResponse<ParticipacionEventoDto>> showById(@PathVariable Long id) {
        ParticipacionEventoDto participacion = participacionEventoService.findById(id);
        return ResponseEntity.ok(ApiResponse.<ParticipacionEventoDto>builder()
                .message("Participación en evento encontrada.")
                .datos(participacion)
                .nombreModelo("ParticipacionEvento")
                .build());
    }

    @PutMapping("/update")
    @PreAuthorize("hasAuthority('Escribir Eventos')")
    public ResponseEntity<ApiResponse<ParticipacionEventoDto>> update(@Valid @RequestBody ParticipacionEventoDto participacionEventoDto) {
        ParticipacionEventoDto updated = participacionEventoService.update(participacionEventoDto);
        return ResponseEntity.ok(ApiResponse.<ParticipacionEventoDto>builder()
                .message("Participación en evento actualizada exitosamente.")
                .datos(updated)
                .nombreModelo("ParticipacionEvento")
                .build());
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('Escribir Eventos')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        participacionEventoService.delete(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Participación en evento eliminada exitosamente.")
                .datos(null)
                .nombreModelo("ParticipacionEvento")
                .build());
    }

    @PutMapping("/estado/{id}")
    @PreAuthorize("hasAuthority('Escribir Eventos')")
    public ResponseEntity<ApiResponse<Void>> estado(@PathVariable Long id) {
        participacionEventoService.estado(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Estado de la participación en evento actualizado exitosamente.")
                .datos(null)
                .nombreModelo("ParticipacionEvento")
                .build());
    }

    @PutMapping("/entregado/{id}")
    @PreAuthorize("hasAuthority('Escribir Eventos')")
    public ResponseEntity<ApiResponse<Void>> toggleEntregado(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        participacionEventoService.toggleEntregado(id, username);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Estado de entrega del certificado actualizado exitosamente.")
                .datos(null)
                .nombreModelo("ParticipacionEvento")
                .build());
    }
}
