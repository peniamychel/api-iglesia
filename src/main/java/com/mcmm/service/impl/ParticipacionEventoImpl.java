package com.mcmm.service.impl;

import com.mcmm.exception.BadRequestException;
import com.mcmm.exception.InternalServerErrorExceptionResource;
import com.mcmm.exception.NotFoundExceptionResource;
import com.mcmm.model.dto.participacionEvento.ParticipacionEventoDto;
import com.mcmm.model.dto.participacionEvento.RegistroEntregaDto;
import com.mcmm.model.entity.ParticipacionEvento;
import com.mcmm.model.entity.Certificado;
import com.mcmm.model.entity.Miembro;
import com.mcmm.model.entity.Evento;
import com.mcmm.model.entity.Usuario;
import com.mcmm.model.dao.ParticipacionEventoDao;
import com.mcmm.model.dao.CertificadoDao;
import com.mcmm.model.dao.MiembroDao;
import com.mcmm.model.dao.EventoDao;
import com.mcmm.model.dao.UsuarioDao;
import com.mcmm.service.IParticipacionEvento;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.dao.DataIntegrityViolationException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
@RequiredArgsConstructor
public class ParticipacionEventoImpl implements IParticipacionEvento {

    private final ParticipacionEventoDao participacionEventoDao;
    private final CertificadoDao certificadoDao;
    private final MiembroDao miembroDao;
    private final EventoDao eventoDao;
    private final UsuarioDao usuarioDao;
    private final ModelMapper modelMapper;

    /**
     * Alfabeto y longitud del código de verificación que viaja en el QR.
     *
     * Se excluyen los caracteres que se confunden al leerlos o tipearlos de un
     * papel: 0 con O y 1 con I. Quedan 32 símbolos, es decir 32^4 = 1.048.576
     * códigos posibles, de sobra para el volumen previsto; los choques que
     * puedan darse los resuelve generarCodigoUnico() antes de insertar.
     */
    private static final String ALFABETO_CODIGO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int LONGITUD_CODIGO = 4;
    private static final int MAX_INTENTOS_CODIGO = 25;
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Devuelve un código de verificación que no exista todavía en la base.
     *
     * Antes se generaba en el @PrePersist de la entidad sin comprobar nada: si el
     * código repetía, el índice único rechazaba el INSERT y el usuario recibía un
     * error al registrar la participación. Aquí se consulta antes de asignarlo y
     * se reintenta; si el espacio de códigos empieza a saturarse se agrega un
     * carácter más, de modo que la generación nunca se queda sin salida.
     */
    private String generarCodigoUnico() {
        int longitud = LONGITUD_CODIGO;

        for (int intento = 1; intento <= MAX_INTENTOS_CODIGO; intento++) {
            String candidato = codigoAleatorio(longitud);
            if (!participacionEventoDao.existsByCodigoUnico(candidato)) {
                return candidato;
            }
            // Varios choques seguidos indican que quedan pocos códigos libres de
            // esta longitud: se amplía en un carácter (multiplica por 36 el espacio).
            if (intento % 5 == 0) {
                longitud++;
            }
        }

        throw new InternalServerErrorExceptionResource(
                "No se pudo generar un código de verificación único para el certificado. Intente nuevamente.");
    }

    /**
     * Token del QR: 12 bytes aleatorios (96 bits) en Base64-URL, 16 caracteres.
     *
     * Se acorta a propósito: cada carácter de la URL suma módulos al dibujo del
     * QR y lo vuelve más denso al imprimirlo. 96 bits siguen siendo inalcanzables
     * por fuerza bruta — son 7,9 × 10^28 combinaciones contra un endpoint que
     * además está limitado en intentos.
     */
    private static final int BYTES_TOKEN = 12;

