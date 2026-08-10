package com.mcmm.service.impl;

import com.mcmm.exception.NotFoundExceptionResource;
import com.mcmm.model.dao.AccionDao;
import com.mcmm.model.dao.RolCargoDao;
import com.mcmm.model.dto.RolCargoDto;
import com.mcmm.model.entity.Accion;
import com.mcmm.model.entity.RolCargo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * RolCargoImpl instancia su ModelMapper inline (no lo inyecta), asi que este
 * test usa un ModelMapper real, no un mock — igual que ResponsableEventoImplTest
 * — para que reproduzca cualquier ConfigurationException real si el mapeo se
 * vuelve ambiguo. create() ademas ignora a proposito las acciones/id que
 * vengan en el payload (solo se gestionan via addAccion/removeAccion), y
 * update() SI protege el estado con un null-check — a diferencia del patron
 * de riesgo visto en MiembroImpl/EventoImpl, este metodo esta bien defendido.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RolCargoImplTest {

    @Mock private RolCargoDao rolCargoDao;
    @Mock private AccionDao accionDao;

    private RolCargoImpl service;

    @BeforeEach
    void setUp() {
        service = new RolCargoImpl(rolCargoDao, accionDao);
    }

    // ───────────────────────── create ─────────────────────────

    @Test
    @DisplayName("create: ignora el id y las acciones que vengan en el payload")
    void create_ignoraIdYAccionesDelPayload() {
        when(rolCargoDao.save(any(RolCargo.class))).thenAnswer(inv -> inv.getArgument(0));

        RolCargoDto dto = RolCargoDto.builder().id(999L).nombre("Pastor").tipo("CARGO")
                .acciones(Set.of()).build();
        RolCargoDto resultado = service.create(dto);

        assertThat(resultado.getNombre()).isEqualTo("Pastor");
        // El id real vino del entity guardado (null en este mock), no del payload.
        assertThat(resultado.getId()).isNull();
    }

    // ───────────────────────── update ─────────────────────────

    @Test
    @DisplayName("update: estado=null en el DTO preserva el estado existente, no lo pisa")
    void update_estadoNullEnDto_preservaElExistente() {
        RolCargo existente = new RolCargo();
        existente.setId(1L);
        existente.setEstado(true);
        when(rolCargoDao.findById(1L)).thenReturn(Optional.of(existente));
        when(rolCargoDao.save(any(RolCargo.class))).thenAnswer(inv -> inv.getArgument(0));

        RolCargoDto dto = RolCargoDto.builder().id(1L).nombre("Pastor").estado(null).build();
        service.update(dto);

        assertThat(existente.getEstado()).isTrue();
    }

    @Test
    @DisplayName("update: estado explicito en el DTO si se aplica")
    void update_estadoExplicito_seAplica() {
        RolCargo existente = new RolCargo();
        existente.setId(1L);
        existente.setEstado(true);
        when(rolCargoDao.findById(1L)).thenReturn(Optional.of(existente));
        when(rolCargoDao.save(any(RolCargo.class))).thenAnswer(inv -> inv.getArgument(0));

        RolCargoDto dto = RolCargoDto.builder().id(1L).nombre("Pastor").estado(false).build();
        service.update(dto);

        assertThat(existente.getEstado()).isFalse();
    }

    @Test
    @DisplayName("update: id inexistente lanza NotFoundExceptionResource")
    void update_idInexistente_lanzaNotFound() {
        when(rolCargoDao.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(RolCargoDto.builder().id(404L).build()))
                .isInstanceOf(NotFoundExceptionResource.class);
    }

    // ───────────────────────── addAccion / removeAccion ─────────────────────────

    @Test
    @DisplayName("addAccion: agrega la accion al set de acciones del rol")
    void addAccion_agregaLaAccion() {
        RolCargo rc = new RolCargo();
        rc.setId(1L);
        rc.setAcciones(new HashSet<>());
        Accion accion = new Accion();
        accion.setId(5L);
        accion.setCodigo("VER");
        when(rolCargoDao.findById(1L)).thenReturn(Optional.of(rc));
        when(accionDao.findById(5L)).thenReturn(Optional.of(accion));
        when(rolCargoDao.save(any(RolCargo.class))).thenAnswer(inv -> inv.getArgument(0));

        service.addAccion(1L, 5L);

        assertThat(rc.getAcciones()).contains(accion);
    }

    @Test
    @DisplayName("addAccion: rolCargo inexistente lanza NotFoundExceptionResource")
    void addAccion_rolCargoInexistente_lanzaNotFound() {
        when(rolCargoDao.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addAccion(404L, 5L)).isInstanceOf(NotFoundExceptionResource.class);
        verify(accionDao, never()).findById(any());
    }

    @Test
    @DisplayName("addAccion: accion inexistente lanza NotFoundExceptionResource")
    void addAccion_accionInexistente_lanzaNotFound() {
        RolCargo rc = new RolCargo();
        rc.setId(1L);
        when(rolCargoDao.findById(1L)).thenReturn(Optional.of(rc));
        when(accionDao.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addAccion(1L, 999L)).isInstanceOf(NotFoundExceptionResource.class);
    }

    @Test
    @DisplayName("removeAccion: quita la accion del set de acciones del rol")
    void removeAccion_quitaLaAccion() {
        Accion accion = new Accion();
        accion.setId(5L);
        RolCargo rc = new RolCargo();
        rc.setId(1L);
        rc.setAcciones(new HashSet<>(Set.of(accion)));
        when(rolCargoDao.findById(1L)).thenReturn(Optional.of(rc));
        when(accionDao.findById(5L)).thenReturn(Optional.of(accion));
        when(rolCargoDao.save(any(RolCargo.class))).thenAnswer(inv -> inv.getArgument(0));

        service.removeAccion(1L, 5L);

        assertThat(rc.getAcciones()).doesNotContain(accion);
    }

    // ───────────────────────── delete / estado / findAllCargo ─────────────────────────

    @Test
    @DisplayName("delete: id inexistente lanza NotFoundExceptionResource, no llama delete")
    void delete_idInexistente_lanzaNotFound() {
        when(rolCargoDao.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(404L)).isInstanceOf(NotFoundExceptionResource.class);
        verify(rolCargoDao, never()).delete(any());
    }

    @Test
    @DisplayName("findAllCargo: delega en el DAO excluyendo ADMIN, sin logica propia que filtre de mas")
    void findAllCargo_delegaEnElDao() {
        RolCargo rc = new RolCargo();
        rc.setId(1L);
        rc.setNombreRol("PASTOR");
        when(rolCargoDao.findByEstadoTrueAndNombreRolNot("ADMIN")).thenReturn(java.util.List.of(rc));

        assertThat(service.findAllCargo()).hasSize(1);
    }
}
