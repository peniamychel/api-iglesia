package com.mcmm.controller;

import com.mcmm.controller.support.ControllerTestSupport;
import com.mcmm.model.dto.BitacoraDto;
import com.mcmm.service.IBitacora;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BitacoraController.class)
class BitacoraControllerTest extends ControllerTestSupport {

    @MockBean
    private IBitacora bitacoraService;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("findAll: ADMIN puede ver la bitacora completa")
    void findAll_admin_devuelve200() throws Exception {
        when(bitacoraService.findAll()).thenReturn(java.util.List.of(new BitacoraDto()));

        mockMvc.perform(get("/api/bitacora/v1/findall"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.length()").value(1));
    }

    @Test
    @WithMockUser(roles = "TESORERO")
    @DisplayName("findAll: TESORERO no esta en la lista de roles del controlador (solo ADMIN/PASTOR), rechaza con 403")
    void findAll_tesorero_rechazaCon403() throws Exception {
        mockMvc.perform(get("/api/bitacora/v1/findall"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PASTOR")
    @DisplayName("findByModulo: PASTOR si esta habilitado")
    void findByModulo_pastor_devuelve200() throws Exception {
        when(bitacoraService.findByModulo("MIEMBRO")).thenReturn(java.util.List.of());

        mockMvc.perform(get("/api/bitacora/v1/modulo/MIEMBRO"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("findByUser: delega en el servicio con el username del path")
    void findByUser_admin_delegaEnElServicio() throws Exception {
        when(bitacoraService.findByUser("carlos")).thenReturn(java.util.List.of());

        mockMvc.perform(get("/api/bitacora/v1/usuario/carlos"))
                .andExpect(status().isOk());
    }
}
