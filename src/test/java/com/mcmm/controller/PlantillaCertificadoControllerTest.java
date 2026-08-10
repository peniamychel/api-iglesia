package com.mcmm.controller;

import com.mcmm.controller.support.ControllerTestSupport;
import com.mcmm.model.dto.plantillaCertificado.PlantillaCertificadoDto;
import com.mcmm.model.entity.PlantillaCertificado;
import com.mcmm.service.PlantillaCertificadoService;
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

/**
 * El controlador llama a modelMapper.map(...) directamente (no es un service
 * que ya devuelva DTOs). No hace falta mockearlo ni proveerlo aparte:
 * WebIglesiaApplication (la clase @SpringBootApplication) ya define un
 * @Bean ModelMapper, y @WebMvcTest lo registra igual que cualquier otro @Bean
 * de la clase de configuracion principal — el mapeo entidad->DTO funciona de
 * verdad, sin necesidad de un @TestConfiguration propio (que ademas chocaria
 * con ese bean: registrar dos beans "modelMapper" revienta el contexto).
 */
@WebMvcTest(controllers = PlantillaCertificadoController.class)
class PlantillaCertificadoControllerTest extends ControllerTestSupport {

    @MockBean
    private PlantillaCertificadoService plantillaCertificadoService;

    private PlantillaCertificado entidad(Long id, String nombre) {
        PlantillaCertificado p = new PlantillaCertificado();
        p.setId(id);
        p.setNombre(nombre);
        p.setEstado(true);
        return p;
    }

    @Test
    @WithMockUser(authorities = "CERTIFICADOS:VER")
    @DisplayName("findAll: mapea las entidades a DTO usando el ModelMapper real")
    void findAll_conAutoridad_mapeaEntidadesADto() throws Exception {
        when(plantillaCertificadoService.findAll()).thenReturn(java.util.List.of(entidad(1L, "Bautismo")));

        mockMvc.perform(get("/api/v1/plantilla-certificado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos[0].nombre").value("Bautismo"));
    }

    @Test
    @WithMockUser(roles = "PASTOR")
    @DisplayName("findAll: PASTOR entra a la clase pero no tiene CERTIFICADOS:VER ni ADMIN, rechaza con 403")
    void findAll_pastorSinAutoridad_rechazaCon403() throws Exception {
        mockMvc.perform(get("/api/v1/plantilla-certificado"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("create: nombre en blanco (@NotBlank), responde 400")
    void create_nombreEnBlanco_devuelve400() throws Exception {
        PlantillaCertificadoDto invalido = PlantillaCertificadoDto.builder().nombre(" ").build();

        mockMvc.perform(post("/api/v1/plantilla-certificado").with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message.nombre").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("create: happy path responde 201 con el DTO mapeado")
    void create_admin_devuelve201() throws Exception {
        when(plantillaCertificadoService.save(any())).thenReturn(entidad(1L, "Bautismo"));

        mockMvc.perform(post("/api/v1/plantilla-certificado").with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(PlantillaCertificadoDto.builder().nombre("Bautismo").build())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.datos.id").value(1))
                .andExpect(jsonPath("$.datos.nombre").value("Bautismo"));
    }
}
