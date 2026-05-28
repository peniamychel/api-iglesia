package com.mcmm.controller;

import com.mcmm.exception.BadRequestException;
import com.mcmm.exception.NotFoundExceptionResource;
import com.mcmm.model.dto.iglesiaDto.IglesiaDto;
import com.mcmm.model.payload.ApiResponse;
import com.mcmm.service.IIglesia;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/iglesia/v1")
@PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO')")
@RequiredArgsConstructor
public class IglesiaController {

    private final IIglesia iglesiaService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO') AND hasAuthority('Gestionar Iglesias')")
    public ResponseEntity<ApiResponse<IglesiaDto>> create(@RequestBody @Valid IglesiaDto iglesiaDto) {
        IglesiaDto existeIglesia = iglesiaService.buscarNombreIglesia(iglesiaDto.getNombre());
        if (existeIglesia != null) {
            throw new BadRequestException("El nombre de la iglesia ya existe.");
        }
        IglesiaDto iglesiaSave = iglesiaService.save(iglesiaDto);
        return new ResponseEntity<>(
                ApiResponse.<IglesiaDto>builder()
                        .message("Iglesia guardada exitosamente.")
                        .datos(iglesiaSave)
                        .nombreModelo("Iglesia")
                        .build(),
                HttpStatus.CREATED);
    }

    @PutMapping("/update")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO') AND hasAuthority('Gestionar Iglesias')")
    public ResponseEntity<ApiResponse<IglesiaDto>> update(@RequestBody @Valid IglesiaDto iglesiaDto) {
        IglesiaDto iglesiaUpdate = iglesiaService.save(iglesiaDto);
        return ResponseEntity.ok(
                ApiResponse.<IglesiaDto>builder()
                        .message("Iglesia actualizada exitosamente.")
                        .datos(iglesiaUpdate)
                        .nombreModelo("Iglesia")
                        .build());
    }

    @PutMapping("/update2/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO') AND hasAuthority('Gestionar Iglesias')")
    public ResponseEntity<ApiResponse<IglesiaDto>> update(
            @PathVariable Long id,
            @RequestBody @Valid IglesiaDto iglesiaDto) {
        IglesiaDto iglesiaUpdate = iglesiaService.update(id, iglesiaDto);
        return ResponseEntity.ok(
                ApiResponse.<IglesiaDto>builder()
                        .message("Iglesia actualizada exitosamente.")
                        .datos(iglesiaUpdate)
                        .nombreModelo("Iglesia")
                        .build());
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO') AND hasAuthority('Gestionar Iglesias')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        iglesiaService.delete(id);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .message("Iglesia eliminada exitosamente.")
                        .datos(null)
                        .nombreModelo("Iglesia")
                        .build());
    }

    @GetMapping("/showbyid/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApiResponse<IglesiaDto>> showById(@PathVariable("id") Long id) {
        IglesiaDto iglesiaFiedById = iglesiaService.findById(id);
        return ResponseEntity.ok(
                ApiResponse.<IglesiaDto>builder()
                        .message("Iglesia encontrada.")
                        .datos(iglesiaFiedById)
                        .nombreModelo("Iglesia")
                        .build());
    }

    @GetMapping("/findall")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApiResponse<List<IglesiaDto>>> findAll() {
        List<IglesiaDto> iglesiaDtos = iglesiaService.findAll();
        return ResponseEntity.ok(
                ApiResponse.<List<IglesiaDto>>builder()
                        .message("Listado de iglesias")
                        .datos(iglesiaDtos)
                        .nombreModelo("Iglesia")
                        .build());
    }

    @PutMapping("/estado/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO') AND hasAuthority('Gestionar Iglesias')")
    public ResponseEntity<ApiResponse<IglesiaDto>> estado(@PathVariable("id") Long id) {
        IglesiaDto iglesiaActualizado = iglesiaService.estado(id);
        return ResponseEntity.ok(
                ApiResponse.<IglesiaDto>builder()
                        .message("Se cambió el estado de la iglesia exitosamente a: " + iglesiaActualizado.getEstado())
                        .datos(iglesiaActualizado)
                        .nombreModelo("Iglesia")
                        .build());
    }

    @GetMapping("/showbynombreiglesia/{nameIglesia}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApiResponse<IglesiaDto>> buscarNombreIglesia(
            @PathVariable("nameIglesia") String nameIglesia) {
        IglesiaDto buscarNombreIglesia = iglesiaService.buscarNombreIglesia(nameIglesia);
        if (buscarNombreIglesia == null) {
            throw new NotFoundExceptionResource("Iglesia", "nameIglesia", nameIglesia);
        }
        return ResponseEntity.ok(
                ApiResponse.<IglesiaDto>builder()
                        .message("Iglesia encontrada.")
                        .datos(buscarNombreIglesia)
                        .nombreModelo("Iglesia")
                        .build());
    }

    @GetMapping("/showbynombreiglesiaexceptoid/{nameIglesia}/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApiResponse<IglesiaDto>> buscarNombreIglesiaExceptoId(
            @PathVariable("nameIglesia") String nameIglesia, @PathVariable("id") Long id) {
        IglesiaDto buscarNombreIglesia = iglesiaService.buscarNombreIglesiaExceptoId(id, nameIglesia);
        if (buscarNombreIglesia == null) {
            throw new NotFoundExceptionResource("Iglesia", "nameIglesia", nameIglesia);
        }
        return ResponseEntity.ok(
                ApiResponse.<IglesiaDto>builder()
                        .message("Iglesia encontrada.")
                        .datos(buscarNombreIglesia)
                        .nombreModelo("Iglesia")
                        .build());
    }

    @GetMapping("/checkbynombreandidnot/{nameIglesia}/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApiResponse<IglesiaDto>> findByNombreAndIdNot(
            @PathVariable("nameIglesia") String nameIglesia, @PathVariable("id") Long id) {
        IglesiaDto buscarNombreIglesia = iglesiaService.findByNombreAndIdNot(nameIglesia, id);
        if (buscarNombreIglesia == null) {
            throw new NotFoundExceptionResource("Iglesia", "nameIglesia", nameIglesia);
        }
        return ResponseEntity.ok(
                ApiResponse.<IglesiaDto>builder()
                        .message("Iglesia encontrada.")
                        .datos(buscarNombreIglesia)
                        .nombreModelo("Iglesia")
                        .build());
    }

    @GetMapping("/findall-activas")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApiResponse<List<IglesiaDto>>> findByEstadoTrue() {
        List<IglesiaDto> iglesiaDtos = iglesiaService.findByEstadoTrue();
        return ResponseEntity.ok(
                ApiResponse.<List<IglesiaDto>>builder()
                        .message("Listado de iglesias activas")
                        .datos(iglesiaDtos)
                        .nombreModelo("Iglesia")
                        .build());
    }
}
