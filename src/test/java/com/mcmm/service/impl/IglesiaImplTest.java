package com.mcmm.service.impl;

import com.mcmm.exception.NotFoundExceptionResource;
import com.mcmm.model.dao.IglesiaDao;
import com.mcmm.model.dto.iglesiaDto.IglesiaDto;
import com.mcmm.model.entity.Iglesia;
import com.mcmm.service.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.modelmapper.ModelMapper;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class IglesiaImplTest {

    @Mock private ModelMapper modelMapper;
    @Mock private IglesiaDao iglesiaDao;
    @Mock private FileStorageService fileStorageService;

    private IglesiaImpl service;

    @BeforeEach
    void setUp() {
        service = new IglesiaImpl(modelMapper, iglesiaDao, fileStorageService);
        ReflectionTestUtils.setField(service, "uploadDir", "/uploads");
        when(modelMapper.map(any(Iglesia.class), eq(IglesiaDto.class)))
                .thenAnswer(inv -> IglesiaDto.builder().build());
    }

    // ───────────────────────── update ─────────────────────────

    @Test
    @DisplayName("update: uriFoto se preserva, no se toca desde este metodo")
    void update_preservaUriFotoExistente() {
        Iglesia existente = new Iglesia();
        existente.setId(1L);
        existente.setUriFoto("original.jpg");
        when(iglesiaDao.findById(1L)).thenReturn(Optional.of(existente));
        when(iglesiaDao.save(any(Iglesia.class))).thenAnswer(inv -> inv.getArgument(0));

        IglesiaDto dto = IglesiaDto.builder().nombre("Palmar").build();
        service.update(1L, dto);

        assertThat(existente.getUriFoto()).isEqualTo("original.jpg");
    }

    @Test
    @DisplayName("update: id inexistente lanza NotFoundExceptionResource")
    void update_idInexistente_lanzaNotFound() {
        when(iglesiaDao.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(404L, IglesiaDto.builder().build()))
                .isInstanceOf(NotFoundExceptionResource.class);
    }

    // ───────────────────────── estado ─────────────────────────

    @Test
    @DisplayName("estado: invierte el estado actual")
    void estado_invierteElEstadoActual() {
        Iglesia iglesia = new Iglesia();
        iglesia.setId(1L);
        iglesia.setEstado(true);
        when(iglesiaDao.findById(1L)).thenReturn(Optional.of(iglesia));
        when(iglesiaDao.save(any(Iglesia.class))).thenAnswer(inv -> inv.getArgument(0));

        service.estado(1L);

        assertThat(iglesia.getEstado()).isFalse();
    }

    // ───────────────────────── delete ─────────────────────────

    @Test
    @DisplayName("delete: si falla borrar la foto, NO propaga el error y borra igual el registro")
    void delete_fotoFallaAlBorrar_noPropagaYBorraElRegistro() throws IOException {
        Iglesia iglesia = new Iglesia();
        iglesia.setId(1L);
        iglesia.setUriFoto("foto.jpg");
        when(iglesiaDao.findById(1L)).thenReturn(Optional.of(iglesia));
        org.mockito.Mockito.doThrow(new IOException("disco lleno"))
                .when(fileStorageService).deleteFile(anyString());

        assertThatCode(() -> service.delete(1L)).doesNotThrowAnyException();

        verify(iglesiaDao).delete(iglesia);
    }

    @Test
    @DisplayName("delete: id inexistente lanza NotFoundExceptionResource")
    void delete_idInexistente_lanzaNotFound() {
        when(iglesiaDao.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(404L)).isInstanceOf(NotFoundExceptionResource.class);
    }

    // ───────────────────────── busquedas por nombre ─────────────────────────

    @Test
    @DisplayName("buscarNombreIglesia: nombre inexistente devuelve null, no lanza")
    void buscarNombreIglesia_inexistente_devuelveNull() {
        when(iglesiaDao.buscarPorNombreIglesia("no existe")).thenReturn(null);

        assertThat(service.buscarNombreIglesia("no existe")).isNull();
    }

    @Test
    @DisplayName("buscarNombreIglesiaExceptoId: nombre inexistente devuelve null, no lanza")
    void buscarNombreIglesiaExceptoId_inexistente_devuelveNull() {
        when(iglesiaDao.buscarPorNombreIglesiaExceptoId(1L, "no existe")).thenReturn(null);

        assertThat(service.buscarNombreIglesiaExceptoId(1L, "no existe")).isNull();
    }

    @Test
    @DisplayName("findByNombreAndIdNot: nombre inexistente devuelve null, no lanza")
    void findByNombreAndIdNot_inexistente_devuelveNull() {
        when(iglesiaDao.findByNombreAndIdNot("no existe", 1L)).thenReturn(null);

        assertThat(service.findByNombreAndIdNot("no existe", 1L)).isNull();
    }

    // ───────────────────────── deleteFoto ─────────────────────────

    @Test
    @DisplayName("deleteFoto: error real al borrar se traduce a RuntimeException, distinto de delete()")
    void deleteFoto_errorAlBorrar_propagaComoRuntimeException() throws IOException {
        Iglesia iglesia = new Iglesia();
        iglesia.setId(1L);
        iglesia.setUriFoto("foto.jpg");
        when(iglesiaDao.findById(1L)).thenReturn(Optional.of(iglesia));
        org.mockito.Mockito.doThrow(new IOException("disco lleno"))
                .when(fileStorageService).deleteFile(anyString());

        assertThatThrownBy(() -> service.deleteFoto(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("disco lleno");
    }

    @Test
    @DisplayName("deleteFoto: sin foto, no intenta borrar ningun archivo y limpia el campo igual")
    void deleteFoto_sinFoto_noTocaAlmacenamiento() throws IOException {
        Iglesia iglesia = new Iglesia();
        iglesia.setId(1L);
        iglesia.setUriFoto(null);
        when(iglesiaDao.findById(1L)).thenReturn(Optional.of(iglesia));

        service.deleteFoto(1L);

        verify(fileStorageService, never()).deleteFile(anyString());
    }

    // ───────────────────────── updateOrden ─────────────────────────

    @Test
    @DisplayName("updateOrden: asigna el orden 1-indexado segun la posicion en la lista")
    void updateOrden_asignaOrden1Indexado() {
        Iglesia a = new Iglesia();
        a.setId(10L);
        Iglesia b = new Iglesia();
        b.setId(20L);
        when(iglesiaDao.findById(10L)).thenReturn(Optional.of(a));
        when(iglesiaDao.findById(20L)).thenReturn(Optional.of(b));
        when(iglesiaDao.save(any(Iglesia.class))).thenAnswer(inv -> inv.getArgument(0));

        service.updateOrden(List.of(10L, 20L));

        assertThat(a.getOrden()).isEqualTo(1);
        assertThat(b.getOrden()).isEqualTo(2);
    }

    @Test
    @DisplayName("updateOrden: un id inexistente en la lista simplemente se ignora, no interrumpe el resto")
    void updateOrden_idInexistente_seIgnora() {
        Iglesia b = new Iglesia();
        b.setId(20L);
        when(iglesiaDao.findById(999L)).thenReturn(Optional.empty());
        when(iglesiaDao.findById(20L)).thenReturn(Optional.of(b));
        when(iglesiaDao.save(any(Iglesia.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThatCode(() -> service.updateOrden(List.of(999L, 20L))).doesNotThrowAnyException();

        assertThat(b.getOrden()).isEqualTo(2); // conserva su posicion real en la lista, no se recorre
    }
}
