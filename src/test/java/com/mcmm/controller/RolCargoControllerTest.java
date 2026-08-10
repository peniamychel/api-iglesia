package com.mcmm.controller;

import com.mcmm.controller.support.ControllerTestSupport;
import com.mcmm.model.dto.RolCargoDto;
import com.mcmm.service.IRolCargo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RolCargoController.class)
class RolCargoControllerTest extends ControllerTestSupport {

    @MockBean
    private IRolCargo rolCargoService;

    @Test
    @WithMockUser(roles = "TESORERO")
    @DisplayName("findAll: TESORERO entra a la clase por rol, pero findAll() exige una autoridad especifica o ADMIN, rechaza con 403")
    void findAll_tesorero_rechazaCon403() throws Exception {
        mockMvc.perform(get("/api/rol-cargo/v1/findall"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "OBREROS:VER")
    @DisplayName("findAll: con la autoridad OBREROS:VER, sin ningun rol, responde 200")
    void findAll_conAutoridadObrerosVer_devuelve200() throws Exception {
        when(rolCargoService.findAll()).thenReturn(java.util.List.of());

        mockMvc.perform(get("/api/rol-cargo/v1/findall"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("update: el id del path sobreescribe el id del body antes de llamar al servicio")
    void update_admin_elIdDelPathSobreescribeElDelBody() throws Exception {
        when(rolCargoService.update(any())).thenReturn(RolCargoDto.builder().id(1L).nombre("Pastor").build());

        RolCargoDto bodyConIdDistinto = RolCargoDto.builder().id(999L).nombre("Pastor").build();
        mockMvc.perform(put("/api/rol-cargo/v1/update/1").with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(bodyConIdDistinto)))
                .andExpect(status().isOk());

        verify(rolCargoService).update(argThat(dto -> dto.getId().equals(1L)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("delete: responde 204 real (No Content)")
    void delete_admin_devuelve204() throws Exception {
        mockMvc.perform(delete("/api/rol-cargo/v1/delete/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = "OBREROS:DESVINCULAR")
    @DisplayName("delete: OBREROS:DESVINCULAR habilita el borrado, aunque no de USUARIOS:EDITAR ni OBREROS:DESIGNAR (create/update usan otra autoridad)")
    void delete_conAutoridadDesvincular_devuelve204() throws Exception {
        mockMvc.perform(delete("/api/rol-cargo/v1/delete/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = "OBREROS:DESVINCULAR")
    @DisplayName("create: OBREROS:DESVINCULAR NO habilita crear (usa OBREROS:DESIGNAR, no DESVINCULAR), rechaza con 403")
    void create_conAutoridadDesvincular_rechazaCon403() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/rol-cargo/v1/create").with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(RolCargoDto.builder().nombre("Pastor").build())))
                .andExpect(status().isForbidden());
    }
}
