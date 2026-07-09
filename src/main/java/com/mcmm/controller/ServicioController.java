package com.mcmm.controller;

import com.mcmm.model.dto.AccionDto;
import com.mcmm.model.dto.RolCargoDto;
import com.mcmm.model.dto.ServicioDto;
import com.mcmm.service.IAccion;
import com.mcmm.service.IRolCargo;
import com.mcmm.service.IServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/servicios/v1")
@PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO', 'PASTOR')")
public class ServicioController {

    @Autowired
    private IServicio servicioService;

    @Autowired
    private IAccion accionService;

    @Autowired
    private IRolCargo rolCargoService;

    @GetMapping("/findall")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('USUARIOS:VER') OR hasRole('ADMIN')")
    public ResponseEntity<List<ServicioDto>> findAll() {
        List<ServicioDto> servicios = servicioService.findAll();
        return ResponseEntity.ok(servicios);
    }

    @GetMapping("/showbyid/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('USUARIOS:VER') OR hasRole('ADMIN')")
    public ResponseEntity<ServicioDto> findById(@PathVariable Long id) {
        ServicioDto servicio = servicioService.findById(id);
        return ResponseEntity.ok(servicio);
    }

    @GetMapping("/acciones/findall")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('USUARIOS:VER') OR hasRole('ADMIN')")
    public ResponseEntity<List<AccionDto>> findAllAcciones() {
        List<AccionDto> acciones = accionService.findAll();
        return ResponseEntity.ok(acciones);
    }

    @PostMapping("/rol-cargo/{rolCargoId}/add-accion/{accionId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('USUARIOS:EDITAR') OR hasRole('ADMIN')")
    public ResponseEntity<RolCargoDto> addAccionToRolCargo(
            @PathVariable Long rolCargoId,
            @PathVariable Long accionId) {
        RolCargoDto updated = rolCargoService.addAccion(rolCargoId, accionId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/rol-cargo/{rolCargoId}/remove-accion/{accionId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('USUARIOS:EDITAR') OR hasRole('ADMIN')")
    public ResponseEntity<RolCargoDto> removeAccionFromRolCargo(
            @PathVariable Long rolCargoId,
            @PathVariable Long accionId) {
        RolCargoDto updated = rolCargoService.removeAccion(rolCargoId, accionId);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/rol-cargo/{rolCargoId}/acciones")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('USUARIOS:VER') OR hasRole('ADMIN')")
    public ResponseEntity<Set<AccionDto>> getAccionesByRolCargo(
            @PathVariable Long rolCargoId) {
        RolCargoDto rolCargo = rolCargoService.findById(rolCargoId);
        return ResponseEntity.ok(rolCargo.getAcciones());
    }
}
