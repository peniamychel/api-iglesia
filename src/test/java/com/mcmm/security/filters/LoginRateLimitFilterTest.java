package com.mcmm.security.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test del filtro anti fuerza-bruta de login: permite MAX_ATTEMPTS intentos por
 * IP y bloquea con 429 el siguiente; y por defecto NO confia en X-Forwarded-For
 * (usa la IP real), para que no se pueda evadir rotando la cabecera.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LoginRateLimitFilterTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    private LoginRateLimitFilter filter;

    @BeforeEach
    void setUp() throws Exception {
        filter = new LoginRateLimitFilter();
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
    }

    @Test
    void bloqueaConTooManyRequestsTrasCincoIntentos() throws Exception {
        when(request.getRequestURI()).thenReturn("/login");
        when(request.getMethod()).thenReturn("POST");
        when(request.getRemoteAddr()).thenReturn("10.0.0.5");

        for (int i = 0; i < 5; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }
        // Los primeros 5 pasan al resto de la cadena.
        verify(filterChain, times(5)).doFilter(request, response);

        // El 6to intento se bloquea con 429 y NO continua la cadena.
        filter.doFilterInternal(request, response, filterChain);
        verify(response).setStatus(429);
        verify(filterChain, times(5)).doFilter(request, response); // sigue en 5
    }

    @Test
    void ignoraXForwardedForPorDefecto_usaLaIpReal() throws Exception {
        when(request.getRequestURI()).thenReturn("/login");
        when(request.getMethod()).thenReturn("POST");
        when(request.getRemoteAddr()).thenReturn("10.0.0.9");

        // Aunque el atacante rote la cabecera en cada intento, el bucket se cuenta
        // por getRemoteAddr(), asi que igual se bloquea al 6to.
        when(request.getHeader("X-Forwarded-For"))
                .thenReturn("1.1.1.1", "2.2.2.2", "3.3.3.3", "4.4.4.4", "5.5.5.5", "6.6.6.6");

        for (int i = 0; i < 6; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        verify(response, atLeastOnce()).setStatus(429);
    }

    @Test
    void rutaDistintaDeLoginNoSeLimita() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/miembro/v1/findall");
        when(request.getMethod()).thenReturn("GET");

        for (int i = 0; i < 20; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        verify(filterChain, times(20)).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }
}
