package com.mcmm.controller;

import com.mcmm.model.dto.PrivilegioDto;
import com.mcmm.model.dto.RolDto;
import com.mcmm.model.entity.ERole;
import com.mcmm.model.entity.Privilegio;
import com.mcmm.model.entity.Rol;
import com.mcmm.service.IPrivilegio;
import com.mcmm.service.IRol;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/privilegios/v1")
@PreAuthorize("hasRole('ADMIN')")
public class PrivilegioController {

    @Autowired
    private IPrivilegio privilegioService;

    @Autowired
    private IRol rolService;

    private ModelMapper modelMapper = new ModelMapper();

    @GetMapping("/findall")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<PrivilegioDto>> findAll() {
        List<PrivilegioDto> privilegios = (List<PrivilegioDto>) privilegioService.findAll();
        return ResponseEntity.ok(privilegios);
    }

    @GetMapping("/showbyid/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<PrivilegioDto> findById(@PathVariable Long id) {
        PrivilegioDto privilegio = privilegioService.findById(id);
        if (privilegio != null) {
            return ResponseEntity.ok(privilegio);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN') AND hasAuthority('Gestionar Privilegios')")
    public ResponseEntity<PrivilegioDto> create(@RequestBody PrivilegioDto privilegioDto) {
        PrivilegioDto savedPrivilegio = privilegioService.save(privilegioDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPrivilegio);
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN') AND hasAuthority('Gestionar Privilegios')")
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
    @PreAuthorize("hasRole('ADMIN') AND hasAuthority('Gestionar Privilegios')")
    public ResponseEntity<PrivilegioDto> update(@PathVariable Long id, @RequestBody PrivilegioDto privilegioDto) {
        PrivilegioDto updatedPrivilegio = privilegioService.update(id, privilegioDto);
        if (updatedPrivilegio != null) {
            return ResponseEntity.ok(updatedPrivilegio);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/rol/{rolName}/add/{privilegioId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN') AND hasAuthority('Gestionar Privilegios')")
    public ResponseEntity<RolDto> addPrivilegioToRol(
            @PathVariable String rolName,
            @PathVariable Long privilegioId) {
        ERole eRole = ERole.valueOf(rolName.toUpperCase());
        Rol updated = rolService.addPrivilegio(eRole, privilegioId);
        RolDto dto = modelMapper.map(updated, RolDto.class);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/rol/{rolName}/remove/{privilegioId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN') AND hasAuthority('Gestionar Privilegios')")
    public ResponseEntity<RolDto> removePrivilegioFromRol(
            @PathVariable String rolName,
            @PathVariable Long privilegioId) {
        ERole eRole = ERole.valueOf(rolName.toUpperCase());
        Rol updated = rolService.removePrivilegio(eRole, privilegioId);
        RolDto dto = modelMapper.map(updated, RolDto.class);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/rol/{rolName}/privilegios")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Set<PrivilegioDto>> getPrivilegiosByRol(
            @PathVariable String rolName) {
        ERole eRole = ERole.valueOf(rolName.toUpperCase());
        Set<Privilegio> privilegios = rolService.getPrivilegiosByRol(eRole);
        Set<PrivilegioDto> dtos = privilegios.stream()
                .map(p -> modelMapper.map(p, PrivilegioDto.class))
                .collect(Collectors.toSet());
        return ResponseEntity.ok(dtos);
    }
}
