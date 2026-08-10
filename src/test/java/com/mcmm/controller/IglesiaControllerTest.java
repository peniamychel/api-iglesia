package com.mcmm.controller;

import com.mcmm.controller.support.ControllerTestSupport;
import com.mcmm.model.dto.iglesiaDto.IglesiaDto;
import com.mcmm.service.IIglesia;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = IglesiaController.class)
class IglesiaControllerTest extends ControllerTestSupport {

    @MockBean
    private IIglesia iglesiaService;

    private IglesiaDto dtoValido() {
        return IglesiaDto.builder().nombre("Palmar").build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("create: nombre ya existente responde 400 (BadRequestException), no llega a llamar a save()")
    void create_nombreYaExiste_devuelve400() throws Exception {
        when(iglesiaService.buscarNombreIglesia("Palmar")).thenReturn(dtoValido());

        mockMvc.perform(post("/api/iglesia/v1/create").with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dtoValido())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El nombre de la iglesia ya existe."));

        verify(iglesiaService, org.mockito.Mockito.never()).save(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("create: nombre libre, guarda y responde 201")
    void create_nombreLibre_devuelve201() throws Exception {
        when(iglesiaService.buscarNombreIglesia("Palmar")).thenReturn(null);
        when(iglesiaService.save(any())).thenReturn(dtoValido());

        mockMvc.perform(post("/api/iglesia/v1/create").with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dtoValido())))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "TESORERO")
    @DisplayName("create: TESORERO entra a la clase (por IGLESIAS:VER/rol) pero create() exige IGLESIAS:CREAR o ADMIN, rechaza con 403")
    void create_tesorero_rechazaCon403() throws Exception {
        mockMvc.perform(post("/api/iglesia/v1/create").with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dtoValido())))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("create: nombre en blanco (@NotBlank), responde 400")
    void create_nombreEnBlanco_devuelve400() throws Exception {
        mockMvc.perform(post("/api/iglesia/v1/create").with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(IglesiaDto.builder().build())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message.nombre").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("update: usa el id que trae el body (ruta /update, sin path variable)")
    void update_usaElIdDelBody() throws Exception {
        IglesiaDto dto = IglesiaDto.builder().id(7L).nombre("Palmar").build();
        when(iglesiaService.update(eq(7L), any())).thenReturn(dto);

        mockMvc.perform(put("/api/iglesia/v1/update").with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(iglesiaService).update(eq(7L), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("update2: usa el id del path, no el del body (son dos endpoints distintos con el mismo nombre de metodo Java)")
    void update2_usaElIdDelPath() throws Exception {
        IglesiaDto bodyConIdDistinto = IglesiaDto.builder().id(999L).nombre("Palmar").build();
        when(iglesiaService.update(eq(5L), any())).thenReturn(bodyConIdDistinto);

        mockMvc.perform(put("/api/iglesia/v1/update2/5").with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(bodyConIdDistinto)))
                .andExpect(status().isOk());

        verify(iglesiaService).update(eq(5L), any());
        verify(iglesiaService, org.mockito.Mockito.never()).update(eq(999L), any());
    }

    @Test
    @WithMockUser(roles = "TESORERO")
    @DisplayName("findAll: TESORERO esta explicitamente permitido en findAll()")
    void findAll_tesorero_devuelve200() throws Exception {
        when(iglesiaService.findAll()).thenReturn(java.util.List.of());

        mockMvc.perform(get("/api/iglesia/v1/findall"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "MIEMBROS:VER")
    @DisplayName("showById: MIEMBROS:VER (autoridad de otro modulo) tambien habilita ver una iglesia puntual")
    void showById_conAutoridadMiembrosVer_devuelve200() throws Exception {
        when(iglesiaService.findById(1L)).thenReturn(dtoValido());

        mockMvc.perform(get("/api/iglesia/v1/showbyid/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "MIEMBROS:VER")
    @DisplayName("findAll: MIEMBROS:VER NO esta en la lista de findAll() (a diferencia de showById), rechaza con 403")
    void findAll_conSoloAutoridadMiembrosVer_rechazaCon403() throws Exception {
        mockMvc.perform(get("/api/iglesia/v1/findall"))
                .andExpect(status().isForbidden());
    }
}
