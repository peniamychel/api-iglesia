package com.mcmm.controller;

import com.mcmm.controller.support.ControllerTestSupport;
import com.mcmm.model.dto.MiembroDto.MiembroDto;
import com.mcmm.model.dto.MiembroDto.MiembroImportResultDto;
import com.mcmm.service.IBitacora;
import com.mcmm.service.IMiembro;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * /importar y /plantilla leen el rol y la iglesia directo del Authentication
 * (isCurrentUserAdmin()/getCurrentIglesiaId()), no de @WithMockUser — hace
 * falta un Authentication real con getDetails() como Map para simular un
 * usuario de iglesia, y un GrantedAuthority "ROLE_ADMIN" literal para simular
 * al administrador.
 */
@WebMvcTest(controllers = MiembroController.class)
class MiembroControllerTest extends ControllerTestSupport {

    @MockBean
    private IMiembro miembroService;

    @MockBean
    private IBitacora bitacoraService;

    // MIEMBROS:CREAR (para /importar) y MIEMBROS:VER (para /plantilla) son las
    // autoridades que un ENCARGADO_IGLESIA tendria en la practica via su
    // RolCargo — se agregan explicitamente porque el rol solo no alcanza el
    // nivel de metodo de esos dos endpoints.
    private UsernamePasswordAuthenticationToken usuarioDeIglesia(Long iglesiaId) {
        var auth = new UsernamePasswordAuthenticationToken(
                "carlos", null, List.of(
                        new SimpleGrantedAuthority("ROLE_ENCARGADO_IGLESIA"),
                        new SimpleGrantedAuthority("MIEMBROS:CREAR"),
                        new SimpleGrantedAuthority("MIEMBROS:VER")));
        auth.setDetails(Map.of("iglesiaId", iglesiaId));
        return auth;
    }

    private UsernamePasswordAuthenticationToken usuarioDeIglesiaSinDetails() {
        return new UsernamePasswordAuthenticationToken(
                "carlos", null, List.of(
                        new SimpleGrantedAuthority("ROLE_ENCARGADO_IGLESIA"),
                        new SimpleGrantedAuthority("MIEMBROS:CREAR"),
                        new SimpleGrantedAuthority("MIEMBROS:VER")));
    }

