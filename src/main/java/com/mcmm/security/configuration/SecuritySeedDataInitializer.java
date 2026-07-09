package com.mcmm.security.configuration;

import com.mcmm.model.dao.AccionDao;
import com.mcmm.model.dao.RolCargoDao;
import com.mcmm.model.dao.ServicioDao;
import com.mcmm.model.entity.Accion;
import com.mcmm.model.entity.RolCargo;
import com.mcmm.model.entity.Servicio;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class SecuritySeedDataInitializer implements CommandLineRunner {

    private final ServicioDao servicioDao;
    private final AccionDao accionDao;
    private final RolCargoDao rolCargoDao;

    public SecuritySeedDataInitializer(ServicioDao servicioDao, AccionDao accionDao, RolCargoDao rolCargoDao) {
        this.servicioDao = servicioDao;
        this.accionDao = accionDao;
        this.rolCargoDao = rolCargoDao;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Iniciando verificación de datos semilla para Servicios y Acciones...");

        // 1. Crear o recuperar Servicios
        Servicio sMiembros = getOrCreateServicio("MIEMBROS", "Gestión de Miembros", "Módulo de miembros e historial de membresía", "group", "/admin/miembros", 1);
        Servicio sIglesias = getOrCreateServicio("IGLESIAS", "Gestión de Iglesias", "Módulo de administración de iglesias y sedes", "church", "/admin/igles", 2);
        Servicio sObreros = getOrCreateServicio("OBREROS", "Gestión de Obreros y Cargos", "Módulo de obreros, asignaciones y actas", "badge", "/admin/cargos", 3);
        Servicio sEventos = getOrCreateServicio("EVENTOS", "Gestión de Eventos", "Módulo de eventos y conferencias", "event", "/admin/eventos", 4);
        Servicio sCertificados = getOrCreateServicio("CERTIFICADOS", "Certificados y Actas", "Módulo de generación de certificados", "card_membership", "/admin/certificados", 5);
        Servicio sUsuarios = getOrCreateServicio("USUARIOS", "Usuarios y Seguridad", "Módulo de administración de usuarios y roles", "manage_accounts", "/admin/usuario-sistema", 6);
        Servicio sDashboard = getOrCreateServicio("DASHBOARD", "Panel de Control", "Módulo principal con métricas y estadísticas", "dashboard", "/admin/dashboard", 0);
        Servicio sBitacora = getOrCreateServicio("BITACORA", "Bitácora del Sistema", "Historial de auditoría y cambios", "history", "/admin/bitacora", 7);
        Servicio sOfrendas = getOrCreateServicio("OFRENDAS", "Gestión de Ofrendas", "Módulo de gestión de ofrendas y diezmos", "monetization_on", "/admin/ofrendas", 8);

        // 2. Crear o recuperar Acciones por servicio
        getOrCreateAccion(sMiembros, "VER", "Ver Miembros", "Permite visualizar la lista y detalle de miembros");
        getOrCreateAccion(sMiembros, "CREAR", "Crear Miembro", "Permite registrar nuevos miembros");
        getOrCreateAccion(sMiembros, "EDITAR", "Editar Miembro", "Permite modificar la información de miembros");
        getOrCreateAccion(sMiembros, "ELIMINAR", "Eliminar Miembro", "Permite eliminar registros de miembros");
        getOrCreateAccion(sMiembros, "SUBIR_FOTO", "Subir Foto", "Permite cambiar la foto de perfil del miembro");
        getOrCreateAccion(sMiembros, "IMPRIMIR_HISTORIAL", "Imprimir Historial", "Permite exportar el PDF del historial del miembro");

        getOrCreateAccion(sIglesias, "VER", "Ver Iglesias", "Permite ver el listado y fichas de iglesias");
        getOrCreateAccion(sIglesias, "CREAR", "Crear Iglesia", "Permite registrar nuevas iglesias");
        getOrCreateAccion(sIglesias, "EDITAR", "Editar Iglesia", "Permite actualizar datos de iglesias");
        getOrCreateAccion(sIglesias, "ELIMINAR", "Eliminar Iglesia", "Permite dar de baja o eliminar iglesias");
        getOrCreateAccion(sIglesias, "ASIGNAR_PASTOR", "Asignar Pastor", "Permite asignar pastor responsable a iglesia");

        getOrCreateAccion(sObreros, "VER", "Ver Obreros", "Permite ver obreros y cargos asignados");
        getOrCreateAccion(sObreros, "DESIGNAR", "Designar Obrero", "Permite asignar nuevos cargos u obreros");
        getOrCreateAccion(sObreros, "EDITAR", "Editar Cargo", "Permite modificar datos del cargo");
        getOrCreateAccion(sObreros, "DESVINCULAR", "Desvincular Obrero", "Permite finalizar la asignación de un obrero");
        getOrCreateAccion(sObreros, "SUBIR_ACTA", "Subir Acta", "Permite adjuntar acta de asignación o deslindación");

        getOrCreateAccion(sEventos, "VER", "Ver Eventos", "Permite consultar eventos registrados");
        getOrCreateAccion(sEventos, "CREAR", "Crear Evento", "Permite crear nuevos eventos");
        getOrCreateAccion(sEventos, "EDITAR", "Editar Evento", "Permite modificar eventos");
        getOrCreateAccion(sEventos, "ELIMINAR", "Eliminar Evento", "Permite cancelar o eliminar eventos");

        getOrCreateAccion(sCertificados, "VER", "Ver Certificados", "Permite listar certificados emitidos");
        getOrCreateAccion(sCertificados, "GENERAR", "Generar Certificado", "Permite emitir nuevos certificados");
        getOrCreateAccion(sCertificados, "IMPRIMIR", "Imprimir Certificado", "Permite descargar en PDF certificados");

        getOrCreateAccion(sUsuarios, "VER", "Ver Usuarios", "Permite ver usuarios del sistema");
        getOrCreateAccion(sUsuarios, "CREAR", "Crear Usuario", "Permite crear cuentas de usuario");
        getOrCreateAccion(sUsuarios, "EDITAR", "Editar Usuario", "Permite modificar usuarios y roles");
        getOrCreateAccion(sUsuarios, "CAMBIAR_PASSWORD", "Cambiar Contraseña", "Permite restablecer contraseñas");

        getOrCreateAccion(sDashboard, "VER", "Ver Dashboard", "Permite acceder al panel principal");
        getOrCreateAccion(sBitacora, "VER", "Ver Bitácora", "Permite auditar el historial de acciones");

        getOrCreateAccion(sOfrendas, "VER", "Ver Ofrendas", "Permite ver registros de ofrendas");
        getOrCreateAccion(sOfrendas, "CREAR", "Registrar Ofrenda", "Permite registrar nuevas ofrendas");
        getOrCreateAccion(sOfrendas, "EDITAR", "Editar Ofrenda", "Permite modificar registros de ofrendas");
        getOrCreateAccion(sOfrendas, "ELIMINAR", "Eliminar Ofrenda", "Permite eliminar registros de ofrendas");

        // 3. Sincronizar Roles y sus Acciones exactas según especificaciones de capturas
        List<String> pastorAcciones = Arrays.asList(
                "DASHBOARD:VER",
                "MIEMBROS:VER", "MIEMBROS:CREAR", "MIEMBROS:EDITAR", "MIEMBROS:ELIMINAR", "MIEMBROS:SUBIR_FOTO", "MIEMBROS:IMPRIMIR_HISTORIAL",
                "IGLESIAS:VER",
                "OBREROS:VER", "OBREROS:DESIGNAR", "OBREROS:EDITAR", "OBREROS:DESVINCULAR", "OBREROS:SUBIR_ACTA",
                "EVENTOS:VER", "EVENTOS:CREAR", "EVENTOS:EDITAR", "EVENTOS:ELIMINAR",
                "CERTIFICADOS:VER", "CERTIFICADOS:GENERAR", "CERTIFICADOS:IMPRIMIR",
                "OFRENDAS:VER", "OFRENDAS:CREAR", "OFRENDAS:EDITAR", "OFRENDAS:ELIMINAR"
        );

        List<String> liderJovenesAcciones = Arrays.asList(
                "DASHBOARD:VER",
                "MIEMBROS:VER",
                "EVENTOS:VER", "EVENTOS:CREAR", "EVENTOS:EDITAR"
        );

        List<String> diaconoAcciones = Arrays.asList(
                "DASHBOARD:VER",
                "MIEMBROS:VER",
                "EVENTOS:VER", "EVENTOS:CREAR",
                "CERTIFICADOS:VER", "CERTIFICADOS:GENERAR", "CERTIFICADOS:IMPRIMIR"
        );

        List<String> tesoreroAcciones = Arrays.asList(
                "DASHBOARD:VER",
                "MIEMBROS:VER",
                "OFRENDAS:VER", "OFRENDAS:CREAR", "OFRENDAS:EDITAR", "OFRENDAS:ELIMINAR"
        );

        syncRolePrivileges("PASTOR", "Pastor", pastorAcciones);
        syncRolePrivileges("ENCARGADO_IGLESIA", "Encargado de Iglesia", pastorAcciones);
        syncRolePrivileges("LIDER_JOVENES", "Líder de Jóvenes", liderJovenesAcciones);
        syncRolePrivileges("DIACONO", "Diácono", diaconoAcciones);
        syncRolePrivileges("TESORERO", "Tesorero", tesoreroAcciones);

        log.info("Completada la inicialización de Servicios y Acciones en la base de datos.");
    }

    private void syncRolePrivileges(String nombreRol, String nombre, List<String> authorityCodes) {
        RolCargo rc = rolCargoDao.findByNombreRol(nombreRol).orElseGet(() -> {
            RolCargo nuevo = RolCargo.builder()
                    .nombreRol(nombreRol)
                    .nombre(nombre)
                    .tipo("INTERNO")
                    .estado(true)
                    .build();
            return rolCargoDao.save(nuevo);
        });

        java.util.Set<Accion> targetAcciones = new java.util.HashSet<>();
        for (String auth : authorityCodes) {
            String[] parts = auth.split(":");
            if (parts.length == 2) {
                accionDao.findByServicioCodigoAndCodigo(parts[0], parts[1])
                        .ifPresent(targetAcciones::add);
            }
        }
        rc.setAcciones(targetAcciones);
        rolCargoDao.save(rc);
        log.info("Sincronizado rol {} con {} acciones", nombreRol, targetAcciones.size());
    }

    private Servicio getOrCreateServicio(String codigo, String nombre, String descripcion, String icono, String ruta, int orden) {
        return servicioDao.findByCodigo(codigo).orElseGet(() -> {
            Servicio nuevo = Servicio.builder()
                    .codigo(codigo)
                    .nombre(nombre)
                    .descripcion(descripcion)
                    .icono(icono)
                    .ruta(ruta)
                    .orden(orden)
                    .activo(true)
                    .build();
            Servicio saved = servicioDao.save(nuevo);
            log.info("Creado Servicio semilla: {}", codigo);
            return saved;
        });
    }

    private Accion getOrCreateAccion(Servicio servicio, String codigo, String nombre, String descripcion) {
        return accionDao.findByServicioCodigoAndCodigo(servicio.getCodigo(), codigo).orElseGet(() -> {
            Accion nueva = Accion.builder()
                    .servicio(servicio)
                    .codigo(codigo)
                    .nombre(nombre)
                    .descripcion(descripcion)
                    .activo(true)
                    .build();
            Accion saved = accionDao.save(nueva);
            log.info("Creada Acción semilla: {}:{}", servicio.getCodigo(), codigo);
            return saved;
        });
    }
}
