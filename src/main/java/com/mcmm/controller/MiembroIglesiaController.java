package com.mcmm.controller;

import com.mcmm.exception.BadRequestException;
import com.mcmm.model.dto.GraficoDataDto;
import com.mcmm.model.dto.MiembroDto.MiembroDto;
import com.mcmm.model.dto.MiembroIglesiaDto;
import com.mcmm.model.payload.ApiResponse;
import com.mcmm.service.IMiembroIglesia;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/miembroiglesia/v1")
@PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO')")
@RequiredArgsConstructor
public class MiembroIglesiaController {

    private final IMiembroIglesia miembroIglesiaService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO') AND hasAuthority('Gestionar MiembroIglesia')")
    public ResponseEntity<ApiResponse<MiembroIglesiaDto>> create(@RequestBody @Valid MiembroIglesiaDto miembroDto) {
        MiembroIglesiaDto miembroIglesiaSave = miembroIglesiaService.save(miembroDto);
        return new ResponseEntity<>(
                ApiResponse.<MiembroIglesiaDto>builder()
                        .message("MiembroIglesia guardado exitosamente.")
                        .datos(miembroIglesiaSave)
                        .nombreModelo("MiembroIglesia")
                        .build(),
                HttpStatus.CREATED);
    }

    @PostMapping("/created")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO') AND hasAuthority('Gestionar MiembroIglesia')")
    public ResponseEntity<ApiResponse<MiembroIglesiaDto>> created(@RequestBody @Valid MiembroIglesiaDto miembroIglesiaDto) {
        boolean result = miembroIglesiaService.findByIdMiembro(miembroIglesiaDto.getMiembroId());
        if (!result) {
            throw new BadRequestException("El miembro ya pertenece a una iglesia.");
        }
        MiembroIglesiaDto saved = miembroIglesiaService.save(miembroIglesiaDto);
        return new ResponseEntity<>(
                ApiResponse.<MiembroIglesiaDto>builder()
                        .message("MiembroIglesia guardado exitosamente.")
                        .datos(saved)
                        .nombreModelo("MiembroIglesia")
                        .build(),
                HttpStatus.CREATED);
    }

    @GetMapping("/findall")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApiResponse<List<MiembroIglesiaDto>>> findAll() {
        List<MiembroIglesiaDto> miembroIglesiaDtos = miembroIglesiaService.findAll();
        return ResponseEntity.ok(
                ApiResponse.<List<MiembroIglesiaDto>>builder()
                        .message("Listado de MiembrosIglesia")
                        .datos(miembroIglesiaDtos)
                        .nombreModelo("MiembroIglesia")
                        .build());
    }

    @GetMapping("/showbyid/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApiResponse<MiembroIglesiaDto>> findById(@PathVariable("id") Long id) {
        MiembroIglesiaDto miembroIglesiaDto = miembroIglesiaService.findById(id);
        return ResponseEntity.ok(
                ApiResponse.<MiembroIglesiaDto>builder()
                        .message("MiembroIglesia encontrado.")
                        .datos(miembroIglesiaDto)
                        .nombreModelo("MiembroIglesia")
                        .build());
    }

    @PutMapping("/update")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO') AND hasAuthority('Gestionar MiembroIglesia')")
    public ResponseEntity<ApiResponse<MiembroIglesiaDto>> update(@RequestBody @Valid MiembroIglesiaDto miembroIglesiaDto) {
        MiembroIglesiaDto miembroIglesiaActualizado = miembroIglesiaService.update(miembroIglesiaDto);
        return ResponseEntity.ok(
                ApiResponse.<MiembroIglesiaDto>builder()
                        .message("MiembroIglesia actualizado exitosamente.")
                        .datos(miembroIglesiaActualizado)
                        .nombreModelo("MiembroIglesia")
                        .build());
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO') AND hasAuthority('Gestionar MiembroIglesia')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        miembroIglesiaService.delete(id);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .message("MiembroIglesia eliminado exitosamente.")
                        .datos(null)
                        .nombreModelo("MiembroIglesia")
                        .build());
    }

    @PutMapping("/estado/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO') AND hasAuthority('Gestionar MiembroIglesia')")
    public ResponseEntity<ApiResponse<MiembroIglesiaDto>> estado(@PathVariable Long id) {
        MiembroIglesiaDto miembroIglesiaDto = miembroIglesiaService.estado(id);
        return ResponseEntity.ok(
                ApiResponse.<MiembroIglesiaDto>builder()
                        .message("Se cambió el estado del MiembroIglesia exitosamente a: " + miembroIglesiaDto.getEstado())
                        .datos(miembroIglesiaDto)
                        .nombreModelo("MiembroIglesia")
                        .build());
    }

    @PutMapping("/traspaso")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO') AND hasAuthority('Gestionar MiembroIglesia')")
    public ResponseEntity<ApiResponse<MiembroIglesiaDto>> traspaso(@RequestBody @Valid MiembroIglesiaDto miembroIglesiaDto) {
        MiembroIglesiaDto miembroIglesiaActualizado = miembroIglesiaService.update(miembroIglesiaDto);
        return ResponseEntity.ok(
                ApiResponse.<MiembroIglesiaDto>builder()
                        .message("MiembroIglesia traspasado exitosamente.")
                        .datos(miembroIglesiaActualizado)
                        .nombreModelo("MiembroIglesia")
                        .build());
    }

    @GetMapping("/listmiembrosiglesia/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApiResponse<List<MiembroDto>>> findMiembrosIglesia(@PathVariable("id") Long id) {
        List<MiembroDto> miembroDtos = miembroIglesiaService.findMiembrosIglesia(id);
        return ResponseEntity.ok(
                ApiResponse.<List<MiembroDto>>builder()
                        .message("Miembros de la iglesia encontrados.")
                        .datos(miembroDtos)
                        .nombreModelo("Miembro")
                        .build());
    }

    @GetMapping("/graficomiembrosiglesia/{cant}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApiResponse<List<GraficoDataDto>>> graficoMiembrosIglesia(@PathVariable("cant") Long cant) {
        List<GraficoDataDto> miembroIglesia = miembroIglesiaService.graficoMiembrosIglesia(cant);
        return ResponseEntity.ok(
                ApiResponse.<List<GraficoDataDto>>builder()
                        .message("Gráfico para " + cant + " Iglesias.")
                        .datos(miembroIglesia)
                        .nombreModelo("MiembroIglesia")
                        .build());
    }
}