    private String generarToken() {
        byte[] bytes = new byte[BYTES_TOKEN];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String codigoAleatorio(int longitud) {
        StringBuilder sb = new StringBuilder(longitud);
        for (int i = 0; i < longitud; i++) {
            sb.append(ALFABETO_CODIGO.charAt(RANDOM.nextInt(ALFABETO_CODIGO.length())));
        }
        return sb.toString();
    }

    private Long getCurrentIglesiaId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getDetails() instanceof Map) {
            Map<?, ?> details = (Map<?, ?>) authentication.getDetails();
            Object iglesiaIdObj = details.get("iglesiaId");
            if (iglesiaIdObj instanceof Long) {
                return (Long) iglesiaIdObj;
            } else if (iglesiaIdObj instanceof Integer) {
                return ((Integer) iglesiaIdObj).longValue();
            }
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipacionEventoDto> findAll() {
        Long iglesiaId = getCurrentIglesiaId();
        List<ParticipacionEvento> participaciones;
        if (iglesiaId != null) {
            participaciones = participacionEventoDao.findByEventoIglesiaIdWithRelations(iglesiaId);
        } else {
            participaciones = participacionEventoDao.findAllWithRelations();
        }
        return participaciones.stream()
                .map(this::buildDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ParticipacionEventoDto findById(Long id) {
        ParticipacionEvento participacion = participacionEventoDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("ParticipacionEvento", "id", id));
        return buildDto(participacion);
    }

    @Override
    @Transactional
    public ParticipacionEventoDto create(ParticipacionEventoDto participacionEventoDto) {
        if (participacionEventoDto.getEventoId() != null && participacionEventoDto.getMiembroId() != null) {
            if (participacionEventoDao.existsByEventoIdAndMiembroId(participacionEventoDto.getEventoId(), participacionEventoDto.getMiembroId())) {
                throw new IllegalArgumentException("Esta persona ya está registrada como participante en este evento.");
            }
        }

        ParticipacionEvento participacion = modelMapper.map(participacionEventoDto, ParticipacionEvento.class);
        if (participacionEventoDto.getCertificadoId() != null) {
            Certificado certificado = certificadoDao.findById(participacionEventoDto.getCertificadoId()).orElse(null);
            participacion.setCertificado(certificado);
        }
        if (participacionEventoDto.getMiembroId() != null) {
            Miembro miembro = miembroDao.findById(participacionEventoDto.getMiembroId()).orElse(null);
            participacion.setMiembro(miembro);
        }
        if (participacionEventoDto.getEventoId() != null) {
            Evento evento = eventoDao.findById(participacionEventoDto.getEventoId()).orElse(null);
            participacion.setEvento(evento);
        }

        // El código se asigna aquí, ya verificado contra los existentes; el
        // @PrePersist de la entidad queda solo como red de seguridad.
        if (participacion.getCodigoUnico() == null || participacion.getCodigoUnico().isBlank()) {
            participacion.setCodigoUnico(generarCodigoUnico());
        }
        // Token del QR: con 128 bits aleatorios la colisión es despreciable, no
        // hace falta consultarlo previamente.
        if (participacion.getTokenVerificacion() == null || participacion.getTokenVerificacion().isBlank()) {
            participacion.setTokenVerificacion(generarToken());
        }

        ParticipacionEvento saved;
        try {
            // saveAndFlush para que un choque salga aquí y no al cerrar la transacción
            saved = participacionEventoDao.saveAndFlush(participacion);
        } catch (DataIntegrityViolationException e) {
            // Carrera entre la comprobación y el INSERT: dos registros simultáneos
            // que sacaron el mismo código. La transacción ya no se puede reutilizar,
            // así que se informa con un mensaje claro en vez de un error genérico.
            throw new BadRequestException(
                    "El código de verificación generado ya estaba en uso. Vuelva a registrar la participación.");
        }

        return buildDto(saved);
    }

    @Override
    @Transactional
    public ParticipacionEventoDto update(ParticipacionEventoDto participacionEventoDto) {
        ParticipacionEvento participacion = participacionEventoDao.findById(participacionEventoDto.getId())
                .orElseThrow(() -> new NotFoundExceptionResource("ParticipacionEvento", "id", participacionEventoDto.getId()));
        participacion.setEstado(participacionEventoDto.getEstado());
        participacion.setEntregado(participacionEventoDto.getEntregado());
        participacion.setFechaEntrega(participacionEventoDto.getFechaEntrega());
        if (participacionEventoDto.getCertificadoId() != null) {
            Certificado certificado = certificadoDao.findById(participacionEventoDto.getCertificadoId()).orElse(null);
            participacion.setCertificado(certificado);
        } else {
            participacion.setCertificado(null);
        }
        if (participacionEventoDto.getMiembroId() != null) {
            Miembro miembro = miembroDao.findById(participacionEventoDto.getMiembroId()).orElse(null);
            participacion.setMiembro(miembro);
        }
        if (participacionEventoDto.getEventoId() != null) {
            Evento evento = eventoDao.findById(participacionEventoDto.getEventoId()).orElse(null);
            participacion.setEvento(evento);
        }
        if (participacionEventoDto.getEntregadoPorId() != null) {
            Usuario usuario = usuarioDao.findById(participacionEventoDto.getEntregadoPorId()).orElse(null);
            participacion.setEntregadoPor(usuario);
        }
        ParticipacionEvento saved = participacionEventoDao.save(participacion);
        return buildDto(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ParticipacionEvento participacion = participacionEventoDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("ParticipacionEvento", "id", id));
        participacionEventoDao.delete(participacion);
    }

    @Override
    @Transactional
    public void estado(Long id) {
        participacionEventoDao.toggleEstado(id);
    }

    @Override
    @Transactional
    public void toggleEntregado(Long id, String username) {
        ParticipacionEvento participacion = participacionEventoDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("ParticipacionEvento", "id", id));

        Usuario usuario = usuarioDao.findByUsername(username)
                .orElseThrow(() -> new NotFoundExceptionResource("Usuario", "username", username));

        if (participacion.getEntregado() == null || !participacion.getEntregado()) {
            participacion.setEntregado(true);
            participacion.setFechaEntrega(LocalDateTime.now());
            participacion.setEntregadoPor(usuario);
        } else {
            participacion.setEntregado(false);
            participacion.setFechaEntrega(null);
            participacion.setEntregadoPor(null);
        }

        participacionEventoDao.save(participacion);
    }

    @Override
    @Transactional
    public void toggleEntregadoWithCertificado(Long id, Long certificadoId, String username) {
        ParticipacionEvento participacion = participacionEventoDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("ParticipacionEvento", "id", id));

        Usuario usuario = usuarioDao.findByUsername(username)
                .orElseThrow(() -> new NotFoundExceptionResource("Usuario", "username", username));

        Certificado certificado = certificadoDao.findById(certificadoId)
                .orElseThrow(() -> new NotFoundExceptionResource("Certificado", "id", certificadoId));

        // Siempre marcar como entregado y asociar el certificado al imprimir/entregar, no alternar (toggle)
        participacion.setEntregado(true);
        if (participacion.getFechaEntrega() == null) {
            participacion.setFechaEntrega(LocalDateTime.now());
        }
        if (participacion.getEntregadoPor() == null) {
            participacion.setEntregadoPor(usuario);
        }
        participacion.setCertificado(certificado);

        participacionEventoDao.save(participacion);
    }

