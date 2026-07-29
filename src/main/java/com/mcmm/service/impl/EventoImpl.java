package com.mcmm.service.impl;

import com.mcmm.model.dto.evento.EventoDto;
import com.mcmm.model.entity.Evento;
import com.mcmm.model.entity.TipoEvento;
import com.mcmm.model.entity.Certificado;
import com.mcmm.model.dao.EventoDao;
import com.mcmm.model.dao.TipoEventoDao;
import com.mcmm.model.dao.IglesiaDao;
import com.mcmm.model.dao.CertificadoDao;
import com.mcmm.model.dao.ParticipacionEventoDao;
import com.mcmm.model.dao.EventoAceptacionDao;
import com.mcmm.exception.BadRequestException;
import com.mcmm.exception.NotFoundExceptionResource;
import com.mcmm.service.IEvento;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.mcmm.model.dao.ResponsableEventoDao;
import com.mcmm.model.dao.UsuarioDao;
import com.mcmm.model.entity.ResponsableEvento;
import com.mcmm.model.entity.Usuario;
import com.mcmm.model.entity.Cargo;
import lombok.extern.slf4j.Slf4j;
import java.util.Optional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class EventoImpl implements IEvento {

    private final EventoDao eventoDao;
    private final TipoEventoDao tipoEventoDao;
    private final IglesiaDao iglesiaDao;
    private final ResponsableEventoDao responsableEventoDao;
    private final UsuarioDao usuarioDao;
    private final CertificadoDao certificadoDao;
    private final ParticipacionEventoDao participacionEventoDao;
    private final EventoAceptacionDao eventoAceptacionDao;
    private final ModelMapper modelMapper = new ModelMapper();

    public EventoImpl(EventoDao eventoDao, TipoEventoDao tipoEventoDao, IglesiaDao iglesiaDao,
                      ResponsableEventoDao responsableEventoDao, UsuarioDao usuarioDao,
                      CertificadoDao certificadoDao, ParticipacionEventoDao participacionEventoDao,
                      EventoAceptacionDao eventoAceptacionDao) {
        this.eventoDao = eventoDao;
        this.tipoEventoDao = tipoEventoDao;
        this.iglesiaDao = iglesiaDao;
        this.responsableEventoDao = responsableEventoDao;
        this.usuarioDao = usuarioDao;
        this.certificadoDao = certificadoDao;
        this.participacionEventoDao = participacionEventoDao;
        this.eventoAceptacionDao = eventoAceptacionDao;
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
    public List<EventoDto> findAll() {
        Long iglesiaId = getCurrentIglesiaId();
        List<Evento> eventos = (iglesiaId != null)
                ? eventoDao.findEventosParaIglesia(iglesiaId)
                : eventoDao.findNoArchivados();
        return toDtosConCertificado(eventos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventoDto> findArchivados() {
        Long iglesiaId = getCurrentIglesiaId();
        List<Evento> eventos = (iglesiaId != null)
                ? eventoDao.findEventosArchivadosParaIglesia(iglesiaId)
                : eventoDao.findArchivadosTodos();
        return toDtosConCertificado(eventos);
    }

    /** Mapea la lista poblando tieneCertificado en batch (una sola consulta, sin N+1). */
    private List<EventoDto> toDtosConCertificado(List<Evento> eventos) {
        List<Long> ids = eventos.stream().map(Evento::getId).collect(Collectors.toList());
        java.util.Set<Long> conCert = ids.isEmpty()
                ? java.util.Collections.emptySet()
                : new java.util.HashSet<>(certificadoDao.findEventoIdsConCertificado(ids));
        return eventos.stream()
                .map(evento -> {
                    EventoDto dto = toDto(evento);
                    dto.setTieneCertificado(conCert.contains(evento.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /** Mapea un Evento a DTO resolviendo tipoEventoId e iglesiaId. */
    private EventoDto toDto(Evento evento) {
        EventoDto dto = modelMapper.map(evento, EventoDto.class);
        if (evento.getTipoEvento() != null) {
            dto.setTipoEventoId(evento.getTipoEvento().getId());
        }
        if (evento.getIglesia() != null) {
            dto.setIglesiaId(evento.getIglesia().getId());
        }
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public EventoDto findById(Long id) {
        Evento evento = eventoDao.findById(id).orElse(null);
        if (evento == null) return null;
        EventoDto dto = toDto(evento);
        dto.setTieneCertificado(certificadoDao.existsByEventoId(id));
        return dto;
    }

    @Override
    public EventoDto create(EventoDto eventoDto) {
        Evento evento = modelMapper.map(eventoDto, Evento.class);
        if (eventoDto.getTipoEventoId() != null) {
            TipoEvento tipoEvento = tipoEventoDao.findById(eventoDto.getTipoEventoId()).orElse(null);
            evento.setTipoEvento(tipoEvento);
        }
        
        Long iglesiaId = getCurrentIglesiaId();
        if (iglesiaId != null) {
            evento.setIglesia(iglesiaDao.findById(iglesiaId).orElse(null));
        } else if (eventoDto.getIglesiaId() != null) {
            evento.setIglesia(iglesiaDao.findById(eventoDto.getIglesiaId()).orElse(null));
        }

        Evento savedEvento = eventoDao.save(evento);

        // Asignación automática de responsable de evento para roles locales (no ADMIN)
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                boolean isAdmin = auth.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                if (!isAdmin) {
                    String username = auth.getName();
                    Optional<Usuario> optUser = usuarioDao.findByUsername(username);
                    if (optUser.isPresent() && optUser.get().getMiembro() != null) {
                        Usuario user = optUser.get();
                        Cargo activeCargo = null;
                        
                        // Buscar primero cargo activo de esta iglesia
                        if (iglesiaId != null && user.getMiembro().getCargos() != null) {
                            for (Cargo cargo : user.getMiembro().getCargos()) {
                                if (cargo.getEstado() != null && cargo.getEstado() && 
                                    cargo.getIglesia() != null && cargo.getIglesia().getId().equals(iglesiaId)) {
                                    activeCargo = cargo;
                                    break;
                                }
                            }
                        }
                        
                        // Si no se encontró, buscar cualquier cargo activo
                        if (activeCargo == null && user.getMiembro().getCargos() != null) {
                            for (Cargo cargo : user.getMiembro().getCargos()) {
                                if (cargo.getEstado() != null && cargo.getEstado()) {
                                    activeCargo = cargo;
                                    break;
                                }
                            }
                        }
                        
                        if (activeCargo != null) {
                            ResponsableEvento responsable = new ResponsableEvento();
                            responsable.setEvento(savedEvento);
                            responsable.setCargo(activeCargo);
                            responsable.setEstado(true);
                            responsable.setCreatedAt(java.time.LocalDateTime.now());
                            responsable.setUpdatedAt(java.time.LocalDateTime.now());
                            responsableEventoDao.save(responsable);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error al asignar responsable automatico: {}", e.getMessage());
        }

        // Generación automática del certificado del evento si se marcó la casilla.
        if (Boolean.TRUE.equals(eventoDto.getGeneraCertificado())) {
            crearCertificadoParaEvento(savedEvento);
        }

        EventoDto dto = toDto(savedEvento);
        dto.setTieneCertificado(certificadoDao.existsByEventoId(savedEvento.getId()));
        return dto;
    }

    /**
     * Crea el certificado asociado a un evento (sin plantilla, listo para diseñar después).
     * El motivo se toma del evento (motivo o, si está vacío, el nombre). No aborta la operación
     * principal si algo falla.
     */
    private void crearCertificadoParaEvento(Evento evento) {
        try {
            String motivo = evento.getMotivo();
            if (motivo == null || motivo.isBlank()) {
                motivo = evento.getNombre();
            }
            if (motivo != null && motivo.length() > 254) {
                motivo = motivo.substring(0, 254);
            }
            Certificado certificado = new Certificado();
            certificado.setEvento(evento);
            certificado.setMotivoCertificado(motivo);
            certificado.setEstado(true);
            certificadoDao.save(certificado);
        } catch (Exception e) {
            log.warn("Error al generar el certificado automatico del evento {}: {}", evento.getId(), e.getMessage());
        }
    }

    @Override
    public EventoDto update(EventoDto eventoDto) {
        // Se carga la fila existente y se copian SOLO los campos editables: así los
        // campos que no viajan en el request (archivado, createdAt) conservan su valor
        // en vez de quedar en null (un evento archivado "reaparecía" al editarlo).
        Evento evento = eventoDao.findById(eventoDto.getId())
                .orElseThrow(() -> new NotFoundExceptionResource("Evento", "id", eventoDto.getId()));

        evento.setNombre(eventoDto.getNombre());
        evento.setMotivo(eventoDto.getMotivo());
        evento.setUbicacion(eventoDto.getUbicacion());
        evento.setLocalidad(eventoDto.getLocalidad());
        evento.setProvincia(eventoDto.getProvincia());
        evento.setDepartamento(eventoDto.getDepartamento());
        evento.setFechaInicio(eventoDto.getFechaInicio());
        evento.setFechaFin(eventoDto.getFechaFin());
        evento.setAlcance(eventoDto.getAlcance());
        evento.setMostrarEnCalendario(eventoDto.getMostrarEnCalendario());
        evento.setHabilitarInscripciones(eventoDto.getHabilitarInscripciones());
        evento.setIglesiasInvitadas(eventoDto.getIglesiasInvitadas());
        // estado solo se pisa si viene en el request; archivado se maneja únicamente
        // por los endpoints archivar/desarchivar.
        if (eventoDto.getEstado() != null) {
            evento.setEstado(eventoDto.getEstado());
        }

        if (eventoDto.getTipoEventoId() != null) {
            TipoEvento tipoEvento = tipoEventoDao.findById(eventoDto.getTipoEventoId()).orElse(null);
            evento.setTipoEvento(tipoEvento);
        }

        if (eventoDto.getIglesiaId() != null) {
            evento.setIglesia(iglesiaDao.findById(eventoDto.getIglesiaId()).orElse(null));
        } else {
            Long iglesiaId = getCurrentIglesiaId();
            if (iglesiaId != null) {
                evento.setIglesia(iglesiaDao.findById(iglesiaId).orElse(null));
            }
        }

        Evento updatedEvento = eventoDao.save(evento);

        // En edición solo se puede AÑADIR certificado: si se marcó la casilla y el evento aún no
        // tiene certificado, se crea. Nunca se quita uno existente.
        boolean yaTieneCertificado = certificadoDao.existsByEventoId(updatedEvento.getId());
        if (Boolean.TRUE.equals(eventoDto.getGeneraCertificado()) && !yaTieneCertificado) {
            crearCertificadoParaEvento(updatedEvento);
            yaTieneCertificado = true;
        }

        EventoDto dto = toDto(updatedEvento);
        dto.setTieneCertificado(yaTieneCertificado);
        return dto;
    }

    @Override
    public void delete(Long id) {
        // Un evento solo se puede eliminar si no tiene certificados ni participantes.
        if (certificadoDao.existsByEventoId(id)) {
            throw new BadRequestException("No se puede eliminar el evento porque tiene certificados asociados. Puedes archivarlo en su lugar.");
        }
        if (participacionEventoDao.existsByEventoId(id)) {
            throw new BadRequestException("No se puede eliminar el evento porque tiene participantes registrados. Puedes archivarlo en su lugar.");
        }
        // Limpiar registros de apoyo (no cuentan como certificados ni participantes) para
        // evitar violaciones de llave foránea: decisiones de invitación y responsables.
        eventoAceptacionDao.deleteByEventoId(id);
        responsableEventoDao.deleteByEventoId(id);
        eventoDao.deleteById(id);
    }

    @Override
    public void archivar(Long id) {
        eventoDao.setArchivado(id, true);
    }

    @Override
    public void desarchivar(Long id) {
        eventoDao.setArchivado(id, false);
    }

    @Override
    public void estado(Long id) {
        eventoDao.toggleEstado(id);
    }

    @Override
    public void cloneYearEvents(int fromYear, int toYear) {
        Calendar cal = Calendar.getInstance();
        cal.set(fromYear, Calendar.JANUARY, 1, 0, 0, 0);
        Date start = cal.getTime();
        cal.set(fromYear, Calendar.DECEMBER, 31, 23, 59, 59);
        Date end = cal.getTime();

        List<Evento> eventsFromYear = eventoDao.findByFechaInicioBetween(start, end);
        int yearDiff = toYear - fromYear;

        for (Evento original : eventsFromYear) {
            if (Boolean.TRUE.equals(original.getMostrarEnCalendario()) || "GENERAL".equalsIgnoreCase(original.getAlcance())) {
                Evento cloned = Evento.builder()
                        .nombre(original.getNombre())
                        .motivo(original.getMotivo())
                        .ubicacion(original.getUbicacion())
                        .localidad(original.getLocalidad())
                        .provincia(original.getProvincia())
                        .departamento(original.getDepartamento())
                        .tipoEvento(original.getTipoEvento())
                        .iglesia(original.getIglesia())
                        .alcance(original.getAlcance())
                        .mostrarEnCalendario(original.getMostrarEnCalendario())
                        .estado(true)
                        .build();

                if (original.getFechaInicio() != null) {
                    cal.setTime(original.getFechaInicio());
                    cal.add(Calendar.YEAR, yearDiff);
                    cloned.setFechaInicio(cal.getTime());
                }
                if (original.getFechaFin() != null) {
                    cal.setTime(original.getFechaFin());
                    cal.add(Calendar.YEAR, yearDiff);
                    cloned.setFechaFin(cal.getTime());
                }

                eventoDao.save(cloned);
            }
        }
    }
}