    private UsernamePasswordAuthenticationToken admin() {
        return new UsernamePasswordAuthenticationToken(
                "admin", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("MIEMBROS:CREAR")));
    }

    private MiembroDto dtoValido() {
        return MiembroDto.builder().nombre("Carlos").apellido("Perez").build();
    }

    // ───────────────────────── create / validacion ─────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("create: nombre/apellido con solo un caracter viola @Size(min=2), responde 400")
    void create_nombreMuyCorto_devuelve400() throws Exception {
        MiembroDto invalido = MiembroDto.builder().nombre("C").apellido("P").build();

        mockMvc.perform(post("/api/miembro/v1/create").with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message.nombre").exists())
                .andExpect(jsonPath("$.message.apellido").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("create: ci con caracteres invalidos viola @Pattern, responde 400")
    void create_ciConCaracteresInvalidos_devuelve400() throws Exception {
        MiembroDto invalido = MiembroDto.builder().nombre("Carlos").apellido("Perez").ci("!!!invalido!!!").build();

        mockMvc.perform(post("/api/miembro/v1/create").with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message.ci").exists());
    }

    @Test
    @WithMockUser(roles = "TESORERO")
    @DisplayName("create: TESORERO entra a la clase (MIEMBROS:VER) pero create() exige MIEMBROS:CREAR o ADMIN, rechaza con 403")
    void create_tesorero_rechazaCon403() throws Exception {
        mockMvc.perform(post("/api/miembro/v1/create").with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dtoValido())))
                .andExpect(status().isForbidden());
    }

    // ───────────────────────── buscarci: no-encontrado no es una excepcion ─────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("buscarci: no encontrado responde 404 con ApiResponse, sin pasar por GlobalExceptionHandler")
    void buscarCi_noEncontrado_devuelve404ConApiResponse() throws Exception {
        when(miembroService.buscarCi("999")).thenReturn(null);

        mockMvc.perform(get("/api/miembro/v1/buscarci/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Miembro no encontrado con CI: 999"));
    }

    // ───────────────────────── MIEMBROS:SUBIR_FOTO, permiso granular propio ─────────────────────────

    @Test
    @WithMockUser(authorities = "MIEMBROS:SUBIR_FOTO")
    @DisplayName("foto: MIEMBROS:SUBIR_FOTO habilita subir la foto por si sola, sin MIEMBROS:EDITAR ni ADMIN")
    void uploadProfilePhoto_conAutoridadGranular_devuelve200() throws Exception {
        // Es la unica autoridad de todo el backend que existe para un solo
        // endpoint (se siembra en SecuritySeedDataInitializer y se asigna a un
        // rol real). Sin este test, un typo en la cadena la volveria
        // inconcedible en silencio: la accion se seguiria creando, el rol la
        // seguiria teniendo, y el endpoint rechazaria igual a esos usuarios.
        when(miembroService.updateProfilePhoto(eq(1L), any())).thenReturn("/uploads/miembros/foto.jpg");
        MockMultipartFile file = new MockMultipartFile("file", "foto.jpg", "image/jpeg", "contenido".getBytes());

        mockMvc.perform(multipart("/api/miembro/v1/1/foto").file(file).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos").value("/uploads/miembros/foto.jpg"));
    }

    @Test
    @WithMockUser(authorities = "MIEMBROS:SUBIR_FOTO")
    @DisplayName("foto: pero SUBIR_FOTO NO alcanza para borrarla (ese endpoint exige MIEMBROS:EDITAR) -- el permiso es realmente mas angosto")
    void deleteProfilePhoto_conSoloSubirFoto_rechazaCon403() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/miembro/v1/1/foto").with(csrf()))
                .andExpect(status().isForbidden());
    }

    // ───────────────────────── importar: depende del Authentication real ─────────────────────────

    @Test
    @DisplayName("importar: usuario de iglesia con iglesiaId en el token, importa a SU iglesia")
    void importar_usuarioDeIglesia_importaASuIglesia() throws Exception {
        when(miembroService.importFromExcel(any(), eq(7L)))
                .thenReturn(MiembroImportResultDto.builder().imported(3).omitidos(0).build());
        MockMultipartFile file = new MockMultipartFile("file", "miembros.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "contenido".getBytes());

        mockMvc.perform(multipart("/api/miembro/v1/importar").file(file)
                        .with(csrf()).with(authentication(usuarioDeIglesia(7L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.imported").value(3));

        verify(miembroService).importFromExcel(any(), eq(7L));
        verify(miembroService, never()).importFromExcelPorNombreIglesia(any());
    }

    @Test
    @DisplayName("importar: usuario de iglesia sin iglesiaId en el token ni parametro, responde 400 sin llamar al servicio")
    void importar_sinIglesiaResoluble_devuelve400() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "miembros.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "contenido".getBytes());

        mockMvc.perform(multipart("/api/miembro/v1/importar").file(file)
                        .with(csrf()).with(authentication(usuarioDeIglesiaSinDetails())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Debe especificar la iglesia de destino para los miembros."));

        verify(miembroService, never()).importFromExcel(any(), any());
    }

    @Test
    @DisplayName("importar: usuario de iglesia sin iglesiaId en el token, pero con el parametro iglesiaId, lo usa como respaldo")
    void importar_sinTokenPeroConParametro_usaElParametro() throws Exception {
        when(miembroService.importFromExcel(any(), eq(9L)))
                .thenReturn(MiembroImportResultDto.builder().imported(1).omitidos(0).build());
        MockMultipartFile file = new MockMultipartFile("file", "miembros.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "contenido".getBytes());

        mockMvc.perform(multipart("/api/miembro/v1/importar").file(file).param("iglesiaId", "9")
                        .with(csrf()).with(authentication(usuarioDeIglesiaSinDetails())))
                .andExpect(status().isOk());

        verify(miembroService).importFromExcel(any(), eq(9L));
    }

    @Test
    @DisplayName("importar: ROLE_ADMIN importa por nombre de iglesia de la plantilla, ignora iglesiaId por completo")
    void importar_admin_importaPorNombreDeIglesia() throws Exception {
        when(miembroService.importFromExcelPorNombreIglesia(any()))
                .thenReturn(MiembroImportResultDto.builder().imported(5).omitidos(1).build());
        MockMultipartFile file = new MockMultipartFile("file", "miembros.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "contenido".getBytes());

        mockMvc.perform(multipart("/api/miembro/v1/importar").file(file)
                        .with(csrf()).with(authentication(admin())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.imported").value(5));

        verify(miembroService, never()).importFromExcel(any(), any());
    }

    // ───────────────────────── plantilla: tambien depende de isCurrentUserAdmin() ─────────────────────────

    @Test
    @DisplayName("plantilla: usuario de iglesia recibe la plantilla base (sin columna Iglesia)")
    void plantilla_usuarioDeIglesia_generaPlantillaBase() throws Exception {
        when(miembroService.generateExcelTemplate(false)).thenReturn(new byte[]{1, 2, 3});

        mockMvc.perform(get("/api/miembro/v1/plantilla").with(authentication(usuarioDeIglesiaSinDetails())))
                .andExpect(status().isOk());

        verify(miembroService).generateExcelTemplate(false);
    }

    @Test
    @DisplayName("plantilla: ROLE_ADMIN recibe la plantilla con columna Iglesia")
    void plantilla_admin_generaPlantillaConColumnaIglesia() throws Exception {
        when(miembroService.generateExcelTemplate(true)).thenReturn(new byte[]{1, 2, 3});

        mockMvc.perform(get("/api/miembro/v1/plantilla").with(authentication(admin())))
                .andExpect(status().isOk());

        verify(miembroService).generateExcelTemplate(true);
    }
}
