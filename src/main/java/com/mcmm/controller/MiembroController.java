package com.mcmm.controller;

import com.mcmm.model.dto.MiembroDto.MiembroDto;
import com.mcmm.model.payload.ApiResponse;
import com.mcmm.service.IMiembro;
import com.mcmm.service.IBitacora;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

/**
 * Controlador REST para la gestión de Miembros de la iglesia.
 * Proporciona endpoints para creación, consulta, actualización, desactivación y
 * eliminación de miembros.
 * 
 * @author Antigravity
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/miembro/v1")
@PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO', 'PASTOR', 'TESORERO') OR hasAuthority('MIEMBROS:VER')")
@RequiredArgsConstructor
public class MiembroController {

    private final IMiembro miembroService;
    private final IBitacora bitacoraService;

    @org.springframework.beans.factory.annotation.Autowired
    private jakarta.servlet.http.HttpServletRequest request;

    private void registrarLog(String accion, String descripcion) {
        try {
            org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication();
            String username = authentication != null ? authentication.getName() : "Sistema";
            String clientIp = request.getHeader("X-Forwarded-For");
            if (clientIp == null || clientIp.isEmpty()) {
                clientIp = request.getRemoteAddr();
            } else {
                clientIp = clientIp.split(",")[0].trim();
            }
            bitacoraService.registrar(null, username, accion, "MIEMBRO", descripcion, clientIp);
        } catch (Exception e) {
            // Ignorar
        }
    }

    /**
     * Crea un nuevo miembro en el sistema.
     * 
     * @param miembroDto DTO con la información básica y la persona asociada a
     *                   registrar.
     * @return ResponseEntity conteniendo el miembro registrado.
     */
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('MIEMBROS:CREAR') OR hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MiembroDto>> create(@RequestBody @Valid MiembroDto miembroDto) {
        MiembroDto miembroSave = miembroService.create(miembroDto);
        registrarLog("CREAR", "Registró al miembro con CI: " + miembroSave.getCi() + " - " + miembroSave.getNombre()
                + " " + miembroSave.getApellido());
        return new ResponseEntity<>(
                ApiResponse.<MiembroDto>builder()
                        .message("Miembro guardado exitosamente.")
                        .datos(miembroSave)
                        .nombreModelo("Miembro")
                        .build(),
                HttpStatus.CREATED);
    }

    /**
     * Obtiene el listado completo de todos los miembros de la iglesia.
     * 
     * @return ResponseEntity con la lista de miembros.
     */
    @GetMapping("/findall")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('MIEMBROS:VER') OR hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Iterable<MiembroDto>>> findAll() {
        Iterable<MiembroDto> miembroDtos = miembroService.findAll();
        return ResponseEntity.ok(
                ApiResponse.<Iterable<MiembroDto>>builder()
                        .message("Listado de Miembros")
                        .datos(miembroDtos)
                        .nombreModelo("Miembro")
                        .build());
    }

    @GetMapping("/findall/paged")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('MIEMBROS:VER') OR hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<org.springframework.data.domain.Page<MiembroDto>>> findAllPaged(
            @RequestParam(value = "searchText", required = false) String searchText,
            @RequestParam(value = "estado", required = false) Boolean estado,
            @RequestParam(value = "iglesiaNombre", required = false) String iglesiaNombre,
            @org.springframework.data.web.PageableDefault(size = 10) org.springframework.data.domain.Pageable pageable) {
        org.springframework.data.domain.Page<MiembroDto> miembroDtos = miembroService.findAllPaged(searchText, estado,
                iglesiaNombre, pageable);
        return ResponseEntity.ok(
                ApiResponse.<org.springframework.data.domain.Page<MiembroDto>>builder()
                        .message("Listado paginado de Miembros")
                        .datos(miembroDtos)
                        .nombreModelo("Miembro")
                        .build());
    }

    @GetMapping("/sin-iglesia")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('MIEMBROS:VER') OR hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<java.util.List<MiembroDto>>> findSinIglesia() {
        java.util.List<MiembroDto> miembroDtos = miembroService.findSinIglesia();
        return ResponseEntity.ok(
                ApiResponse.<java.util.List<MiembroDto>>builder()
                        .message("Listado de Miembros sin Iglesia")
                        .datos(miembroDtos)
                        .nombreModelo("Miembro")
                        .build());
    }

    /**
     * Obtiene miembros activos disponibles para ser asignados a una iglesia.
     * Excluye a miembros que ya pertenecen a una iglesia activa y a quienes
     * tienen un cargo activo de PASTOR (filtrado seguro en el backend).
     */
    @GetMapping("/sin-iglesia-asignacion")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('MIEMBROS:VER') OR hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<java.util.List<MiembroDto>>> findSinIglesiaParaAsignacion() {
        java.util.List<MiembroDto> miembroDtos = miembroService.findSinIglesiaParaAsignacion();
        return ResponseEntity.ok(
                ApiResponse.<java.util.List<MiembroDto>>builder()
                        .message("Listado de Miembros disponibles para asignar a iglesia")
                        .datos(miembroDtos)
                        .nombreModelo("Miembro")
                        .build());
    }

    /**
     * Busca un miembro por su identificador único ID.
     * 
     * @param id Identificador único del miembro.
     * @return ResponseEntity con el miembro correspondiente.
     */
    @GetMapping("/showbyid/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('MIEMBROS:VER') OR hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MiembroDto>> findById(@PathVariable("id") Long id) {
        MiembroDto miembroDto = miembroService.findById(id);
        return ResponseEntity.ok(
                ApiResponse.<MiembroDto>builder()
                        .message("Miembro encontrado.")
                        .datos(miembroDto)
                        .nombreModelo("Miembro")
                        .build());
    }

    /**
     * Actualiza la información de un miembro existente.
     * 
     * @param miembroDto DTO con los datos actualizados del miembro.
     * @return ResponseEntity con el miembro actualizado.
     */
    @PutMapping("/update")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('MIEMBROS:EDITAR') OR hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MiembroDto>> update(@RequestBody @Valid MiembroDto miembroDto) {
        MiembroDto miembroActualizado = miembroService.update(miembroDto);
        registrarLog("MODIFICAR", "Actualizó al miembro ID: " + miembroActualizado.getId() + " - "
                + miembroActualizado.getNombre() + " " + miembroActualizado.getApellido());
        return ResponseEntity.ok(
                ApiResponse.<MiembroDto>builder()
                        .message("Miembro actualizado exitosamente.")
                        .datos(miembroActualizado)
                        .nombreModelo("Miembro")
                        .build());
    }

    /**
     * Elimina a un miembro del sistema por su identificador único ID.
     * 
     * @param id Identificador único del miembro a eliminar.
     * @return ResponseEntity confirmando la eliminación.
     */
    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('MIEMBROS:ELIMINAR') OR hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        miembroService.delete(id);
        registrarLog("ELIMINAR", "Eliminó al miembro con ID: " + id);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .message("Miembro eliminado exitosamente.")
                        .datos(null)
                        .nombreModelo("Miembro")
                        .build());
    }

    /**
     * Alterna el estado (activo/inactivo) de un miembro por su identificador único
     * ID.
     * 
     * @param id Identificador único del miembro.
     * @return ResponseEntity conteniendo la información actualizada con el nuevo
     *         estado del miembro.
     */
    @PutMapping("/estado/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('MIEMBROS:EDITAR') OR hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MiembroDto>> estado(@PathVariable Long id) {
        MiembroDto miembroActualizado = miembroService.estado(id);
        registrarLog("MODIFICAR_ESTADO",
                "Modificó el estado del miembro ID: " + id + " a " + miembroActualizado.getEstado());
        return ResponseEntity.ok(
                ApiResponse.<MiembroDto>builder()
                        .message("Se cambió el estado del miembro exitosamente a: " + miembroActualizado.getEstado())
                        .datos(miembroActualizado)
                        .nombreModelo("Miembro")
                        .build());
    }

    @GetMapping("/buscarci/{ci}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('MIEMBROS:VER') OR hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MiembroDto>> buscarCi(@PathVariable("ci") String ci) {
        MiembroDto miembroDto = miembroService.buscarCi(ci);
        if (miembroDto == null) {
            return new ResponseEntity<>(
                    ApiResponse.<MiembroDto>builder()
                            .message("Miembro no encontrado con CI: " + ci)
                            .datos(null)
                            .nombreModelo("Miembro")
                            .build(),
                    HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(
                ApiResponse.<MiembroDto>builder()
                        .message("Miembro encontrado.")
                        .datos(miembroDto)
                        .nombreModelo("Miembro")
                        .build());
    }

    @PostMapping("/{id}/foto")
    @PreAuthorize("hasAuthority('MIEMBROS:SUBIR_FOTO') OR hasAuthority('MIEMBROS:EDITAR') OR hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> uploadProfilePhoto(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        try {
            String fileUrl = miembroService.updateProfilePhoto(id, file);
            registrarLog("SUBIR_FOTO", "Actualizó foto de perfil del miembro ID: " + id);
            return ResponseEntity.ok(
                    ApiResponse.<String>builder()
                            .message("Foto de perfil actualizada exitosamente.")
                            .datos(fileUrl)
                            .nombreModelo("Miembro")
                            .build());
        } catch (IOException e) {
            throw new RuntimeException("Error al subir la foto: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}/foto")
    @PreAuthorize("hasAuthority('MIEMBROS:EDITAR') OR hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProfilePhoto(@PathVariable Long id) {
        miembroService.deleteProfilePhoto(id);
        registrarLog("ELIMINAR_FOTO", "Eliminó foto de perfil del miembro ID: " + id);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .message("Foto de perfil eliminada exitosamente.")
                        .datos(null)
                        .nombreModelo("Miembro")
                        .build());
    }

    private Long getCurrentIglesiaId() {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        if (authentication != null && authentication.getDetails() instanceof java.util.Map) {
            java.util.Map<?, ?> details = (java.util.Map<?, ?>) authentication.getDetails();
            Object iglesiaIdObj = details.get("iglesiaId");
            if (iglesiaIdObj instanceof Long) {
                return (Long) iglesiaIdObj;
            } else if (iglesiaIdObj instanceof Integer) {
                return ((Integer) iglesiaIdObj).longValue();
            }
        }
        return null;
    }

    @PostMapping("/importar")
    @PreAuthorize("hasAuthority('MIEMBROS:CREAR') OR hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Integer>> importExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "iglesiaId", required = false) Long paramIglesiaId) {

        Long iglesiaId = getCurrentIglesiaId();
        if (iglesiaId == null) {
            iglesiaId = paramIglesiaId;
        }

        if (iglesiaId == null) {
            return ResponseEntity.badRequest().body(ApiResponse.<Integer>builder()
                    .message("Debe especificar la iglesia de destino para los miembros.")
                    .datos(0)
                    .nombreModelo("MiembroImport")
                    .build());
        }

        try {
            int imported = miembroService.importFromExcel(file, iglesiaId);
            registrarLog("IMPORTACION",
                    "Importación masiva exitosa: " + imported + " miembros cargados desde archivo Excel.");
            return ResponseEntity.ok(ApiResponse.<Integer>builder()
                    .message("Importación masiva completada con éxito. Total importados: " + imported)
                    .datos(imported)
                    .nombreModelo("MiembroImport")
                    .build());
        } catch (IOException e) {
            throw new RuntimeException("Error al procesar el archivo Excel: " + e.getMessage());
        }
    }

    @GetMapping("/plantilla")
    @PreAuthorize("hasAuthority('MIEMBROS:VER') OR hasRole('ADMIN')")
    public ResponseEntity<byte[]> downloadTemplate() {
        try {
            byte[] data = miembroService.generateExcelTemplate();
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType
                    .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDisposition(org.springframework.http.ContentDisposition.builder("attachment")
                    .filename("Plantilla_Miembros.xlsx").build());
            return new ResponseEntity<>(data, headers, HttpStatus.OK);
        } catch (IOException e) {
            throw new RuntimeException("Error al generar la plantilla Excel: " + e.getMessage());
        }
    }
}
