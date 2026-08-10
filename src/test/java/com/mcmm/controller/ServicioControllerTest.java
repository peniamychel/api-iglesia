package com.mcmm.controller;

import com.mcmm.controller.support.ControllerTestSupport;
import com.mcmm.model.dto.RolCargoDto;
import com.mcmm.model.dto.ServicioDto;
import com.mcmm.service.IAccion;
import com.mcmm.service.IRolCargo;
import com.mcmm.service.IServicio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unico controlador (aparte de Auth) que nunca envuelve sus respuestas en
 * ApiResponse: el JSON es el DTO/coleccion crudo, asi que las aserciones
 * apuntan a "$" o "$[0]" directo, no a "$.datos".
 */
@WebMvcTest(controllers = ServicioController.class)
class ServicioControllerTest extends ControllerTestSupport {

    @MockBean
    private IServicio servicioService;

    @MockBean
    private IAccion accionService;

    @MockBean
    private IRolCargo rolCargoService;

    @Test
    @WithMockUser(authorities = "USUARIOS:VER")
    @DisplayName("findAll: respuesta JSON cruda (lista de ServicioDto), sin envoltorio ApiResponse")
    void findAll_conAutoridad_devuelveListaCruda() throws Exception {
        when(servicioService.findAll()).thenReturn(java.util.List.of(ServicioDto.builder().codigo("MIEMBROS").build()));

        mockMvc.perform(get("/api/servicios/v1/findall"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("MIEMBROS"));
    }

    @Test
    @WithMockUser(roles = "PASTOR")
    @DisplayName("findAll: PASTOR entra a la clase pero no a findAll() (exige USUARIOS:VER o ADMIN), rechaza con 403")
    void findAll_pastor_rechazaCon403() throws Exception {
        mockMvc.perform(get("/api/servicios/v1/findall"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("addAccionToRolCargo: sin body, respuesta cruda del RolCargoDto actualizado")
    void addAccionToRolCargo_admin_devuelveRolCargoCrudo() throws Exception {
        when(rolCargoService.addAccion(1L, 2L)).thenReturn(RolCargoDto.builder().id(1L).nombre("Pastor").build());

        mockMvc.perform(post("/api/servicios/v1/rol-cargo/1/add-accion/2").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Pastor"));
    }
}
