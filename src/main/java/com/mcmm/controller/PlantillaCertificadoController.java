package com.mcmm.controller;

import com.mcmm.model.dto.plantillaCertificado.PlantillaCertificadoDto;
import com.mcmm.model.entity.PlantillaCertificado;
import com.mcmm.model.payload.ApiResponse;
import com.mcmm.service.PlantillaCertificadoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/plantilla-certificado")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO', 'PASTOR')")
@SecurityRequirement(name = "bearerAuth")
public class PlantillaCertificadoController {

    private final PlantillaCertificadoService plantillaCertificadoService;
    private final ModelMapper modelMapper;

    @GetMapping
    @PreAuthorize("hasAuthority('CERTIFICADOS:VER') OR hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<PlantillaCertificadoDto>>> findAll() {
        List<PlantillaCertificado> plantillas = plantillaCertificadoService.findAll();
        List<PlantillaCertificadoDto> dtos = plantillas.stream()
                .map(plantilla -> modelMapper.map(plantilla, PlantillaCertificadoDto.class))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.<List<PlantillaCertificadoDto>>builder()
                .message("Lista de plantillas")
                .datos(dtos)
                .nombreModelo("PlantillaCertificado")
                .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CERTIFICADOS:VER') OR hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PlantillaCertificadoDto>> findById(@PathVariable Long id) {
        PlantillaCertificado plantilla = plantillaCertificadoService.findById(id);
        PlantillaCertificadoDto dto = modelMapper.map(plantilla, PlantillaCertificadoDto.class);
        return ResponseEntity.ok(ApiResponse.<PlantillaCertificadoDto>builder()
                .message("Plantilla encontrada")
                .datos(dto)
                .nombreModelo("PlantillaCertificado")
                .build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CERTIFICADOS:GENERAR') OR hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PlantillaCertificadoDto>> create(@Valid @RequestBody PlantillaCertificadoDto plantillaCertificadoDto) {
        PlantillaCertificado plantilla = plantillaCertificadoService.save(plantillaCertificadoDto);
        PlantillaCertificadoDto dto = modelMapper.map(plantilla, PlantillaCertificadoDto.class);
        return new ResponseEntity<>(ApiResponse.<PlantillaCertificadoDto>builder()
                .message("Plantilla creada exitosamente")
                .datos(dto)
                .nombreModelo("PlantillaCertificado")
                .build(), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CERTIFICADOS:GENERAR') OR hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PlantillaCertificadoDto>> update(@Valid @RequestBody PlantillaCertificadoDto plantillaCertificadoDto, @PathVariable Long id) {
        PlantillaCertificado plantilla = plantillaCertificadoService.update(plantillaCertificadoDto, id);
        PlantillaCertificadoDto dto = modelMapper.map(plantilla, PlantillaCertificadoDto.class);
        return ResponseEntity.ok(ApiResponse.<PlantillaCertificadoDto>builder()
                .message("Plantilla actualizada exitosamente")
                .datos(dto)
                .nombreModelo("PlantillaCertificado")
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CERTIFICADOS:GENERAR') OR hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        plantillaCertificadoService.delete(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Plantilla eliminada exitosamente")
                .datos(null)
                .nombreModelo("PlantillaCertificado")
                .build());
    }

    @PutMapping("/estado/{id}")
    @PreAuthorize("hasAuthority('CERTIFICADOS:GENERAR') OR hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PlantillaCertificadoDto>> changeState(@PathVariable Long id) {
        PlantillaCertificado plantilla = plantillaCertificadoService.changeState(id);
        PlantillaCertificadoDto dto = modelMapper.map(plantilla, PlantillaCertificadoDto.class);
        return ResponseEntity.ok(ApiResponse.<PlantillaCertificadoDto>builder()
                .message("Estado de plantilla cambiado exitosamente")
                .datos(dto)
                .nombreModelo("PlantillaCertificado")
                .build());
    }

    @PostMapping(value = "/{id}/logo", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('CERTIFICADOS:GENERAR') OR hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> uploadLogo(
            @PathVariable Long id,
            @RequestPart("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            String fileUrl = plantillaCertificadoService.uploadLogo(id, file);
            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .message("Logo subido exitosamente")
                    .datos(fileUrl)
                    .nombreModelo("PlantillaCertificado")
                    .build());
        } catch (java.io.IOException e) {
            throw new RuntimeException("Error al subir el logo: " + e.getMessage());
        }
    }

    @PostMapping(value = "/{id}/marca-agua", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('CERTIFICADOS:GENERAR') OR hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> uploadMarcaAgua(
            @PathVariable Long id,
            @RequestPart("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            String fileUrl = plantillaCertificadoService.uploadMarcaAgua(id, file);
            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .message("Marca de agua subida exitosamente")
                    .datos(fileUrl)
                    .nombreModelo("PlantillaCertificado")
                    .build());
        } catch (java.io.IOException e) {
            throw new RuntimeException("Error al subir la marca de agua: " + e.getMessage());
        }
    }

    @PostMapping(value = "/{id}/firma", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('CERTIFICADOS:GENERAR') OR hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> uploadFirma(
            @PathVariable Long id,
            @RequestPart("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            String fileUrl = plantillaCertificadoService.uploadFirma(id, file);
            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .message("Firma subida exitosamente")
                    .datos(fileUrl)
                    .nombreModelo("PlantillaCertificado")
                    .build());
        } catch (java.io.IOException e) {
            throw new RuntimeException("Error al subir la firma: " + e.getMessage());
        }
    }
}
