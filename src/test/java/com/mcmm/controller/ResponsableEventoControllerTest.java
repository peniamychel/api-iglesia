package com.mcmm.controller;

import com.mcmm.controller.support.ControllerTestSupport;
import com.mcmm.model.dto.responsableEvento.ResponsableEventoDto;
import com.mcmm.service.IResponsableEvento;
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

@WebMvcTest(controllers = ResponsableEventoController.class)
class ResponsableEventoControllerTest extends ControllerTestSupport {

    @MockBean
    private IResponsableEvento responsableEventoService;

    private ResponsableEventoDto dtoValido() {
        return ResponsableEventoDto.builder().eventoId(1L).cargoId(1L).build();
    }

    @Test
    @WithMockUser(authorities = "EVENTOS:CREAR")
    @DisplayName("create: con la autoridad EVENTOS:CREAR, sin rol, responde 201")
    void create_conAutoridad_devuelve201() throws Exception {
        when(responsableEventoService.create(any())).thenReturn(dtoValido());

        mockMvc.perform(post("/api/responsable-evento/v1/create").with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dtoValido())))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "PASTOR")
    @DisplayName("create: PASTOR entra a la clase pero no tiene EVENTOS:CREAR ni ADMIN, rechaza con 403")
    void create_pastorSinAutoridad_rechazaCon403() throws Exception {
        mockMvc.perform(post("/api/responsable-evento/v1/create").with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dtoValido())))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("create: body sin eventoId/cargoId (@NotNull) responde 400")
    void create_bodyInvalido_devuelve400() throws Exception {
        mockMvc.perform(post("/api/responsable-evento/v1/create").with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(ResponsableEventoDto.builder().build())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message.eventoId").exists())
                .andExpect(jsonPath("$.message.cargoId").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("showById: id inexistente propaga NotFoundExceptionResource como 404")
    void showById_idInexistente_devuelve404() throws Exception {
        when(responsableEventoService.findById(404L)).thenReturn(null);

        mockMvc.perform(get("/api/responsable-evento/v1/showbyid/404"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("findByEventoId: delega en el servicio con el eventoId del path")
    void findByEventoId_admin_delegaEnElServicio() throws Exception {
        when(responsableEventoService.findByEventoId(5L)).thenReturn(java.util.List.of(dtoValido()));

        mockMvc.perform(get("/api/responsable-evento/v1/evento/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.length()").value(1));
    }
}
