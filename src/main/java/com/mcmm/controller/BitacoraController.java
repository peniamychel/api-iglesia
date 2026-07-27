package com.mcmm.controller;

import com.mcmm.model.dto.BitacoraDto;
import com.mcmm.model.payload.ApiResponse;
import com.mcmm.service.IBitacora;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bitacora/v1")
@PreAuthorize("hasAnyRole('ADMIN', 'PASTOR')")
@RequiredArgsConstructor
public class BitacoraController {

    private final IBitacora bitacoraService;

    @GetMapping("/findall")
    public ResponseEntity<ApiResponse<List<BitacoraDto>>> findAll() {
        List<BitacoraDto> bitacoras = bitacoraService.findAll();
        return ResponseEntity.ok(ApiResponse.<List<BitacoraDto>>builder()
                .message("Listado general de bitácora de auditoría.")
                .datos(bitacoras)
                .nombreModelo("Bitacora")
                .build());
    }

    @GetMapping("/modulo/{modulo}")
    public ResponseEntity<ApiResponse<List<BitacoraDto>>> findByModulo(@PathVariable String modulo) {
        List<BitacoraDto> bitacoras = bitacoraService.findByModulo(modulo);
        return ResponseEntity.ok(ApiResponse.<List<BitacoraDto>>builder()
                .message("Bitácora filtrada por módulo.")
                .datos(bitacoras)
                .nombreModelo("Bitacora")
                .build());
    }

    @GetMapping("/usuario/{username}")
    public ResponseEntity<ApiResponse<List<BitacoraDto>>> findByUser(@PathVariable String username) {
        List<BitacoraDto> bitacoras = bitacoraService.findByUser(username);
        return ResponseEntity.ok(ApiResponse.<List<BitacoraDto>>builder()
                .message("Bitácora filtrada por usuario.")
                .datos(bitacoras)
                .nombreModelo("Bitacora")
                .build());
    }
}
