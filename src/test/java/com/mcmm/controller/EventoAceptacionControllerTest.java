package com.mcmm.controller;

import com.mcmm.controller.support.ControllerTestSupport;
import com.mcmm.model.dto.eventoAceptacion.EventoAceptacionDto;
import com.mcmm.service.IEventoAceptacion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * EventoAceptacionDto no tiene ninguna anotacion de validacion (@Valid en el
 * controlador no hace nada practico) — un body invalido solo puede probarse
 * via un error de deserializacion JSON (tipo incorrecto), no via bean
 * validation. Ningun endpoint tiene @PreAuthorize propio: los tres heredan el
 * de la clase.
 */
@WebMvcTest(controllers = EventoAceptacionController.class)
class EventoAceptacionControllerTest extends ControllerTestSupport {

    @MockBean
    private IEventoAceptacion eventoAceptacionService;

    @Test
    @WithMockUser(roles = "PASTOR")
    @DisplayName("decidir: PASTOR esta en la lista de roles de la clase, responde 200")
    void decidir_pastor_devuelve200() throws Exception {
        when(eventoAceptacionService.decidir(any()))
                .thenReturn(EventoAceptacionDto.builder().eventoId(1L).iglesiaId(2L).estado("ACEPTADO").build());

        mockMvc.perform(post("/api/evento-aceptacion/v1/decidir").with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                EventoAceptacionDto.builder().eventoId(1L).iglesiaId(2L).estado("ACEPTADO").build())))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "TESORERO")
    @DisplayName("decidir: TESORERO no esta en la lista de roles de la clase, rechaza con 403")
    void decidir_tesorero_rechazaCon403() throws Exception {
        mockMvc.perform(post("/api/evento-aceptacion/v1/decidir").with(csrf())
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("decidir: eventoId con tipo incorrecto en el JSON responde 400 (error de deserializacion, no de bean validation)")
    void decidir_tipoIncorrectoEnJson_devuelve400() throws Exception {
        mockMvc.perform(post("/api/evento-aceptacion/v1/decidir").with(csrf())
                        .contentType("application/json")
                        .content("{\"eventoId\": \"no-es-un-numero\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("findByIglesiaId: delega en el servicio con el iglesiaId del path")
    void findByIglesiaId_admin_delegaEnElServicio() throws Exception {
        when(eventoAceptacionService.findByIglesiaId(5L)).thenReturn(java.util.List.of());

        mockMvc.perform(get("/api/evento-aceptacion/v1/iglesia/5"))
                .andExpect(status().isOk());
    }
}
