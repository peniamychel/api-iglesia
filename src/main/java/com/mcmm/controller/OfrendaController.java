package com.mcmm.controller;

import com.mcmm.exception.NotFoundExceptionResource;
import com.mcmm.model.dto.OfrendaDto;
import com.mcmm.model.entity.Usuario;
import com.mcmm.model.dao.UsuarioDao;
import com.mcmm.model.payload.ApiResponse;
import com.mcmm.service.IOfrenda;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ofrenda/v1")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'TESORERO', 'PASTOR', 'ENCARGADO_IGLESIA')")
public class OfrendaController {

    private final IOfrenda ofrendaService;
    private final UsuarioDao usuarioDao;
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
            bitacoraService.registrar(null, username, accion, "OFRENDA", descripcion, clientIp);
        } catch (Exception e) {
            // Ignorar
        }
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            Usuario usuario = usuarioDao.findByUsername(authentication.getName()).orElse(null);
            if (usuario != null) {
                return usuario.getId();
            }
        }
        return null;
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN', 'TESORERO')")
    public ResponseEntity<ApiResponse<OfrendaDto>> create(@Valid @RequestBody OfrendaDto ofrendaDto) {
        Long usuarioId = getCurrentUserId();
        OfrendaDto saved = ofrendaService.create(ofrendaDto, usuarioId);
        registrarLog("CREAR", "Registró una ofrenda (" + saved.getTipoMovimiento() + ") por el monto de: " + saved.getMonto() + " - Concepto: " + saved.getConceptoDetalle());
        return new ResponseEntity<>(ApiResponse.<OfrendaDto>builder()
                .message("Registro de ofrenda creado exitosamente.")
                .datos(saved)
                .nombreModelo("Ofrenda")
                .build(), HttpStatus.CREATED);
    }

    @GetMapping("/findall")
    public ResponseEntity<ApiResponse<List<OfrendaDto>>> findAll() {
        List<OfrendaDto> ofrendas = ofrendaService.findAll();
        return ResponseEntity.ok(ApiResponse.<List<OfrendaDto>>builder()
                .message("Listado de ofrendas.")
                .datos(ofrendas)
                .nombreModelo("Ofrenda")
                .build());
    }

    @GetMapping("/showbyid/{id}")
    public ResponseEntity<ApiResponse<OfrendaDto>> showById(@PathVariable Long id) {
        OfrendaDto ofrenda = ofrendaService.findById(id);
        if (ofrenda == null) throw new NotFoundExceptionResource("Ofrenda", "id", id);
        return ResponseEntity.ok(ApiResponse.<OfrendaDto>builder()
                .message("Ofrenda encontrada.")
                .datos(ofrenda)
                .nombreModelo("Ofrenda")
                .build());
    }

    @PutMapping("/update")
    @PreAuthorize("hasAnyRole('ADMIN', 'TESORERO')")
    public ResponseEntity<ApiResponse<OfrendaDto>> update(@Valid @RequestBody OfrendaDto ofrendaDto) {
        OfrendaDto updated = ofrendaService.update(ofrendaDto);
        registrarLog("MODIFICAR", "Actualizó la ofrenda ID: " + updated.getId() + " (" + updated.getTipoMovimiento() + ") - Nuevo Monto: " + updated.getMonto());
        return ResponseEntity.ok(ApiResponse.<OfrendaDto>builder()
                .message("Ofrenda actualizada exitosamente.")
                .datos(updated)
                .nombreModelo("Ofrenda")
                .build());
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TESORERO')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        ofrendaService.delete(id);
        registrarLog("ELIMINAR", "Eliminó el registro de ofrenda ID: " + id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Ofrenda eliminada exitosamente.")
                .datos(null)
                .nombreModelo("Ofrenda")
                .build());
    }

    @GetMapping("/periodo")
    public ResponseEntity<ApiResponse<List<OfrendaDto>>> findByPeriod(
            @RequestParam("start") @DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
            @RequestParam("end") @DateTimeFormat(pattern = "yyyy-MM-dd") Date end) {
        List<OfrendaDto> ofrendas = ofrendaService.findByPeriod(start, end);
        return ResponseEntity.ok(ApiResponse.<List<OfrendaDto>>builder()
                .message("Ofrendas filtradas por período.")
                .datos(ofrendas)
                .nombreModelo("Ofrenda")
                .build());
    }

    @GetMapping("/resumen-periodo")
    public ResponseEntity<ApiResponse<Map<String, Double>>> getResumenPeriodo(
            @RequestParam("start") @DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
            @RequestParam("end") @DateTimeFormat(pattern = "yyyy-MM-dd") Date end,
            @RequestParam(value = "iglesiaId", required = false) Long iglesiaId) {
        
        Double totalIngresos;
        Double totalEgresos;

        if (iglesiaId != null) {
            totalIngresos = ofrendaService.getSumByIglesiaAndTipoAndPeriod(iglesiaId, "INGRESO", start, end);
            totalEgresos = ofrendaService.getSumByIglesiaAndTipoAndPeriod(iglesiaId, "EGRESO", start, end);
        } else {
            totalIngresos = ofrendaService.getSumByTipoAndPeriod("INGRESO", start, end);
            totalEgresos = ofrendaService.getSumByTipoAndPeriod("EGRESO", start, end);
        }

        Map<String, Double> resumen = new HashMap<>();
        resumen.put("ingresos", totalIngresos);
        resumen.put("egresos", totalEgresos);
        resumen.put("neto", totalIngresos - totalEgresos);

        return ResponseEntity.ok(ApiResponse.<Map<String, Double>>builder()
                .message("Resumen financiero obtenido con éxito.")
                .datos(resumen)
                .nombreModelo("OfrendaResumen")
                .build());
    }
}
