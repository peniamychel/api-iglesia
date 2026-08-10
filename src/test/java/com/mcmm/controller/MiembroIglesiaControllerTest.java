package com.mcmm.controller;

import com.mcmm.controller.support.ControllerTestSupport;
import com.mcmm.model.dto.MiembroDto.MiembroDto;
import com.mcmm.model.dto.MiembroIglesiaDto;
import com.mcmm.service.IBitacora;
import com.mcmm.service.IMiembroIglesia;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** /mis-miembros lee el Authentication real, igual que MiembroController.importar. */
@WebMvcTest(controllers = MiembroIglesiaController.class)
class MiembroIglesiaControllerTest extends ControllerTestSupport {

    @MockBean
    private IMiembroIglesia miembroIglesiaService;

    @MockBean
    private IBitacora bitacoraService;

    private MiembroIglesiaDto dtoValido() {
        return MiembroIglesiaDto.builder().miembroId(1L).iglesiaId(1L).build();
    }

    private UsernamePasswordAuthenticationToken usuarioDeIglesia(Long iglesiaId) {
        var auth = new UsernamePasswordAuthenticationToken("carlos", null,
                List.of(new SimpleGrantedAuthority("ROLE_ENCARGADO_IGLESIA"), new SimpleGrantedAuthority("MIEMBROS:VER")));
        if (iglesiaId != null) auth.setDetails(Map.of("iglesiaId", iglesiaId));
        return auth;
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("created: miembro que ya pertenece a una iglesia, responde 400 (BadRequestException) sin guardar")
    void created_miembroYaEnIglesia_devuelve400() throws Exception {
        when(miembroIglesiaService.findByIdMiembro(1L)).thenReturn(false);

        mockMvc.perform(post("/api/miembroiglesia/v1/created").with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dtoValido())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El miembro ya pertenece a una iglesia."));

        verify(miembroIglesiaService, never()).save(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("created: miembro libre, guarda y responde 201")
    void created_miembroLibre_devuelve201() throws Exception {
        when(miembroIglesiaService.findByIdMiembro(1L)).thenReturn(true);
        when(miembroIglesiaService.save(any())).thenReturn(dtoValido());

        mockMvc.perform(post("/api/miembroiglesia/v1/created").with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dtoValido())))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("create: sin miembroId/iglesiaId (@NotNull), responde 400")
    void create_bodyInvalido_devuelve400() throws Exception {
        mockMvc.perform(post("/api/miembroiglesia/v1/create").with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(MiembroIglesiaDto.builder().build())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message.miembroId").exists())
                .andExpect(jsonPath("$.message.iglesiaId").exists());
    }

    @Test
    @DisplayName("mis-miembros: con iglesiaId en el token, delega en findMiembrosIglesia con esa iglesia")
    void misMiembros_conIglesiaIdEnElToken_delegaEnElServicio() throws Exception {
        when(miembroIglesiaService.findMiembrosIglesia(7L)).thenReturn(List.of(new MiembroDto()));

        mockMvc.perform(get("/api/miembroiglesia/v1/mis-miembros").with(authentication(usuarioDeIglesia(7L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.length()").value(1));

        verify(miembroIglesiaService).findMiembrosIglesia(eq(7L));
    }

    @Test
    @DisplayName("mis-miembros: sin details en el Authentication, responde 400 (BadRequestException)")
    void misMiembros_sinDetails_devuelve400() throws Exception {
        mockMvc.perform(get("/api/miembroiglesia/v1/mis-miembros").with(authentication(usuarioDeIglesia(null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("No se encontraron detalles de autenticación."));
    }

    @Test
    @WithMockUser(authorities = "MIEMBROS:VER")
    @DisplayName("mis-miembros: con @WithMockUser (Authentication no es UsernamePasswordAuthenticationToken con details), responde 400")
    void misMiembros_conWithMockUserPlano_devuelve400() throws Exception {
        // @WithMockUser SI produce un UsernamePasswordAuthenticationToken, pero sin
        // getDetails() poblado -- confirma que el 400 real es "sin detalles", no
        // "tipo de Authentication incorrecto".
        mockMvc.perform(get("/api/miembroiglesia/v1/mis-miembros"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("No se encontraron detalles de autenticación."));
    }

    @Test
    @WithMockUser(roles = "TESORERO")
    @DisplayName("create: TESORERO entra a la clase pero no a create() (exige MIEMBROS:EDITAR o ADMIN), rechaza con 403")
    void create_tesorero_rechazaCon403() throws Exception {
        mockMvc.perform(post("/api/miembroiglesia/v1/create").with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dtoValido())))
                .andExpect(status().isForbidden());
    }
}
