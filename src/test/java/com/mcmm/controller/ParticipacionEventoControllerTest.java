package com.mcmm.controller;

import com.mcmm.controller.support.ControllerTestSupport;
import com.mcmm.model.dto.participacionEvento.ParticipacionEventoDto;
import com.mcmm.model.dto.participacionEvento.RegistroEntregaDto;
import com.mcmm.service.IParticipacionEvento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ParticipacionEventoController.class)
class ParticipacionEventoControllerTest extends ControllerTestSupport {

    @MockBean
    private IParticipacionEvento participacionEventoService;

    private ParticipacionEventoDto dtoValido() {
        return ParticipacionEventoDto.builder().miembroId(1L).eventoId(1L).build();
    }

    @Test
    @WithMockUser(authorities = "EVENTOS:CREAR")
    @DisplayName("create: con la autoridad EVENTOS:CREAR responde 201")
    void create_conAutoridad_devuelve201() throws Exception {
        when(participacionEventoService.create(any())).thenReturn(dtoValido());

        mockMvc.perform(post("/api/participacion-evento/v1/create").with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dtoValido())))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("create: sin miembroId/eventoId (@NotNull), responde 400")
    void create_bodyInvalido_devuelve400() throws Exception {
        mockMvc.perform(post("/api/participacion-evento/v1/create").with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(ParticipacionEventoDto.builder().build())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message.miembroId").exists())
                .andExpect(jsonPath("$.message.eventoId").exists());
    }

    @Test
    @WithMockUser(username = "carlos.pastor", roles = "ADMIN")
    @DisplayName("toggleEntregado: pasa el username del usuario autenticado al servicio")
    void toggleEntregado_admin_pasaElUsernameAutenticado() throws Exception {
        mockMvc.perform(put("/api/participacion-evento/v1/entregado/1").with(csrf()))
                .andExpect(status().isOk());

        verify(participacionEventoService).toggleEntregado(eq(1L), eq("carlos.pastor"));
    }

    @Test
    @WithMockUser(username = "carlos.pastor", roles = "ADMIN")
    @DisplayName("entregar: valida el body y pasa el username autenticado al servicio")
    void registrarEntrega_admin_pasaElUsernameAutenticado() throws Exception {
        RegistroEntregaDto registro = RegistroEntregaDto.builder()
                .certificadoId(5L).numeroLibro("1").numeroFolio("10").build();
        when(participacionEventoService.registrarEntrega(eq(1L), any(), eq("carlos.pastor")))
                .thenReturn(dtoValido());

        mockMvc.perform(put("/api/participacion-evento/v1/entregar/1").with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registro)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("entregar: sin numeroLibro/numeroFolio (@NotBlank), responde 400")
    void registrarEntrega_bodyInvalido_devuelve400() throws Exception {
        RegistroEntregaDto registro = RegistroEntregaDto.builder().certificadoId(5L).build();

        mockMvc.perform(put("/api/participacion-evento/v1/entregar/1").with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registro)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message.numeroLibro").exists())
                .andExpect(jsonPath("$.message.numeroFolio").exists());
    }

    @Test
    @WithMockUser(roles = "PASTOR")
    @DisplayName("delete: PASTOR entra a la clase pero no a delete(), rechaza con 403")
    void delete_pastor_rechazaCon403() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/participacion-evento/v1/delete/1").with(csrf()))
                .andExpect(status().isForbidden());
    }
}
