package com.mcmm.controller;

import com.mcmm.model.dto.eventoAceptacion.EventoAceptacionDto;
import com.mcmm.model.payload.ApiResponse;
import com.mcmm.service.IEventoAceptacion;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/evento-aceptacion/v1")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO', 'PASTOR')")
public class EventoAceptacionController {

    private final IEventoAceptacion eventoAceptacionService;

    @PostMapping("/decidir")
    public ResponseEntity<ApiResponse<EventoAceptacionDto>> decidir(@Valid @RequestBody EventoAceptacionDto dto) {
        EventoAceptacionDto result = eventoAceptacionService.decidir(dto);
        return new ResponseEntity<>(ApiResponse.<EventoAceptacionDto>builder()
                .message("Decisión de evento registrada exitosamente.")
                .datos(result)
                .nombreModelo("EventoAceptacion")
                .build(), HttpStatus.OK);
    }

    @GetMapping("/iglesia/{iglesiaId}")
    public ResponseEntity<ApiResponse<List<EventoAceptacionDto>>> findByIglesiaId(@PathVariable Long iglesiaId) {
        List<EventoAceptacionDto> list = eventoAceptacionService.findByIglesiaId(iglesiaId);
        return ResponseEntity.ok(ApiResponse.<List<EventoAceptacionDto>>builder()
                .message("Listado de decisiones de invitaciones a eventos para la iglesia")
                .datos(list)
                .nombreModelo("EventoAceptacion")
                .build());
    }

    @GetMapping("/evento/{eventoId}/iglesia/{iglesiaId}")
    public ResponseEntity<ApiResponse<EventoAceptacionDto>> findByEventoIdAndIglesiaId(
            @PathVariable Long eventoId, @PathVariable Long iglesiaId) {
        EventoAceptacionDto result = eventoAceptacionService.findByEventoIdAndIglesiaId(eventoId, iglesiaId);
        return ResponseEntity.ok(ApiResponse.<EventoAceptacionDto>builder()
                .message("Aceptación de evento encontrada")
                .datos(result)
                .nombreModelo("EventoAceptacion")
                .build());
    }
}
