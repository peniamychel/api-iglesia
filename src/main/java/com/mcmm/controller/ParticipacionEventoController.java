package com.mcmm.controller;

import com.mcmm.exception.NotFoundExceptionResource;
import com.mcmm.model.dto.participacionEvento.ParticipacionEventoDto;
import com.mcmm.model.payload.ApiResponse;
import com.mcmm.service.IParticipacionEvento;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/participacion-evento/v1")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO')")
public class ParticipacionEventoController {

    private final IParticipacionEvento participacionEventoService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<ParticipacionEventoDto>> create(@Valid @RequestBody ParticipacionEventoDto participacionEventoDto) {
        ParticipacionEventoDto saved = participacionEventoService.create(participacionEventoDto);
        return new ResponseEntity<>(ApiResponse.<ParticipacionEventoDto>builder()
                .message("Participación en evento creada exitosamente.")
                .datos(saved)
                .nombreModelo("ParticipacionEvento")
                .build(), HttpStatus.CREATED);
    }

    @GetMapping("/findall")
    public ResponseEntity<ApiResponse<List<ParticipacionEventoDto>>> findAll() {
        List<ParticipacionEventoDto> participaciones = participacionEventoService.findAll();
        return ResponseEntity.ok(ApiResponse.<List<ParticipacionEventoDto>>builder()
                .message("Listado de participaciones en evento")
                .datos(participaciones)
                .nombreModelo("ParticipacionEvento")
                .build());
    }

    @GetMapping("/showbyid/{id}")
    public ResponseEntity<ApiResponse<ParticipacionEventoDto>> showById(@PathVariable Long id) {
        ParticipacionEventoDto participacion = participacionEventoService.findById(id);
        if (participacion == null) throw new NotFoundExceptionResource("ParticipacionEvento", "id", id);
        return ResponseEntity.ok(ApiResponse.<ParticipacionEventoDto>builder()
                .message("Participación en evento encontrada.")
                .datos(participacion)
                .nombreModelo("ParticipacionEvento")
                .build());
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<ParticipacionEventoDto>> update(@Valid @RequestBody ParticipacionEventoDto participacionEventoDto) {
        ParticipacionEventoDto updated = participacionEventoService.update(participacionEventoDto);
        return ResponseEntity.ok(ApiResponse.<ParticipacionEventoDto>builder()
                .message("Participación en evento actualizada exitosamente.")
                .datos(updated)
                .nombreModelo("ParticipacionEvento")
                .build());
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        participacionEventoService.delete(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Participación en evento eliminada exitosamente.")
                .datos(null)
                .nombreModelo("ParticipacionEvento")
                .build());
    }

    @PutMapping("/estado/{id}")
    public ResponseEntity<ApiResponse<Void>> estado(@PathVariable Long id) {
        participacionEventoService.estado(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Estado de la participación en evento actualizado exitosamente.")
                .datos(null)
                .nombreModelo("ParticipacionEvento")
                .build());
    }
}