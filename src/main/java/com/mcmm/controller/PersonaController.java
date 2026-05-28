package com.mcmm.controller;

import com.mcmm.exception.BadRequestException;
import com.mcmm.model.dto.personaDto.PersonaDto;
import com.mcmm.model.payload.ApiResponse;
import com.mcmm.service.IPersona;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/persona/v1")
@PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO')")
@RequiredArgsConstructor
public class PersonaController {

    private final IPersona personaService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO') AND hasAuthority('Gestionar Personas')")
    public ResponseEntity<ApiResponse<PersonaDto>> create(@RequestBody @Valid PersonaDto personaDto) {
        PersonaDto personaSave = personaService.save(personaDto);
        return new ResponseEntity<>(
                ApiResponse.<PersonaDto>builder()
                        .message("Persona guardada exitosamente.")
                        .datos(personaSave)
                        .nombreModelo("Persona")
                        .build(),
                HttpStatus.CREATED);
    }

    @GetMapping("/findall")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApiResponse<List<PersonaDto>>> findAll() {
        List<PersonaDto> personaDtos = personaService.findAll();
        return ResponseEntity.ok(
                ApiResponse.<List<PersonaDto>>builder()
                        .message("Listado de Personas")
                        .datos(personaDtos)
                        .nombreModelo("Persona")
                        .build());
    }

    @PutMapping("/update")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO') AND hasAuthority('Gestionar Personas')")
    public ResponseEntity<ApiResponse<PersonaDto>> update(@RequestBody @Valid PersonaDto personaDto) {
        PersonaDto personaUpdate = personaService.update(personaDto.getId(), personaDto);
        return ResponseEntity.ok(
                ApiResponse.<PersonaDto>builder()
                        .message("Persona actualizada exitosamente.")
                        .datos(personaUpdate)
                        .nombreModelo("Persona")
                        .build());
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO') AND hasAuthority('Gestionar Personas')")
    public ResponseEntity<ApiResponse<PersonaDto>> partialUpdate(@PathVariable Long id, @RequestBody PersonaDto partialDto) {
        if (partialDto.getNombre() == null && partialDto.getApellido() == null &&
                partialDto.getCi() == null && partialDto.getFechaNac() == null &&
                partialDto.getCelular() == null && partialDto.getSexo() == null &&
                partialDto.getDireccion() == null && partialDto.getEstado() == null) {
            throw new BadRequestException("Debe proporcionar al menos un campo para actualizar.");
        }
        PersonaDto personaUpdated = personaService.partialUpdate(id, partialDto);
        return ResponseEntity.ok(
                ApiResponse.<PersonaDto>builder()
                        .message("Persona actualizada parcialmente exitosamente.")
                        .datos(personaUpdated)
                        .nombreModelo("Persona")
                        .build());
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO') AND hasAuthority('Gestionar Personas')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        personaService.delete(id);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .message("Persona eliminada exitosamente.")
                        .datos(null)
                        .nombreModelo("Persona")
                        .build());
    }

    @GetMapping("/showbyid/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApiResponse<PersonaDto>> showById(@PathVariable("id") Long id) {
        PersonaDto personaFiedById = personaService.findById(id);
        return ResponseEntity.ok(
                ApiResponse.<PersonaDto>builder()
                        .message("Persona encontrada.")
                        .datos(personaFiedById)
                        .nombreModelo("Persona")
                        .build());
    }

    @GetMapping("/personanomiembro")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApiResponse<List<PersonaDto>>> personaNoMiembro() {
        List<PersonaDto> personaDtos = personaService.personaNoMiembro();
        return ResponseEntity.ok(
                ApiResponse.<List<PersonaDto>>builder()
                        .message("Listado de Personas sin miembro")
                        .datos(personaDtos)
                        .nombreModelo("Persona")
                        .build());
    }

    @GetMapping("/showbyci/{ci}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApiResponse<PersonaDto>> buscarCi(@PathVariable("ci") String ci) {
        PersonaDto personaFiedById = personaService.buscarCi(ci);
        if (personaFiedById == null) {
            return ResponseEntity.ok(
                    ApiResponse.<PersonaDto>builder()
                            .message("Persona no encontrada con CI: " + ci)
                            .datos(null)
                            .nombreModelo("Persona")
                            .build());
        }
        return ResponseEntity.ok(
                ApiResponse.<PersonaDto>builder()
                        .message("Persona encontrada.")
                        .datos(personaFiedById)
                        .nombreModelo("Persona")
                        .build());
    }

    @PostMapping("/{id}/foto")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO') AND hasAuthority('Gestionar Personas')")
    public ResponseEntity<ApiResponse<String>> uploadProfilePhoto(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        try {
            String fileUrl = personaService.updateProfilePhoto(id, file);
            return ResponseEntity.ok(
                    ApiResponse.<String>builder()
                            .message("Foto de perfil actualizada exitosamente.")
                            .datos(fileUrl)
                            .nombreModelo("Persona")
                            .build());
        } catch (IOException e) {
            throw new RuntimeException("Error al subir la foto: " + e.getMessage());
        }
    }
}
