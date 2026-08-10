package com.mcmm.controller;

import com.mcmm.controller.support.ControllerTestSupport;
import com.mcmm.model.dto.tipoEvento.TipoEventoDto;
import com.mcmm.service.ITipoEvento;
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

@WebMvcTest(controllers = TipoEventoController.class)
class TipoEventoControllerTest extends ControllerTestSupport {

    @MockBean
    private ITipoEvento tipoEventoService;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("create: ADMIN puede crear")
    void create_admin_devuelve201() throws Exception {
        when(tipoEventoService.create(any())).thenReturn(TipoEventoDto.builder().nombre("Retiro").build());

        mockMvc.perform(post("/api/tipo-evento/v1/create").with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(TipoEventoDto.builder().nombre("Retiro").build())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.datos.nombre").value("Retiro"));
    }

    @Test
    @WithMockUser(roles = "TESORERO")
    @DisplayName("create: TESORERO no esta habilitado ni por rol ni por autoridad, rechaza con 403")
    void create_tesorero_rechazaCon403() throws Exception {
        mockMvc.perform(post("/api/tipo-evento/v1/create").with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(TipoEventoDto.builder().nombre("Retiro").build())))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("create: body sin nombre (@NotBlank) responde 400")
    void create_sinNombre_devuelve400() throws Exception {
        mockMvc.perform(post("/api/tipo-evento/v1/create").with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(TipoEventoDto.builder().build())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message.nombre").exists());
    }

    @Test
    @WithMockUser(authorities = "EVENTOS:VER")
    @DisplayName("findAll: solo con la autoridad EVENTOS:VER, sin ningun rol, tambien puede")
    void findAll_conAutoridad_devuelve200() throws Exception {
        when(tipoEventoService.findAll()).thenReturn(java.util.List.of());

        mockMvc.perform(get("/api/tipo-evento/v1/findall"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("showById: servicio devuelve null (no encontrado), responde 404")
    void showById_servicioDevuelveNull_devuelve404() throws Exception {
        when(tipoEventoService.findById(404L)).thenReturn(null);

        mockMvc.perform(get("/api/tipo-evento/v1/showbyid/404"))
                .andExpect(status().isNotFound());
    }
}
