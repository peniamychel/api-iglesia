package com.mcmm.service.impl;

import com.mcmm.exception.NotFoundExceptionResource;
import com.mcmm.model.dao.PlantillaCertificadoRepository;
import com.mcmm.model.dto.plantillaCertificado.PlantillaCertificadoDto;
import com.mcmm.model.entity.PlantillaCertificado;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PlantillaCertificadoServiceImplTest {

    @Mock private PlantillaCertificadoRepository plantillaCertificadoRepository;
    @Mock private ModelMapper modelMapper;
    @Mock private FileStorageService fileStorageService;

    private PlantillaCertificadoServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PlantillaCertificadoServiceImpl(plantillaCertificadoRepository, modelMapper, fileStorageService);
    }

    @Test
    @DisplayName("save: fuerza estado=true sin importar lo que traiga el DTO")
    void save_fuerzaEstadoTrue() {
        PlantillaCertificado mapeada = new PlantillaCertificado();
        mapeada.setEstado(false); // el DTO trae false
        when(modelMapper.map(any(PlantillaCertificadoDto.class), eq(PlantillaCertificado.class))).thenReturn(mapeada);
        when(plantillaCertificadoRepository.save(any(PlantillaCertificado.class))).thenAnswer(inv -> inv.getArgument(0));

        PlantillaCertificado resultado = service.save(PlantillaCertificadoDto.builder().estado(false).build());

        assertThat(resultado.getEstado()).isTrue();
    }

    @Test
    @DisplayName("update: solo actualiza nombre y configuracionJson, preserva el estado existente")
    void update_preservaEstado() {
        PlantillaCertificado existente = new PlantillaCertificado();
        existente.setId(1L);
        existente.setEstado(false);
        when(plantillaCertificadoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(plantillaCertificadoRepository.save(any(PlantillaCertificado.class))).thenAnswer(inv -> inv.getArgument(0));

        PlantillaCertificadoDto dto = PlantillaCertificadoDto.builder()
                .nombre("Nueva").configuracionJson("{}").estado(true).build(); // estado=true en el DTO, se ignora
        PlantillaCertificado resultado = service.update(dto, 1L);

        assertThat(resultado.getNombre()).isEqualTo("Nueva");
        assertThat(resultado.getConfiguracionJson()).isEqualTo("{}");
        assertThat(resultado.getEstado()).isFalse();
    }

    @Test
    @DisplayName("update: id inexistente lanza NotFoundExceptionResource")
    void update_idInexistente_lanzaNotFound() {
        when(plantillaCertificadoRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(PlantillaCertificadoDto.builder().build(), 404L))
                .isInstanceOf(NotFoundExceptionResource.class);
    }

    @Test
    @DisplayName("findById: id inexistente lanza NotFoundExceptionResource")
    void findById_idInexistente_lanzaNotFound() {
        when(plantillaCertificadoRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(404L)).isInstanceOf(NotFoundExceptionResource.class);
    }

    @Test
    @DisplayName("delete: id inexistente lanza NotFoundExceptionResource, no llama delete")
    void delete_idInexistente_lanzaNotFound() {
        when(plantillaCertificadoRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(404L)).isInstanceOf(NotFoundExceptionResource.class);
        verify(plantillaCertificadoRepository, org.mockito.Mockito.never()).delete(any());
    }

    @Test
    @DisplayName("changeState: invierte el estado actual")
    void changeState_invierteElEstado() {
        PlantillaCertificado existente = new PlantillaCertificado();
        existente.setId(1L);
        existente.setEstado(true);
        when(plantillaCertificadoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(plantillaCertificadoRepository.save(any(PlantillaCertificado.class))).thenAnswer(inv -> inv.getArgument(0));

        PlantillaCertificado resultado = service.changeState(1L);

        assertThat(resultado.getEstado()).isFalse();
    }
}
