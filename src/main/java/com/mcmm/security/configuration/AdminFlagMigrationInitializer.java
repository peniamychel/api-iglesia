package com.mcmm.security.configuration;

import com.mcmm.model.dao.UsuarioDao;
import com.mcmm.model.entity.Usuario;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Backfill idempotente del flag {@code esAdmin} de {@link Usuario}.
 *
 * Antes, el privilegio de Administrador Global se inferia de {@code miembro == null}.
 * Ahora es una marca explicita. Este runner corre una sola vez de forma efectiva:
 * para los usuarios existentes cuyo flag todavia es {@code null}, asigna
 * {@code esAdmin = true} a los que no tienen miembro vinculado (los admins actuales)
 * y {@code false} al resto. Tras el primer arranque no vuelve a tocar nada.
 */
@Slf4j
@Component
@Order(1)
public class AdminFlagMigrationInitializer implements CommandLineRunner {

    private final UsuarioDao usuarioDao;

    public AdminFlagMigrationInitializer(UsuarioDao usuarioDao) {
        this.usuarioDao = usuarioDao;
    }

    @Override
    @Transactional
    public void run(String... args) {
        List<Usuario> pendientes = usuarioDao.findByEsAdminIsNull();
        if (pendientes.isEmpty()) {
            return;
        }
        for (Usuario u : pendientes) {
            // Regla de migracion: los usuarios sin miembro eran los admin globales.
            u.setEsAdmin(u.getMiembro() == null);
        }
        usuarioDao.saveAll(pendientes);
        log.info("Migracion esAdmin: {} usuario(s) inicializados (admins={}).",
                pendientes.size(),
                pendientes.stream().filter(Usuario::getEsAdmin).count());
    }
}
