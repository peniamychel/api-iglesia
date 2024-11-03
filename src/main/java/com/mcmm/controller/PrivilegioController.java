package com.mcmm.controller;

import com.mcmm.model.dto.PrivilegioDto;
import com.mcmm.service.IPrivilegio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/privilegios/v1")
@PreAuthorize("hasRole('ADMIN')")
public class PrivilegioController {

    @Autowired
    private IPrivilegio privilegioService;

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
    public ResponseEntity<PrivilegioDto> create(@RequestBody PrivilegioDto privilegioDto) {
        PrivilegioDto savedPrivilegio = privilegioService.save(privilegioDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPrivilegio);
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
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
    public ResponseEntity<PrivilegioDto> update(@PathVariable Long id, @RequestBody PrivilegioDto privilegioDto) {
        PrivilegioDto updatedPrivilegio = privilegioService.update(id, privilegioDto);
        if (updatedPrivilegio != null) {
            return ResponseEntity.ok(updatedPrivilegio);
        }
        return ResponseEntity.notFound().build();
    }
}
