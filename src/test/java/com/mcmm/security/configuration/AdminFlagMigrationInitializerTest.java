package com.mcmm.security.configuration;

import com.mcmm.model.dao.UsuarioDao;
import com.mcmm.model.entity.Miembro;
import com.mcmm.model.entity.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test de la regla de backfill del flag esAdmin: los usuarios sin miembro
 * vinculado eran los administradores globales; el resto no. Regla critica de
 * seguridad — un backfill erroneo asignaria admin incorrectamente.
 */
@ExtendWith(MockitoExtension.class)
class AdminFlagMigrationInitializerTest {

    @Mock
    private UsuarioDao usuarioDao;

    @InjectMocks
    private AdminFlagMigrationInitializer initializer;

    @Test
    void run_asignaAdminSoloAUsuariosSinMiembro() throws Exception {
        Usuario sinMiembro = new Usuario();      // admin global: miembro == null
        Usuario conMiembro = new Usuario();
        conMiembro.setMiembro(new Miembro());    // usuario normal

        when(usuarioDao.findByEsAdminIsNull()).thenReturn(Arrays.asList(sinMiembro, conMiembro));

        initializer.run();

        assertThat(sinMiembro.getEsAdmin()).isTrue();
        assertThat(conMiembro.getEsAdmin()).isFalse();
        verify(usuarioDao).saveAll(Arrays.asList(sinMiembro, conMiembro));
    }

    @Test
    void run_sinPendientes_noPersisteNada() throws Exception {
        when(usuarioDao.findByEsAdminIsNull()).thenReturn(Collections.emptyList());

        initializer.run();

        verify(usuarioDao, never()).saveAll(org.mockito.ArgumentMatchers.anyList());
    }
}
