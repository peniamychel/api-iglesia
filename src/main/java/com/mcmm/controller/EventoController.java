package com.mcmm.controller;

import com.mcmm.exception.NotFoundExceptionResource;
import com.mcmm.model.dto.evento.EventoDto;
import com.mcmm.model.payload.ApiResponse;
import com.mcmm.service.IEvento;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/evento/v1")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO', 'PASTOR')")
public class EventoController {

    private final IEvento eventoService;

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('Gestionar Eventos')")
    public ResponseEntity<ApiResponse<EventoDto>> create(@Valid @RequestBody EventoDto eventoDto) {
        EventoDto saved = eventoService.create(eventoDto);
        return new ResponseEntity<>(ApiResponse.<EventoDto>builder()
                .message("Evento creado exitosamente.")
                .datos(saved)
                .nombreModelo("Evento")
                .build(), HttpStatus.CREATED);
    }

    @GetMapping("/findall")
    public ResponseEntity<ApiResponse<List<EventoDto>>> findAll() {
        List<EventoDto> eventos = eventoService.findAll();
        return ResponseEntity.ok(ApiResponse.<List<EventoDto>>builder()
                .message("Listado de eventos")
                .datos(eventos)
                .nombreModelo("Evento")
                .build());
    }

    @GetMapping("/showbyid/{id}")
    public ResponseEntity<ApiResponse<EventoDto>> showById(@PathVariable Long id) {
        EventoDto evento = eventoService.findById(id);
        if (evento == null) throw new NotFoundExceptionResource("Evento", "id", id);
        return ResponseEntity.ok(ApiResponse.<EventoDto>builder()
                .message("Evento encontrado.")
                .datos(evento)
                .nombreModelo("Evento")
                .build());
    }

    @PutMapping("/update")
    @PreAuthorize("hasAuthority('Gestionar Eventos')")
    public ResponseEntity<ApiResponse<EventoDto>> update(@Valid @RequestBody EventoDto eventoDto) {
        EventoDto updated = eventoService.update(eventoDto);
        return ResponseEntity.ok(ApiResponse.<EventoDto>builder()
                .message("Evento actualizado exitosamente.")
                .datos(updated)
                .nombreModelo("Evento")
                .build());
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('Gestionar Eventos')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        eventoService.delete(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Evento eliminado exitosamente.")
                .datos(null)
                .nombreModelo("Evento")
                .build());
    }

    @PutMapping("/estado/{id}")
    @PreAuthorize("hasAuthority('Gestionar Eventos')")
    public ResponseEntity<ApiResponse<Void>> estado(@PathVariable Long id) {
        eventoService.estado(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Estado del evento actualizado exitosamente.")
                .datos(null)
                .nombreModelo("Evento")
                .build());
    }
}