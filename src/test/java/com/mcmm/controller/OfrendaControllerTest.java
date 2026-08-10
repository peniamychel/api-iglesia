package com.mcmm.controller;

import com.mcmm.controller.support.ControllerTestSupport;
import com.mcmm.model.dto.OfrendaDto;
import com.mcmm.service.IBitacora;
import com.mcmm.service.IOfrenda;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * create/update/delete usan hasAnyRole('TESORERO','PASTOR','ENCARGADO_IGLESIA')
 * SIN incluir ADMIN ni ninguna @hasAuthority — a diferencia de practicamente
 * todos los demas controladores del sistema, que siempre dejan pasar a
 * ADMIN via "OR hasRole('ADMIN')". Un usuario con SOLO ROLE_ADMIN queda
 * excluido de escribir ofrendas (si esto es intencional o un descuido, lo
 * decide el negocio; el test documenta el comportamiento real tal cual esta).
 */
@WebMvcTest(controllers = OfrendaController.class)
class OfrendaControllerTest extends ControllerTestSupport {

    @MockBean
    private IOfrenda ofrendaService;

    @MockBean
    private IBitacora bitacoraService;

    // usuarioDao ya viene mockeado desde ControllerTestSupport (lo necesita
    // GlobalExceptionHandler para levantar el contexto).

    private OfrendaDto dtoValido() {
        return OfrendaDto.builder().tipoMovimiento("INGRESO").monto(100.0).fechaRecaudacion(new Date()).build();
    }

    @Test
    @WithMockUser(roles = "TESORERO")
    @DisplayName("create: TESORERO puede crear")
    void create_tesorero_devuelve201() throws Exception {
        when(usuarioDao.findByUsername(any())).thenReturn(java.util.Optional.empty());
        when(ofrendaService.create(any(), any())).thenReturn(dtoValido());

        mockMvc.perform(post("/api/ofrenda/v1/create").with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dtoValido())))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("create: ADMIN, aunque entra a la clase, NO esta en la lista de create() -- rechaza con 403 (hallazgo: unico modulo asi)")
    void create_soloAdmin_rechazaCon403() throws Exception {
        mockMvc.perform(post("/api/ofrenda/v1/create").with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dtoValido())))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "TESORERO")
    @DisplayName("create: monto negativo viola @DecimalMin(0.01), responde 400")
    void create_montoNegativo_devuelve400() throws Exception {
        OfrendaDto invalido = OfrendaDto.builder().tipoMovimiento("INGRESO").monto(-5.0).fechaRecaudacion(new Date()).build();

        mockMvc.perform(post("/api/ofrenda/v1/create").with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message.monto").exists());
    }

    @Test
    @WithMockUser(roles = "TESORERO")
    @DisplayName("create: tipoMovimiento fuera de INGRESO|EGRESO viola @Pattern, responde 400")
    void create_tipoMovimientoInvalido_devuelve400() throws Exception {
        OfrendaDto invalido = OfrendaDto.builder().tipoMovimiento("TRANSFERENCIA").monto(100.0).fechaRecaudacion(new Date()).build();

        mockMvc.perform(post("/api/ofrenda/v1/create").with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message.tipoMovimiento").exists());
    }

    @Test
    @WithMockUser(roles = "TESORERO")
    @DisplayName("periodo: sin el parametro requerido 'end' responde 500, NO 400 (hallazgo real, afecta a Informes)")
    void periodo_faltaParametroRequerido_devuelve500() throws Exception {
        // Mismo hueco que EventoController./clonar: GlobalExceptionHandler no
        // maneja MissingServletRequestParameterException (solo cubre
        // MethodArgumentTypeMismatchException, para tipos incorrectos, no para
        // parametros ausentes), asi que cae en el catch-all de Exception y
        // responde "Error interno del servidor" en vez de un 400 claro.
        // Estos endpoints alimentan el modulo de Informes: si el frontend
        // alguna vez omite el rango de fechas, el usuario ve un error de
        // servidor en lugar de un mensaje util.
        mockMvc.perform(get("/api/ofrenda/v1/periodo").param("start", "2026-01-01"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = "TESORERO")
    @DisplayName("periodo: con ambas fechas bien formadas responde 200 (el 500 de arriba es por el parametro ausente, no por el endpoint)")
    void periodo_conAmbasFechas_devuelve200() throws Exception {
        when(ofrendaService.findByPeriod(any(), any())).thenReturn(java.util.List.of());

        mockMvc.perform(get("/api/ofrenda/v1/periodo")
                        .param("start", "2026-01-01").param("end", "2026-01-31"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "carlos", roles = "TESORERO")
    @DisplayName("create: resuelve el usuarioId del username autenticado via UsuarioDao y lo pasa al servicio")
    void create_resuelveUsuarioIdDelAutenticado() throws Exception {
        com.mcmm.model.entity.Usuario usuario = new com.mcmm.model.entity.Usuario();
        usuario.setId(42L);
        when(usuarioDao.findByUsername("carlos")).thenReturn(java.util.Optional.of(usuario));
        when(ofrendaService.create(any(), org.mockito.ArgumentMatchers.eq(42L))).thenReturn(dtoValido());

        mockMvc.perform(post("/api/ofrenda/v1/create").with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dtoValido())))
                .andExpect(status().isCreated());

        org.mockito.Mockito.verify(ofrendaService).create(any(), org.mockito.ArgumentMatchers.eq(42L));
    }
}
