package com.mcmm.controller;

import com.mcmm.model.dto.PrivilegioDto;
import com.mcmm.model.dto.RolCargoDto;
import com.mcmm.model.entity.Privilegio;
import com.mcmm.service.IPrivilegio;
import com.mcmm.service.IRolCargo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/privilegios/v1")
@PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO', 'PASTOR')")
public class PrivilegioController {

    @Autowired
    private IPrivilegio privilegioService;

    @Autowired
    private IRolCargo rolCargoService;

    @GetMapping("/findall")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('Ver Privilegios')")
    public ResponseEntity<List<PrivilegioDto>> findAll() {
        List<PrivilegioDto> privilegios = (List<PrivilegioDto>) privilegioService.findAll();
        return ResponseEntity.ok(privilegios);
    }

    @GetMapping("/showbyid/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('Ver Privilegios')")
    public ResponseEntity<PrivilegioDto> findById(@PathVariable Long id) {
        PrivilegioDto privilegio = privilegioService.findById(id);
        if (privilegio != null) {
            return ResponseEntity.ok(privilegio);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('Escribir Privilegios')")
    public ResponseEntity<PrivilegioDto> create(@RequestBody PrivilegioDto privilegioDto) {
        PrivilegioDto savedPrivilegio = privilegioService.save(privilegioDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPrivilegio);
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('Escribir Privilegios')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        PrivilegioDto privilegio = privilegioService.findById(id);
        if (privilegio != null) {
            privilegioService.delete(privilegio);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/update/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('Escribir Privilegios')")
    public ResponseEntity<PrivilegioDto> update(@PathVariable Long id, @RequestBody PrivilegioDto privilegioDto) {
        PrivilegioDto updatedPrivilegio = privilegioService.update(id, privilegioDto);
        if (updatedPrivilegio != null) {
            return ResponseEntity.ok(updatedPrivilegio);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/rol-cargo/{rolCargoId}/add/{privilegioId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('Escribir Privilegios')")
    public ResponseEntity<RolCargoDto> addPrivilegioToRolCargo(
            @PathVariable Long rolCargoId,
            @PathVariable Long privilegioId) {
        RolCargoDto updated = rolCargoService.addPrivilegio(rolCargoId, privilegioId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/rol-cargo/{rolCargoId}/remove/{privilegioId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('Escribir Privilegios')")
    public ResponseEntity<RolCargoDto> removePrivilegioFromRolCargo(
            @PathVariable Long rolCargoId,
            @PathVariable Long privilegioId) {
        RolCargoDto updated = rolCargoService.removePrivilegio(rolCargoId, privilegioId);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/rol-cargo/{rolCargoId}/privilegios")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('Ver Privilegios')")
    public ResponseEntity<Set<PrivilegioDto>> getPrivilegiosByRolCargo(
            @PathVariable Long rolCargoId) {
        RolCargoDto rolCargo = rolCargoService.findById(rolCargoId);
        return ResponseEntity.ok(rolCargo.getPrivilegios());
    }
}