    /**
     * Asienta la entrega del certificado: guarda libro y folio, marca la
     * participación como entregada y la vincula al certificado emitido. Es el
     * único punto donde se registra una entrega, y ocurre al generar el PDF.
     */
    @Override
    @Transactional
    public ParticipacionEventoDto registrarEntrega(Long id, RegistroEntregaDto registro, String username) {
        ParticipacionEvento participacion = participacionEventoDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("ParticipacionEvento", "id", id));

        Usuario usuario = usuarioDao.findByUsername(username)
                .orElseThrow(() -> new NotFoundExceptionResource("Usuario", "username", username));

        Certificado certificado = certificadoDao.findById(registro.getCertificadoId())
                .orElseThrow(() -> new NotFoundExceptionResource("Certificado", "id", registro.getCertificadoId()));

        participacion.setNumeroLibro(registro.getNumeroLibro().trim());
        participacion.setNumeroFolio(registro.getNumeroFolio().trim());
        participacion.setCertificado(certificado);
        participacion.setEntregado(true);

        // La primera entrega es la que queda registrada: una reimpresión no
        // reescribe quién ni cuándo se entregó.
        if (participacion.getFechaEntrega() == null) {
            participacion.setFechaEntrega(LocalDateTime.now());
        }
        if (participacion.getEntregadoPor() == null) {
            participacion.setEntregadoPor(usuario);
        }

        return buildDto(participacionEventoDao.save(participacion));
    }

    private ParticipacionEventoDto buildDto(ParticipacionEvento participacion) {
        ParticipacionEventoDto dto = modelMapper.map(participacion, ParticipacionEventoDto.class);
        if (participacion.getCertificado() != null) {
            dto.setCertificadoId(participacion.getCertificado().getId());
        }
        if (participacion.getMiembro() != null) {
            dto.setMiembroId(participacion.getMiembro().getId());
            dto.setMiembroDto(modelMapper.map(participacion.getMiembro(), com.mcmm.model.dto.MiembroDto.MiembroDto.class));
        }
        if (participacion.getEvento() != null) {
            dto.setEventoId(participacion.getEvento().getId());
        }
        if (participacion.getEntregadoPor() != null) {
            dto.setEntregadoPorId(participacion.getEntregadoPor().getId());
        }
        return dto;
    }
}
