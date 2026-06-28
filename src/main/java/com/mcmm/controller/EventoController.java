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
            bitacoraService.registrar(null, username, accion, "EVENTO", descripcion, clientIp);
        } catch (Exception e) {
            // Ignorar
        }
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('Escribir Eventos')")
    public ResponseEntity<ApiResponse<EventoDto>> create(@Valid @RequestBody EventoDto eventoDto) {
        EventoDto saved = eventoService.create(eventoDto);
        registrarLog("CREAR", "Creó el evento: " + saved.getNombre() + " (Fecha: " + saved.getFechaInicio() + ")");
        return new ResponseEntity<>(ApiResponse.<EventoDto>builder()
                .message("Evento creado exitosamente.")
                .datos(saved)
                .nombreModelo("Evento")
                .build(), HttpStatus.CREATED);
    }

    @GetMapping("/findall")
    @PreAuthorize("hasAuthority('Ver Eventos')")
    public ResponseEntity<ApiResponse<List<EventoDto>>> findAll() {
        List<EventoDto> eventos = eventoService.findAll();
        return ResponseEntity.ok(ApiResponse.<List<EventoDto>>builder()
                .message("Listado de eventos")
                .datos(eventos)
                .nombreModelo("Evento")
                .build());
    }

    @GetMapping("/showbyid/{id}")
    @PreAuthorize("hasAuthority('Ver Eventos')")
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
    @PreAuthorize("hasAuthority('Escribir Eventos')")
    public ResponseEntity<ApiResponse<EventoDto>> update(@Valid @RequestBody EventoDto eventoDto) {
        EventoDto updated = eventoService.update(eventoDto);
        registrarLog("MODIFICAR", "Actualizó el evento ID: " + updated.getId() + " - " + updated.getNombre());
        return ResponseEntity.ok(ApiResponse.<EventoDto>builder()
                .message("Evento actualizado exitosamente.")
                .datos(updated)
                .nombreModelo("Evento")
                .build());
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('Escribir Eventos')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        eventoService.delete(id);
        registrarLog("ELIMINAR", "Eliminó el evento ID: " + id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Evento eliminado exitosamente.")
                .datos(null)
                .nombreModelo("Evento")
                .build());
    }

    @PutMapping("/estado/{id}")
    @PreAuthorize("hasAuthority('Escribir Eventos')")
    public ResponseEntity<ApiResponse<Void>> estado(@PathVariable Long id) {
        eventoService.estado(id);
        registrarLog("MODIFICAR_ESTADO", "Modificó el estado del evento ID: " + id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Estado del evento actualizado exitosamente.")
                .datos(null)
                .nombreModelo("Evento")
                .build());
    }

    @PostMapping("/clonar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> cloneYear(
            @RequestParam("from") int fromYear,
            @RequestParam("to") int toYear) {
        eventoService.cloneYearEvents(fromYear, toYear);
        registrarLog("CLONAR", "Clonó los eventos anuales de la gestión " + fromYear + " a la gestión " + toYear);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Eventos clonados exitosamente de la gestión " + fromYear + " a " + toYear)
                .datos(null)
                .nombreModelo("Evento")
                .build());
    }
}