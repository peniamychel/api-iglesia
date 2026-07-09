package com.mcmm.service.impl;

import com.mcmm.exception.BadRequestException;
import com.mcmm.exception.NotFoundExceptionResource;
import com.mcmm.model.dao.MiembroDao;
import com.mcmm.model.dao.AccionDao;
import com.mcmm.model.dao.UsuarioDao;
import com.mcmm.model.dto.AccionDto;
import com.mcmm.model.dto.RolCargoDto;
import com.mcmm.model.dto.usuarioDto.UsuarioChangePasswordDto;
import com.mcmm.model.dto.usuarioDto.UsuarioResetPasswordDto;
import com.mcmm.model.dto.usuarioDto.UsuarioUpdateDto;
import com.mcmm.model.dto.usuarioDto.UsuarioDto;
import com.mcmm.model.dto.usuarioDto.UsuarioDtoRes;
import com.mcmm.model.entity.Miembro;
import com.mcmm.model.entity.Usuario;
import com.mcmm.service.FileStorageService;
import com.mcmm.service.IUsuario;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class UsuarioImpl implements IUsuario {

    private static final String USUARIOS_DIR = "usuarios/";

    private final ModelMapper modelMapper;
    private final UsuarioDao usuarioDao;
    private final PasswordEncoder passwordEncoder;
    private final MiembroDao miembroDao;
    private final FileStorageService fileStorageService;
    private final AccionDao accionDao;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public UsuarioImpl(ModelMapper modelMapper, UsuarioDao usuarioDao,
                       PasswordEncoder passwordEncoder,
                       MiembroDao miembroDao, FileStorageService fileStorageService,
                       AccionDao accionDao) {
        this.modelMapper = modelMapper;
        this.usuarioDao = usuarioDao;
        this.passwordEncoder = passwordEncoder;
        this.miembroDao = miembroDao;
        this.fileStorageService = fileStorageService;
        this.accionDao = accionDao;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioDtoRes> findAll() {
        return StreamSupport.stream(usuarioDao.findAll().spliterator(), false)
                .map(this::buildDtoWithPhotoUrl)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioDtoRes findById(Long id) {
        Usuario usuario = usuarioDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("Usuario", "id", id));
        return buildDtoWithPhotoUrl(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioDtoRes findByUsername(String username) {
        Usuario usuario = usuarioDao.findByUsername(username)
                .orElseThrow(() -> new NotFoundExceptionResource("Usuario", "username", username));
        return buildDtoWithPhotoUrl(usuario);
    }

    @Override
    @Transactional
    public UsuarioDtoRes create(UsuarioDto usuarioDto) {
        if (usuarioDao.existsByUsername(usuarioDto.getUsername())) {
            throw new BadRequestException("El nombre de usuario ya existe");
        }

        if (usuarioDao.existsByEmail(usuarioDto.getEmail())) {
            throw new BadRequestException("El email ya existe");
        }

        if (usuarioDto.getMiembroId() != null && usuarioDao.existsByMiembroIdAndEstadoTrue(usuarioDto.getMiembroId())) {
            throw new BadRequestException("Este miembro ya cuenta con una cuenta de usuario activa.");
        }

        Usuario usuario = modelMapper.map(usuarioDto, Usuario.class);
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        
        if (usuarioDto.getMiembroId() != null) {
            Miembro miembro = miembroDao.findById(usuarioDto.getMiembroId()).orElse(null);
            usuario.setMiembro(miembro);
        }

        Usuario saved = usuarioDao.save(usuario);
        return buildDtoWithPhotoUrl(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Usuario usuario = usuarioDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("Usuario", "id", id));
        
        if (usuario.getUriFoto() != null && !usuario.getUriFoto().isBlank()) {
            String uriFoto = usuario.getUriFoto();
            if (!uriFoto.endsWith("/")) {
                String fileNameOnly = uriFoto.substring(uriFoto.lastIndexOf("/") + 1);
                if (!fileNameOnly.isBlank()) {
                    try {
                        fileStorageService.deleteFile(USUARIOS_DIR + fileNameOnly);
                    } catch (IOException e) {
                        // Logged or handled, continuing deletion process
                    }
                }
            }
        }
        
        usuarioDao.delete(usuario);
    }

    @Override
    @Transactional
    public UsuarioDtoRes updateUser(UsuarioUpdateDto usuarioUpdateDto) {
        Usuario usuario = usuarioDao.findById(usuarioUpdateDto.getId())
                .orElseThrow(() -> new NotFoundExceptionResource("Usuario", "id", usuarioUpdateDto.getId()));

        if (usuarioUpdateDto.getUsername() != null &&
                !usuarioUpdateDto.getUsername().equals(usuario.getUsername()) &&
                usuarioDao.existsByUsername(usuarioUpdateDto.getUsername())) {
            throw new BadRequestException("El nombre de usuario ya está en uso");
        }

        if (usuarioUpdateDto.getEmail() != null &&
                !usuarioUpdateDto.getEmail().equals(usuario.getEmail()) &&
                usuarioDao.existsByEmail(usuarioUpdateDto.getEmail())) {
            throw new BadRequestException("El email ya está en uso");
        }

        if (usuarioUpdateDto.getUsername() != null) {
            usuario.setUsername(usuarioUpdateDto.getUsername());
        }
        if (usuarioUpdateDto.getEmail() != null) {
            usuario.setEmail(usuarioUpdateDto.getEmail());
        }
        
        if (usuarioUpdateDto.getMiembroId() != null) {
            Miembro miembro = miembroDao.findById(usuarioUpdateDto.getMiembroId()).orElse(null);
            usuario.setMiembro(miembro);
            if (miembro != null) {
                usuario.setName(miembro.getNombre());
                usuario.setApellidos(miembro.getApellido());
            }
        } else if (usuarioUpdateDto.getMiembroId() == null) {
            usuario.setMiembro(null);
            if (usuarioUpdateDto.getName() != null) {
                usuario.setName(usuarioUpdateDto.getName());
            }
            if (usuarioUpdateDto.getApellidos() != null) {
                usuario.setApellidos(usuarioUpdateDto.getApellidos());
            }
        }

        if (usuarioUpdateDto.getEstado() != null) {
            usuario.setEstado(usuarioUpdateDto.getEstado());
        }

        Usuario saved = usuarioDao.save(usuario);
        return buildDtoWithPhotoUrl(saved);
    }

    @Override
    @Transactional
    public void changePassword(UsuarioChangePasswordDto usuarioChangePasswordDto, String currentUsername) {
        Usuario usuario = usuarioDao.findById(usuarioChangePasswordDto.getId())
                .orElseThrow(() -> new NotFoundExceptionResource("Usuario", "id", usuarioChangePasswordDto.getId()));

        if (!usuario.getUsername().equals(currentUsername)) {
            throw new AccessDeniedException("Solo puedes cambiar tu propia contraseña");
        }

        if (!passwordEncoder.matches(usuarioChangePasswordDto.getCurrentPassword(), usuario.getPassword())) {
            throw new BadRequestException("La contraseña actual es incorrecta");
        }

        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuarioDao.save(usuario);
    }

    @Override
    @Transactional
    public void resetPassword(UsuarioResetPasswordDto usuarioResetPasswordDto) {
        Usuario usuario = usuarioDao.findById(usuarioResetPasswordDto.getId())
                .orElseThrow(() -> new NotFoundExceptionResource("Usuario", "id", usuarioResetPasswordDto.getId()));

        usuario.setPassword(passwordEncoder.encode(usuarioResetPasswordDto.getNewPassword()));
        usuarioDao.save(usuario);
    }

    @Override
    @Transactional
    public String updateProfilePhoto(Long id, MultipartFile file) throws IOException {
        Usuario usuario = usuarioDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("Usuario", "id", id));

        if (usuario.getUriFoto() != null && !usuario.getUriFoto().isBlank()) {
            String uriFoto = usuario.getUriFoto();
            if (!uriFoto.endsWith("/")) {
                String fileNameOnly = uriFoto.substring(uriFoto.lastIndexOf("/") + 1);
                if (!fileNameOnly.isBlank()) {
                    fileStorageService.deleteFile(USUARIOS_DIR + fileNameOnly);
                }
            }
        }

        String fileName = fileStorageService.storeFile(file, usuario.getUsername(), USUARIOS_DIR);

        String fileUrl = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/uploads/")
                .path(USUARIOS_DIR)
                .path(fileName)
                .toUriString();
        usuario.setUriFoto(fileName);
        usuarioDao.save(usuario);
        return fileUrl;
    }

    @Override
    @Transactional
    public void deleteProfilePhoto(Long id) {
        Usuario usuario = usuarioDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("Usuario", "id", id));

        if (usuario.getUriFoto() != null && !usuario.getUriFoto().isBlank()) {
            String uriFoto = usuario.getUriFoto();
            if (!uriFoto.endsWith("/")) {
                String fileNameOnly = uriFoto.substring(uriFoto.lastIndexOf("/") + 1);
                if (!fileNameOnly.isBlank()) {
                    try {
                        fileStorageService.deleteFile(USUARIOS_DIR + fileNameOnly);
                    } catch (IOException e) {
                        throw new RuntimeException("Error al eliminar la foto: " + e.getMessage());
                    }
                }
            }
        }

        usuario.setUriFoto(null);
        usuarioDao.save(usuario);
    }

    private UsuarioDtoRes buildDtoWithPhotoUrl(Usuario usuario) {
        UsuarioDtoRes dto = modelMapper.map(usuario, UsuarioDtoRes.class);
        Set<AccionDto> accionDtos = new HashSet<>();
        
        if (usuario.getMiembro() != null) {
            dto.setMiembroId(usuario.getMiembro().getId());
            dto.setName(usuario.getMiembro().getNombre());
            dto.setApellidos(usuario.getMiembro().getApellido());
            
            if (usuario.getMiembro().getUriFoto() != null) {
                String fileUrl = ServletUriComponentsBuilder
                        .fromCurrentContextPath()
                        .path("/uploads/")
                        .path("miembros/")
                        .path(usuario.getMiembro().getUriFoto())
                        .toUriString();
                dto.setUriFoto(fileUrl);
            } else {
                dto.setUriFoto(null);
            }
            
            if (usuario.getMiembro().getMiembroIglesias() != null) {
                String iglesiaName = usuario.getMiembro().getMiembroIglesias().stream()
                        .filter(mi -> Boolean.TRUE.equals(mi.getEstado()))
                        .map(mi -> mi.getIglesia() != null ? mi.getIglesia().getNombre() : null)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(null);
                dto.setIglesiaNombre(iglesiaName);
            }

            if (usuario.getMiembro().getCargos() != null) {
                Date now = new Date();
                Set<com.mcmm.model.dto.RolCargoDto> rolesDtos = usuario.getMiembro().getCargos().stream()
                        .filter(c -> Boolean.TRUE.equals(c.getEstado()) && (c.getFechaFin() == null || c.getFechaFin().after(now)))
                        .filter(c -> c.getRolCargo() != null)
                        .map(c -> {
                            if (c.getRolCargo().getAcciones() != null) {
                                c.getRolCargo().getAcciones().forEach(a -> {
                                    AccionDto aDto = modelMapper.map(a, AccionDto.class);
                                    aDto.setAuthorityCode(a.getAuthorityCode());
                                    if (a.getServicio() != null) {
                                        aDto.setServicioId(a.getServicio().getId());
                                        aDto.setServicioCodigo(a.getServicio().getCodigo());
                                    }
                                    accionDtos.add(aDto);
                                });
                            }
                            RolCargoDto rcDto = modelMapper.map(c.getRolCargo(), RolCargoDto.class);
                            return rcDto;
                        })
                        .collect(Collectors.toSet());
                dto.setRoles(rolesDtos);
            }
        } else {
            dto.setUriFoto(null);
            dto.setIglesiaNombre("Administración Central");
            com.mcmm.model.dto.RolCargoDto adminDto = com.mcmm.model.dto.RolCargoDto.builder()
                    .nombre("Administrador Global")
                    .nombreRol("ADMIN")
                    .estado(true)
                    .build();
            dto.setRoles(Collections.singleton(adminDto));

            accionDao.findAll().forEach(a -> {
                AccionDto aDto = modelMapper.map(a, AccionDto.class);
                aDto.setAuthorityCode(a.getAuthorityCode());
                if (a.getServicio() != null) {
                    aDto.setServicioId(a.getServicio().getId());
                    aDto.setServicioCodigo(a.getServicio().getCodigo());
                }
                accionDtos.add(aDto);
            });
        }

        dto.setAcciones(accionDtos);
        return dto;
    }
}
