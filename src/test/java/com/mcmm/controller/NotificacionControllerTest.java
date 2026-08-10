package com.mcmm.controller;

import com.mcmm.controller.support.ControllerTestSupport;
import com.mcmm.model.dto.NotificacionBadgeDto;
import com.mcmm.service.INotificacion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = NotificacionController.class)
class NotificacionControllerTest extends ControllerTestSupport {

    @MockBean
    private INotificacion notificacionService;

    @Test
    @WithMockUser
    @DisplayName("badge: cualquier usuario autenticado, sin importar el rol, puede consultarlo (isAuthenticated())")
    void badge_cualquierUsuarioAutenticado_devuelve200() throws Exception {
        when(notificacionService.getBadge(any())).thenReturn(new NotificacionBadgeDto());

        mockMvc.perform(get("/api/notificaciones/v1/badge"))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("badge: sin autenticar, isAuthenticated() lo rechaza con 401 (no es un 403 de rol insuficiente, es falta de autenticacion)")
    void badge_anonimo_rechazaCon401() throws Exception {
        mockMvc.perform(get("/api/notificaciones/v1/badge"))
                .andExpect(status().isUnauthorized());
    }
}
