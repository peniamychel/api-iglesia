package com.mcmm.controller;

import com.mcmm.controller.support.ControllerTestSupport;
import com.mcmm.model.dto.ActivoDto;
import com.mcmm.service.IActivo;
import com.mcmm.service.IBitacora;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Todos los endpoints heredan el @PreAuthorize de la clase, ninguno tiene el propio. */
@WebMvcTest(controllers = ActivoController.class)
class ActivoControllerTest extends ControllerTestSupport {

    @MockBean
    private IActivo activoService;

    @MockBean
    private IBitacora bitacoraService;

    private ActivoDto dtoValido() {
        return ActivoDto.builder().nombre("Proyector").cantidad(1).iglesiaId(1L).build();
    }

    @Test
    @WithMockUser(roles = "DIACONO")
    @DisplayName("create: DIACONO esta habilitado por la clase, responde 201")
    void create_diacono_devuelve201() throws Exception {
        when(activoService.save(any())).thenReturn(dtoValido());

        mockMvc.perform(post("/api/activo/v1/create").with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dtoValido())))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "TESORERO")
    @DisplayName("create: TESORERO no esta en la lista de la clase, rechaza con 403")
    void create_tesorero_rechazaCon403() throws Exception {
        mockMvc.perform(post("/api/activo/v1/create").with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dtoValido())))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("create: cantidad=0 viola @Min(1), responde 400")
    void create_cantidadCero_devuelve400() throws Exception {
        ActivoDto invalido = ActivoDto.builder().nombre("Proyector").cantidad(0).iglesiaId(1L).build();

        mockMvc.perform(post("/api/activo/v1/create").with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message.cantidad").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("create: nombre en blanco viola @NotBlank, responde 400")
    void create_nombreEnBlanco_devuelve400() throws Exception {
        ActivoDto invalido = ActivoDto.builder().nombre(" ").cantidad(1).iglesiaId(1L).build();

        mockMvc.perform(post("/api/activo/v1/create").with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message.nombre").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("update: registra la accion en bitacora tras actualizar")
    void update_admin_registraEnBitacora() throws Exception {
        ActivoDto actualizado = dtoValido();
        actualizado.setId(1L);
        when(activoService.update(any())).thenReturn(actualizado);

        mockMvc.perform(put("/api/activo/v1/update").with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(actualizado)))
                .andExpect(status().isOk());

        org.mockito.Mockito.verify(bitacoraService).registrarAccion(
                org.mockito.ArgumentMatchers.eq("ACTIVO"), org.mockito.ArgumentMatchers.eq("MODIFICAR"), anyString());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("uploadPhoto: multipart, responde con la URL del archivo")
    void uploadPhoto_admin_devuelveUrl() throws Exception {
        when(activoService.uploadPhoto(org.mockito.ArgumentMatchers.eq(1L), any())).thenReturn("/uploads/activos/foto.jpg");
        MockMultipartFile file = new MockMultipartFile("file", "foto.jpg", "image/jpeg", "contenido".getBytes());

        mockMvc.perform(multipart("/api/activo/v1/1/foto").file(file).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos").value("/uploads/activos/foto.jpg"));
    }
}
