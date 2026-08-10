package com.mcmm.controller.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcmm.model.dao.BitacoraDao;
import com.mcmm.model.dao.UsuarioDao;
import com.mcmm.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Base comun para los tests @WebMvcTest de controladores.
 *
 * SecurityConfig (donde vive @EnableMethodSecurity) es un @Configuration
 * simple, no un @Controller/@ControllerAdvice/Filter/etc., asi que
 * @WebMvcTest NO lo carga por defecto: sin este @Import, todos los
 * @PreAuthorize de los controladores serian no-ops y un test de "usuario sin
 * permiso" pasaria por accidente aunque el candado real este roto.
 *
 * JwtAuthorizationFilter y LoginRateLimitFilter SI se cargan solos (son
 * @Component + Filter, y WebMvcTypeExcludeFilter los incluye), pero
 * addFilters=false evita que se ejecuten en cada request — no hace falta un
 * JWT real, la autenticacion de cada test se simula con @WithMockUser o
 * SecurityMockMvcRequestPostProcessors.user(...). JwtUtils sigue haciendo
 * falta como @MockBean solo para que el filtro pueda construirse al levantar
 * el contexto (addFilters=false no evita la creacion del bean, solo su
 * ejecucion).
 *
 * GlobalExceptionHandler es @RestControllerAdvice (tambien auto-incluido) y
 * tiene BitacoraDao/UsuarioDao @Autowired por campo — sin mockearlos el
 * contexto no levanta.
 *
 * OJO CSRF: como no se importa el SecurityConfig real (que en produccion
 * tiene CSRF deshabilitado), Boot auto-configura una cadena de seguridad por
 * defecto para el slice de @WebMvcTest, y esa SI trae CSRF activo. Aunque
 * addFilters=false evita que los filtros de la app corran, no evita este
 * chequeo — un POST/PUT/DELETE sin
 * .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
 * devuelve 403 vacio (no pasa por GlobalExceptionHandler) sin importar el rol
 * del usuario. Es un artefacto del slice de test, no de produccion real.
 */
@AutoConfigureMockMvc(addFilters = false)
@Import(ControllerTestSupport.MethodSecurityTestConfig.class)
public abstract class ControllerTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    protected JwtUtils jwtUtils;

    @MockBean
    protected BitacoraDao bitacoraDao;

    @MockBean
    protected UsuarioDao usuarioDao;

    // Verificado por mutacion (2026-08-01): al quitar @EnableMethodSecurity de
    // aqui, 22 tests de autorizacion fallan en 17 de las 20 clases de
    // controlador (todos los "rechazaConNNN", con "expected 403 but was
    // 200/201"). O sea que los @PreAuthorize se estan evaluando de verdad y no
    // son no-ops silenciosos. Si algun dia esta anotacion desaparece, la suite
    // avisa a gritos.
    @TestConfiguration
    @EnableMethodSecurity(prePostEnabled = true)
    static class MethodSecurityTestConfig {
    }
}
