package com.mcmm.controller;

import com.mcmm.controller.support.ControllerTestSupport;
import com.mcmm.model.dto.usuarioDto.UsuarioChangePasswordDto;
import com.mcmm.model.dto.usuarioDto.UsuarioDto;
import com.mcmm.model.dto.usuarioDto.UsuarioDtoRes;
import com.mcmm.model.dto.usuarioDto.UsuarioResetPasswordDto;
import com.mcmm.service.IBitacora;
import com.mcmm.service.IUsuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UsuarioController.class)
class UsuarioControllerTest extends ControllerTestSupport {

    @MockBean
    private IUsuario usuarioService;

    @MockBean
    private IBitacora bitacoraService;

    private UsuarioDto dtoValido() {
        return UsuarioDto.builder().email("carlos@test.com").username("carlos").password("password123").build();
    }

    private UsuarioChangePasswordDto changePasswordDto(Long id, String actual, String nueva) {
        UsuarioChangePasswordDto dto = new UsuarioChangePasswordDto();
        dto.setId(id);
        dto.setCurrentPassword(actual);
        dto.setNewPassword(nueva);
        return dto;
    }

    private UsuarioResetPasswordDto resetPasswordDto(Long id, String nueva) {
        UsuarioResetPasswordDto dto = new UsuarioResetPasswordDto();
        dto.setId(id);
        dto.setNewPassword(nueva);
        return dto;
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("create: happy path responde 201")
    void create_admin_devuelve201() throws Exception {
        when(usuarioService.create(any())).thenReturn(UsuarioDtoRes.builder().username("carlos").build());

        mockMvc.perform(post("/api/usuario/v1/create").with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dtoValido())))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "TESORERO")
    @DisplayName("create: TESORERO entra a la clase pero no a create() (exige USUARIOS:EDITAR o ADMIN), rechaza con 403")
    void create_tesorero_rechazaCon403() throws Exception {
        mockMvc.perform(post("/api/usuario/v1/create").with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dtoValido())))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("create: email invalido (@Email) responde 400")
    void create_emailInvalido_devuelve400() throws Exception {
        UsuarioDto invalido = UsuarioDto.builder().email("no-es-un-email").username("carlos").password("password123").build();

        mockMvc.perform(post("/api/usuario/v1/create").with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message.email").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("create: password muy corto (@Size min=6) responde 400")
    void create_passwordMuyCorto_devuelve400() throws Exception {
        UsuarioDto invalido = UsuarioDto.builder().email("carlos@test.com").username("carlos").password("123").build();

        mockMvc.perform(post("/api/usuario/v1/create").with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message.password").exists());
    }

    @Test
    @WithMockUser(username = "carlos", roles = "DIACONO")
    @DisplayName("change-password: DIACONO NO esta en la lista de roles de la clase, pero isAuthenticated() a nivel de metodo igual lo deja pasar")
    void changePassword_conRolFueraDeLaClase_devuelve200() throws Exception {
        UsuarioChangePasswordDto dto = changePasswordDto(1L, "actual123", "nueva123");

        mockMvc.perform(put("/api/usuario/v1/change-password").with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        org.mockito.Mockito.verify(usuarioService).changePassword(any(), eq("carlos"));
    }

    @Test
    @WithMockUser(roles = "PASTOR")
    @DisplayName("reset-password: exige ROLE_ADMIN especificamente -- PASTOR (dentro de la clase) rechaza con 403")
    void resetPassword_pastor_rechazaCon403() throws Exception {
        UsuarioResetPasswordDto dto = resetPasswordDto(1L, "nueva123");

        mockMvc.perform(put("/api/usuario/v1/reset-password").with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("reset-password: newPassword en blanco (@NotBlank), responde 400")
    void resetPassword_passwordEnBlanco_devuelve400() throws Exception {
        UsuarioResetPasswordDto dto = resetPasswordDto(1L, "");

        mockMvc.perform(put("/api/usuario/v1/reset-password").with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message.newPassword").exists());
    }

    @Test
    @WithMockUser(username = "carlos", roles = "DIACONO")
    @DisplayName("findbyusername: DIACONO fuera de la clase, pero isAuthenticated() lo deja pasar; usa el username del contexto")
    void findByUsername_conRolFueraDeLaClase_usaElUsernameDelContexto() throws Exception {
        when(usuarioService.findByUsername("carlos")).thenReturn(UsuarioDtoRes.builder().username("carlos").build());

        mockMvc.perform(get("/api/usuario/v1/findbyusername"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.username").value("carlos"));
    }
}
