package com.mcmm.service.impl;

import com.mcmm.exception.NotFoundExceptionResource;
import com.mcmm.model.dao.CertificadoDao;
import com.mcmm.model.dao.EventoDao;
import com.mcmm.model.dao.ParticipacionEventoDao;
import com.mcmm.model.dao.PlantillaCertificadoRepository;
import com.mcmm.model.entity.Certificado;
import com.mcmm.model.entity.PlantillaCertificado;
import com.mcmm.service.FileStorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.modelmapper.ModelMapper;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test de delete(): borra en un orden que importa (desvincular participaciones
 * y quitar la referencia a la plantilla ANTES de borrar el certificado, para no
 * chocar con las llaves foraneas), y solo despues borra la plantilla asociada.
 *
 * La plantilla ya no guarda imagenes (logo/marca de agua/firma se retiraron
 * hoy), asi que delete() ya no intenta borrar archivos de la plantilla — solo
 * el registro. Ese es el cambio que motiva este test.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CertificadoImplTest {

    @Mock private CertificadoDao certificadoDao;
    @Mock private EventoDao eventoDao;
    @Mock private PlantillaCertificadoRepository plantillaCertificadoDao;
    @Mock private ParticipacionEventoDao participacionEventoDao;
    @Mock private ModelMapper modelMapper;
    @Mock private FileStorageService fileStorageService;

    private CertificadoImpl service;

    private CertificadoImpl newService() {
        return new CertificadoImpl(certificadoDao, eventoDao, plantillaCertificadoDao,
                participacionEventoDao, modelMapper, fileStorageService);
    }

    @Test
    @DisplayName("Id inexistente: NotFoundExceptionResource, sin tocar nada mas")
    void delete_idInexistente_lanzaNotFound() {
        service = newService();
        when(certificadoDao.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(404L))
                .isInstanceOf(NotFoundExceptionResource.class);

        verify(certificadoDao, never()).delete(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Con foto: borra el archivo antes de eliminar el certificado")
    void delete_conFoto_borraElArchivo() throws IOException {
        service = newService();
        Certificado certificado = new Certificado();
        certificado.setId(1L);
        certificado.setUriFoto("cert1.jpg");
        when(certificadoDao.findById(1L)).thenReturn(Optional.of(certificado));

        service.delete(1L);

        verify(fileStorageService).deleteFile("certificados/cert1.jpg");
        verify(certificadoDao).delete(certificado);
    }

    @Test
    @DisplayName("Sin foto: no intenta borrar ningun archivo")
    void delete_sinFoto_noLlamaAlAlmacenamiento() throws IOException {
        service = newService();
        Certificado certificado = new Certificado();
        certificado.setId(2L);
        certificado.setUriFoto(null);
        when(certificadoDao.findById(2L)).thenReturn(Optional.of(certificado));

        service.delete(2L);

        verify(fileStorageService, never()).deleteFile(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    @DisplayName("Error al borrar la foto: se traduce a una excepcion clara, no se pierde silenciosamente")
    void delete_errorAlBorrarFoto_propagaComoRuntimeException() throws IOException {
        service = newService();
        Certificado certificado = new Certificado();
        certificado.setId(3L);
        certificado.setUriFoto("cert3.jpg");
        when(certificadoDao.findById(3L)).thenReturn(Optional.of(certificado));
        org.mockito.Mockito.doThrow(new IOException("disco lleno"))
                .when(fileStorageService).deleteFile("certificados/cert3.jpg");

        assertThatThrownBy(() -> service.delete(3L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("disco lleno");

        // Si fallo borrando la foto, no debe haber intentado borrar el registro.
        verify(certificadoDao, never()).delete(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Con plantilla asociada: desvincula participaciones, luego borra el certificado y recien despues la plantilla")
    void delete_conPlantilla_respetaElOrdenDeBorrado() {
        service = newService();
        PlantillaCertificado plantilla = new PlantillaCertificado();
        plantilla.setId(50L);
        Certificado certificado = new Certificado();
        certificado.setId(4L);
        certificado.setPlantillaCertificado(plantilla);
        when(certificadoDao.findById(4L)).thenReturn(Optional.of(certificado));

        service.delete(4L);

        InOrder orden = inOrder(participacionEventoDao, certificadoDao, plantillaCertificadoDao);
        orden.verify(participacionEventoDao).detachCertificado(4L);
        orden.verify(certificadoDao).delete(certificado);
        orden.verify(plantillaCertificadoDao).delete(plantilla);

        // La referencia se quita antes del borrado para no chocar con la FK.
        assertThatCode(() -> certificado.getPlantillaCertificado()).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Sin plantilla asociada: no intenta borrar ninguna plantilla")
    void delete_sinPlantilla_noBorraPlantilla() {
        service = newService();
        Certificado certificado = new Certificado();
        certificado.setId(5L);
        certificado.setPlantillaCertificado(null);
        when(certificadoDao.findById(5L)).thenReturn(Optional.of(certificado));

        service.delete(5L);

        verify(plantillaCertificadoDao, never()).delete(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Siempre desvincula las participaciones del certificado, tenga o no plantilla")
    void delete_siempreDesvinculaParticipaciones() {
        service = newService();
        Certificado certificado = new Certificado();
        certificado.setId(6L);
        when(certificadoDao.findById(6L)).thenReturn(Optional.of(certificado));

        service.delete(6L);

        verify(participacionEventoDao, times(1)).detachCertificado(6L);
    }
}
