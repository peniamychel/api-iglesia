package com.mcmm.controller;

import com.mcmm.controller.support.ControllerTestSupport;
import com.mcmm.model.dto.evento.EventoDto;
import com.mcmm.service.IBitacora;
import com.mcmm.service.IEvento;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EventoController.class)
class EventoControllerTest extends ControllerTestSupport {

    @MockBean
    private IEvento eventoService;

    @MockBean
    private IBitacora bitacoraService;

    private EventoDto dtoValido() {
        return EventoDto.builder().tipoEventoId(1L).iglesiaId(1L).nombre("Retiro")
                .localidad("Cochabamba").provincia("Cercado").departamento("Cochabamba").build();
    }

    @Test
    @WithMockUser(authorities = "EVENTOS:CREAR")
    @DisplayName("create: con la autoridad EVENTOS:CREAR responde 201")
    void create_conAutoridad_devuelve201() throws Exception {
        when(eventoService.create(any())).thenReturn(dtoValido());

        mockMvc.perform(post("/api/evento/v1/create").with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dtoValido())))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ENCARGADO_IGLESIA")
    @DisplayName("create: ENCARGADO_IGLESIA entra a la clase pero no a create(), rechaza con 403")
    void create_encargadoIglesia_rechazaCon403() throws Exception {
        mockMvc.perform(post("/api/evento/v1/create").with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dtoValido())))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("create: sin localidad/provincia/departamento (@NotBlank), responde 400 con los tres campos")
    void create_sinDatosDeUbicacion_devuelve400() throws Exception {
        EventoDto invalido = EventoDto.builder().tipoEventoId(1L).iglesiaId(1L).nombre("Retiro").build();

        mockMvc.perform(post("/api/evento/v1/create").with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message.localidad").exists())
                .andExpect(jsonPath("$.message.provincia").exists())
                .andExpect(jsonPath("$.message.departamento").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("showById: servicio devuelve null, responde 404")
    void showById_servicioDevuelveNull_devuelve404() throws Exception {
        when(eventoService.findById(404L)).thenReturn(null);

        mockMvc.perform(get("/api/evento/v1/showbyid/404"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "PASTOR")
    @DisplayName("clonar: solo ADMIN puede, PASTOR (aunque entra a la clase) rechaza con 403")
    void clonar_pastor_rechazaCon403() throws Exception {
        mockMvc.perform(post("/api/evento/v1/clonar?from=2025&to=2026").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("clonar: falta el parametro requerido 'to' -- responde 500, NO 400 (hallazgo real)")
    void clonar_faltaParametroRequerido_devuelve500() throws Exception {
        // GlobalExceptionHandler no tiene un @ExceptionHandler especifico para
        // MissingServletRequestParameterException (solo cubre
        // MethodArgumentTypeMismatchException, para params de tipo incorrecto, no
        // ausentes). Sin ese handler dedicado, cae en el catch-all de Exception y
        // responde 500 "Error interno del servidor" en vez de un 400 claro por un
        // parametro de query faltante -- confunde a quien consume la API.
        mockMvc.perform(post("/api/evento/v1/clonar?from=2025").with(csrf()))
                .andExpect(status().isInternalServerError());
    }
}
