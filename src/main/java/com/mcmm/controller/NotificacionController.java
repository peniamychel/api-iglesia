package com.mcmm.controller;

import com.mcmm.model.dto.NotificacionBadgeDto;
import com.mcmm.model.payload.ApiResponse;
import com.mcmm.service.INotificacion;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notificaciones/v1")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class NotificacionController {

    private final INotificacion notificacionService;

    @GetMapping("/badge")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApiResponse<NotificacionBadgeDto>> badge(
            @RequestParam(required = false) Long iglesiaId) {
        NotificacionBadgeDto badge = notificacionService.getBadge(iglesiaId);
        return ResponseEntity.ok(
                ApiResponse.<NotificacionBadgeDto>builder()
                        .message("Conteo de notificaciones pendientes")
                        .datos(badge)
                        .nombreModelo("Notificacion")
                        .build()
        );
    }
}
