package com.mcmm.service.impl;

import com.mcmm.model.dao.AccionDao;
import com.mcmm.model.dao.MiembroDao;
import com.mcmm.model.dao.UsuarioDao;
import com.mcmm.model.dto.usuarioDto.UsuarioDto;
import com.mcmm.model.dto.usuarioDto.UsuarioUpdateDto;
import com.mcmm.model.entity.Usuario;
import com.mcmm.service.FileStorageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Test del guard de escalada de privilegios (fix de seguridad #2): otorgar
 * esAdmin=true requiere que el llamante ya sea ROLE_ADMIN. Con el
 * SecurityContext vacio (llamante no autenticado/no admin) debe rechazarse.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UsuarioImplTest {

    @Mock private ModelMapper modelMapper;
    @Mock private UsuarioDao usuarioDao;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private MiembroDao miembroDao;
    @Mock private FileStorageService fileStorageService;
    @Mock private AccionDao accionDao;

    private UsuarioImpl service;

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    private UsuarioImpl newService() {
        return new UsuarioImpl(modelMapper, usuarioDao, passwordEncoder,
                miembroDao, fileStorageService, accionDao);
    }

    @Test
    void create_conEsAdminTrueYLlamanteNoAdmin_lanzaAccessDenied() {
        service = newService();
        SecurityContextHolder.clearContext(); // llamante no admin

        UsuarioDto dto = new UsuarioDto();
        dto.setUsername("nuevo");
        dto.setEmail("nuevo@x.com");
        dto.setPassword("secreto");
        dto.setMiembroId(null);
        dto.setEsAdmin(true);

        when(usuarioDao.existsByUsername("nuevo")).thenReturn(false);
        when(usuarioDao.existsByEmail("nuevo@x.com")).thenReturn(false);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void updateUser_otorgandoEsAdminSiendoNoAdmin_lanzaAccessDenied() {
        service = newService();
        SecurityContextHolder.clearContext(); // llamante no admin

        UsuarioUpdateDto dto = new UsuarioUpdateDto();
        dto.setId(1L);
        dto.setEsAdmin(true);

        lenient().when(usuarioDao.findById(1L)).thenReturn(Optional.of(new Usuario()));

        assertThatThrownBy(() -> service.updateUser(dto))
                .isInstanceOf(AccessDeniedException.class);
    }
}
