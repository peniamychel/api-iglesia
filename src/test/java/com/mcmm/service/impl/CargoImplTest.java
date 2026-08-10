package com.mcmm.service.impl;

import com.mcmm.exception.NotFoundExceptionResource;
import com.mcmm.model.dao.CargoDao;
import com.mcmm.model.dao.IglesiaDao;
import com.mcmm.model.dao.MiembroDao;
import com.mcmm.model.dao.RolCargoDao;
import com.mcmm.model.dto.CargoDto;
import com.mcmm.model.entity.Cargo;
import com.mcmm.model.entity.Iglesia;
import com.mcmm.model.entity.Miembro;
import com.mcmm.model.entity.RolCargo;
import com.mcmm.service.FileStorageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Cubre sobre todo las reglas de validacion de create()/update() (iglesia
 * inactiva, miembro inactivo, rolCargo inactivo, colision de rol activo) y
 * una asimetria real entre ambos: create() rechaza asignar un miembro que ya
 * tiene un cargo activo (existsByMiembroIdAndEstadoTrue), pero update() NO
 * repite esa validacion al reasignar el miembro — se documenta con un test
 * explicito del comportamiento actual, sin tocar produccion (puede ser
 * intencional: reasignar via update es una correccion administrativa).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CargoImplTest {

    @Mock private ModelMapper modelMapper;
    @Mock private CargoDao cargoDao;
    @Mock private IglesiaDao iglesiaDao;
    @Mock private MiembroDao miembroDao;
    @Mock private RolCargoDao rolCargoDao;
    @Mock private FileStorageService fileStorageService;

    private CargoImpl service;

    @BeforeEach
    void setUp() {
        service = new CargoImpl(modelMapper, cargoDao, iglesiaDao, miembroDao, rolCargoDao, fileStorageService);
        when(modelMapper.map(any(CargoDto.class), org.mockito.ArgumentMatchers.eq(Cargo.class)))
                .thenReturn(new Cargo());
        when(modelMapper.map(any(Cargo.class), org.mockito.ArgumentMatchers.eq(CargoDto.class)))
                .thenAnswer(inv -> CargoDto.builder().build());
    }

    @AfterEach
    void limpiarContextoDeSeguridad() {
        SecurityContextHolder.clearContext();
    }

    private void loguearComoUsuarioDeIglesia(Long iglesiaId) {
        var auth = new UsernamePasswordAuthenticationToken("carlos", null);
        auth.setDetails(Map.of("iglesiaId", iglesiaId));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private Iglesia nuevaIglesia(Long id, boolean estado) {
        Iglesia i = new Iglesia();
        i.setId(id);
        i.setEstado(estado);
        return i;
    }

    private Miembro nuevoMiembro(Long id, boolean estado) {
        Miembro m = new Miembro();
        m.setId(id);
        m.setEstado(estado);
        return m;
    }

    private RolCargo nuevoRolCargo(Long id, boolean estado) {
        RolCargo rc = new RolCargo();
        rc.setId(id);
        rc.setEstado(estado);
        return rc;
    }

    // ───────────────────────── create ─────────────────────────

    @Test
    @DisplayName("create: iglesia explicita en el DTO pero inactiva, rechaza")
    void create_iglesiaInactiva_lanzaIllegalArgument() {
        when(iglesiaDao.findById(1L)).thenReturn(Optional.of(nuevaIglesia(1L, false)));

        CargoDto dto = CargoDto.builder().iglesiaId(1L).build();
        assertThatThrownBy(() -> service.create(dto)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("create: sin iglesia en el DTO, usa la del contexto de seguridad")
    void create_sinIglesiaEnDto_usaLaDelContexto() {
        loguearComoUsuarioDeIglesia(7L);
        when(iglesiaDao.findById(7L)).thenReturn(Optional.of(nuevaIglesia(7L, true)));
        when(cargoDao.save(any(Cargo.class))).thenAnswer(inv -> inv.getArgument(0));

        CargoDto dto = CargoDto.builder().build();
        assertThatCode(() -> service.create(dto)).doesNotThrowAnyException();
        verify(iglesiaDao).findById(7L);
    }

    @Test
    @DisplayName("create: miembro que ya tiene un cargo activo, rechaza")
    void create_miembroConCargoActivo_lanzaIllegalArgument() {
        when(cargoDao.existsByMiembroIdAndEstadoTrue(5L)).thenReturn(true);

        CargoDto dto = CargoDto.builder().idMiembro(5L).build();
        assertThatThrownBy(() -> service.create(dto)).isInstanceOf(IllegalArgumentException.class);
        verify(miembroDao, never()).findById(any());
    }

    @Test
    @DisplayName("create: miembro inactivo, rechaza")
    void create_miembroInactivo_lanzaIllegalArgument() {
        when(cargoDao.existsByMiembroIdAndEstadoTrue(5L)).thenReturn(false);
        when(miembroDao.findById(5L)).thenReturn(Optional.of(nuevoMiembro(5L, false)));

        CargoDto dto = CargoDto.builder().idMiembro(5L).build();
        assertThatThrownBy(() -> service.create(dto)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("create: rolCargo inactivo, rechaza")
    void create_rolCargoInactivo_lanzaIllegalArgument() {
        when(rolCargoDao.findById(9L)).thenReturn(Optional.of(nuevoRolCargo(9L, false)));

        CargoDto dto = CargoDto.builder().rolCargoId(9L).build();
        assertThatThrownBy(() -> service.create(dto)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("create: happy path resuelve iglesia, miembro y rolCargo y guarda")
    void create_happyPath_resuelveTodasLasRelaciones() {
        when(iglesiaDao.findById(1L)).thenReturn(Optional.of(nuevaIglesia(1L, true)));
        when(cargoDao.existsByMiembroIdAndEstadoTrue(5L)).thenReturn(false);
        when(miembroDao.findById(5L)).thenReturn(Optional.of(nuevoMiembro(5L, true)));
        when(rolCargoDao.findById(9L)).thenReturn(Optional.of(nuevoRolCargo(9L, true)));
        when(cargoDao.save(any(Cargo.class))).thenAnswer(inv -> inv.getArgument(0));

        CargoDto dto = CargoDto.builder().iglesiaId(1L).idMiembro(5L).rolCargoId(9L).build();
        assertThatCode(() -> service.create(dto)).doesNotThrowAnyException();
    }

    // ───────────────────────── update ─────────────────────────

    @Test
    @DisplayName("update: NO revalida colision de cargo activo al reasignar el miembro (asimetria documentada con create)")
    void update_noRevalidaColisionDeMiembro() {
        Cargo existente = new Cargo();
        existente.setId(1L);
        when(cargoDao.findById(1L)).thenReturn(Optional.of(existente));
        when(miembroDao.findById(5L)).thenReturn(Optional.of(nuevoMiembro(5L, true)));
        when(cargoDao.save(any(Cargo.class))).thenAnswer(inv -> inv.getArgument(0));

        CargoDto dto = CargoDto.builder().id(1L).idMiembro(5L).build();
        assertThatCode(() -> service.update(dto)).doesNotThrowAnyException();

        verify(cargoDao, never()).existsByMiembroIdAndEstadoTrue(any());
    }

    @Test
    @DisplayName("update: iglesia inactiva, rechaza")
    void update_iglesiaInactiva_lanzaIllegalArgument() {
        Cargo existente = new Cargo();
        existente.setId(1L);
        when(cargoDao.findById(1L)).thenReturn(Optional.of(existente));
        when(iglesiaDao.findById(2L)).thenReturn(Optional.of(nuevaIglesia(2L, false)));

        CargoDto dto = CargoDto.builder().id(1L).iglesiaId(2L).build();
        assertThatThrownBy(() -> service.update(dto)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("update: id inexistente lanza NotFoundExceptionResource")
    void update_idInexistente_lanzaNotFound() {
        when(cargoDao.findById(404L)).thenReturn(Optional.empty());

        CargoDto dto = CargoDto.builder().id(404L).build();
        assertThatThrownBy(() -> service.update(dto)).isInstanceOf(NotFoundExceptionResource.class);
    }

    // ───────────────────────── delete / estado ─────────────────────────

    @Test
    @DisplayName("delete: id inexistente lanza NotFoundExceptionResource, no llama deleteById")
    void delete_idInexistente_lanzaNotFound() {
        when(cargoDao.existsById(404L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(404L)).isInstanceOf(NotFoundExceptionResource.class);
        verify(cargoDao, never()).deleteById(any());
    }

    @Test
    @DisplayName("estado: id inexistente lanza NotFoundExceptionResource, no llama toggleEstado")
    void estado_idInexistente_lanzaNotFound() {
        when(cargoDao.existsById(404L)).thenReturn(false);

        assertThatThrownBy(() -> service.estado(404L)).isInstanceOf(NotFoundExceptionResource.class);
        verify(cargoDao, never()).toggleEstado(any());
    }

    // ───────────────────────── save ─────────────────────────

    @Test
    @DisplayName("save: violacion de integridad se traduce a IllegalArgumentException con mensaje claro")
    void save_violacionDeIntegridad_seTraduce() {
        when(cargoDao.save(any(Cargo.class))).thenThrow(new DataIntegrityViolationException("fk violada"));

        assertThatThrownBy(() -> service.save(CargoDto.builder().build()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ───────────────────────── estadoConFecha ─────────────────────────

    @Test
    @DisplayName("estadoConFecha: al desactivar, fija fechaFin con la fecha dada")
    void estadoConFecha_alDesactivar_fijaFechaFinDada() {
        Cargo cargo = new Cargo();
        cargo.setId(1L);
        cargo.setEstado(true);
        when(cargoDao.findById(1L)).thenReturn(Optional.of(cargo));

        Date fechaFin = new Date(1000);
        boolean nuevoEstado = service.estadoConFecha(1L, fechaFin);

        assertThat(nuevoEstado).isFalse();
        assertThat(cargo.getFechaFin()).isEqualTo(fechaFin);
    }

    @Test
    @DisplayName("estadoConFecha: al reactivar, limpia fechaFin y borra el acta de deslindacion (sin propagar error de borrado)")
    void estadoConFecha_alReactivar_limpiaFechaFinYActa() throws java.io.IOException {
        Cargo cargo = new Cargo();
        cargo.setId(1L);
        cargo.setEstado(false);
        cargo.setFechaFin(new Date());
        cargo.setUriActaDeslindacion("acta.pdf");
        when(cargoDao.findById(1L)).thenReturn(Optional.of(cargo));
        doThrow(new java.io.IOException("no se pudo borrar")).when(fileStorageService).deleteFile(org.mockito.ArgumentMatchers.anyString());

        boolean nuevoEstado = service.estadoConFecha(1L, null);

        assertThat(nuevoEstado).isTrue();
        assertThat(cargo.getFechaFin()).isNull();
        assertThat(cargo.getUriActaDeslindacion()).isNull();
    }

    // ───────────────────────── findMisColaboradores ─────────────────────────

    @Test
    @DisplayName("findMisColaboradores: usuario de iglesia ve solo los cargos de su iglesia")
    void findMisColaboradores_usuarioDeIglesia_filtraPorSuIglesia() {
        loguearComoUsuarioDeIglesia(7L);
        Cargo cargo = new Cargo();
        cargo.setId(1L);
        when(cargoDao.findByIglesiaId(7L)).thenReturn(java.util.List.of(cargo));

        assertThat(service.findMisColaboradores()).hasSize(1);
        verify(cargoDao, never()).findAll();
    }
}
