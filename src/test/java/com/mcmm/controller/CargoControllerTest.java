package com.mcmm.controller;

import com.mcmm.controller.support.ControllerTestSupport;
import com.mcmm.exception.NotFoundExceptionResource;
import com.mcmm.model.dto.CargoDto;
import com.mcmm.service.ICargo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Primer test @WebMvcTest del proyecto: sirve de patron de referencia para el
 * resto de controladores. Cubre la capa que los tests unitarios de servicio
 * NO alcanzan: @PreAuthorize (el metodo gana sobre la clase cuando ambos
 * existen — TESORERO entra por el nivel de clase pero create() lo exige a
 * nivel de metodo y lo rechaza), @Valid en el body, y el mapeo de excepciones
 * a codigo HTTP via GlobalExceptionHandler.
 */
@WebMvcTest(controllers = CargoController.class)
class CargoControllerTest extends ControllerTestSupport {

    @MockBean
    private ICargo cargoService;

    private CargoDto cargoDtoValido() {
        return CargoDto.builder().rolCargoId(1L).iglesiaId(1L).idMiembro(1L).build();
    }

    // ───────────────────────── autorizacion ─────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("create: ADMIN puede crear, responde 201 con el DTO guardado")
    void create_admin_devuelve201() throws Exception {
        when(cargoService.create(any(CargoDto.class))).thenReturn(cargoDtoValido());

        mockMvc.perform(post("/api/cargo/v1/create")
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(cargoDtoValido())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.datos.iglesiaId").value(1))
                .andExpect(jsonPath("$.nombreModelo").value("Cargo"));
    }

    @Test
    @WithMockUser(authorities = "OBREROS:DESIGNAR")
    @DisplayName("create: usuario sin rol pero con la autoridad OBREROS:DESIGNAR, tambien puede")
    void create_conAutoridadEspecifica_devuelve201() throws Exception {
        when(cargoService.create(any(CargoDto.class))).thenReturn(cargoDtoValido());

        mockMvc.perform(post("/api/cargo/v1/create")
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(cargoDtoValido())))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "TESORERO")
    @DisplayName("create: TESORERO entra a la clase (nivel controlador) pero create() lo exige a nivel de metodo, y lo rechaza con 403")
    void create_tesorero_rechazaCon403() throws Exception {
        mockMvc.perform(post("/api/cargo/v1/create")
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(cargoDtoValido())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(containsString("Acceso denegado")));
    }

    @Test
    @WithMockUser(roles = "TESORERO")
    @DisplayName("findAll: TESORERO no tiene rol ni autoridad en la lista de findAll, rechaza con 403")
    void findAll_tesorero_rechazaCon403() throws Exception {
        mockMvc.perform(get("/api/cargo/v1/findall"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PASTOR")
    @DisplayName("findAll: PASTOR si esta habilitado, responde 200")
    void findAll_pastor_devuelve200() throws Exception {
        when(cargoService.findAll()).thenReturn(java.util.List.of(cargoDtoValido()));

        mockMvc.perform(get("/api/cargo/v1/findall"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.length()").value(1));
    }

    // ───────────────────────── validacion ─────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("create: body sin los campos @NotNull responde 400 con el mapa de errores por campo")
    void create_bodyInvalido_devuelve400ConErroresPorCampo() throws Exception {
        CargoDto vacio = CargoDto.builder().build(); // sin rolCargoId/iglesiaId/idMiembro

        mockMvc.perform(post("/api/cargo/v1/create")
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(vacio)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message.rolCargoId").exists())
                .andExpect(jsonPath("$.message.iglesiaId").exists())
                .andExpect(jsonPath("$.message.idMiembro").exists());
    }

    // ───────────────────────── mapeo de excepciones ─────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("showById: id inexistente propaga NotFoundExceptionResource como 404")
    void showById_idInexistente_devuelve404() throws Exception {
        when(cargoService.findById(404L)).thenThrow(new NotFoundExceptionResource("Cargo", "id", 404L));

        mockMvc.perform(get("/api/cargo/v1/showbyid/404"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("delete: responde 200, no 204 — el status de ResponseEntity.ok() gana sobre @ResponseStatus(NO_CONTENT) del metodo")
    void delete_admin_devuelve200() throws Exception {
        mockMvc.perform(delete("/api/cargo/v1/delete/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Cargo eliminado exitosamente."));
    }
}
