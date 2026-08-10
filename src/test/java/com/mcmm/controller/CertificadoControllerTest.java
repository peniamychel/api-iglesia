package com.mcmm.controller;

import com.mcmm.controller.support.ControllerTestSupport;
import com.mcmm.model.dto.certificado.CertificadoDto;
import com.mcmm.service.IBitacora;
import com.mcmm.service.ICertificado;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CertificadoController.class)
class CertificadoControllerTest extends ControllerTestSupport {

    @MockBean
    private ICertificado certificadoService;

    @MockBean
    private IBitacora bitacoraService;

    private CertificadoDto dtoValido() {
        return CertificadoDto.builder().eventoId(1L).plantillaCertificadoId(1L).motivoCertificado("Bautismo").build();
    }

    @Test
    @WithMockUser(authorities = "CERTIFICADOS:GENERAR")
    @DisplayName("create: con la autoridad CERTIFICADOS:GENERAR responde 201")
    void create_conAutoridad_devuelve201() throws Exception {
        when(certificadoService.create(any())).thenReturn(dtoValido());

        mockMvc.perform(post("/api/certificado/v1/create").with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dtoValido())))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "PASTOR")
    @DisplayName("create: PASTOR entra a la clase pero no a create() sin la autoridad, rechaza con 403")
    void create_pastorSinAutoridad_rechazaCon403() throws Exception {
        mockMvc.perform(post("/api/certificado/v1/create").with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dtoValido())))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("create: motivoCertificado en blanco (@NotBlank), responde 400")
    void create_motivoEnBlanco_devuelve400() throws Exception {
        CertificadoDto invalido = CertificadoDto.builder().eventoId(1L).plantillaCertificadoId(1L).motivoCertificado(" ").build();

        mockMvc.perform(post("/api/certificado/v1/create").with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message.motivoCertificado").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("uploadProfilePhoto: multipart con @RequestPart y consumes explicito, responde con la URL")
    void uploadProfilePhoto_admin_devuelveUrl() throws Exception {
        when(certificadoService.uploadProfilePhoto(eq(1L), any())).thenReturn("/uploads/certificados/foto.jpg");
        MockMultipartFile file = new MockMultipartFile("file", "foto.jpg", "image/jpeg", "contenido".getBytes());

        mockMvc.perform(multipart("/api/certificado/v1/1/foto").file(file).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos").value("/uploads/certificados/foto.jpg"));
    }
}
