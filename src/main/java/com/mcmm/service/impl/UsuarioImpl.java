package com.mcmm.service.impl;

import com.mcmm.exception.BadRequestException;
import com.mcmm.exception.NotFoundExceptionResource;
import com.mcmm.model.dao.RolDao;
import com.mcmm.model.dao.UsuarioDao;
import com.mcmm.model.dto.usuarioDto.UsuarioChangePasswordDto;
import com.mcmm.model.dto.usuarioDto.UsuarioResetPasswordDto;
import com.mcmm.model.dto.usuarioDto.UsuarioUpdateDto;
import com.mcmm.model.dto.usuarioDto.UsuarioDto;
import com.mcmm.model.dto.usuarioDto.UsuarioDtoRes;
import com.mcmm.model.entity.ERole;
import com.mcmm.model.entity.Rol;
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
    private final RolDao rolDao;
    private final FileStorageService fileStorageService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public UsuarioImpl(ModelMapper modelMapper, UsuarioDao usuarioDao,
                       PasswordEncoder passwordEncoder, RolDao rolDao,
                       FileStorageService fileStorageService) {
        this.modelMapper = modelMapper;
        this.usuarioDao = usuarioDao;
        this.passwordEncoder = passwordEncoder;
        this.rolDao = rolDao;
        this.fileStorageService = fileStorageService;
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

        Usuario usuario = modelMapper.map(usuarioDto, Usuario.class);
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setRoles(resolveRoles(usuarioDto.getRoles()));

        Usuario saved = usuarioDao.save(usuario);
        return buildDtoWithPhotoUrl(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Usuario usuario = usuarioDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("Usuario", "id", id));
        usuarioDao.delete(usuario);
    }

    @Override
    @Transactional
    public UsuarioDtoRes updateUserRoles(UsuarioDto usuarioDto) {
        Usuario usuario = usuarioDao.findById(usuarioDto.getId())
                .orElseThrow(() -> new NotFoundExceptionResource("Usuario", "id", usuarioDto.getId()));

        usuario.getRoles().clear();
        usuario.getRoles().addAll(resolveRoles(usuarioDto.getRoles()));

        Usuario saved = usuarioDao.save(usuario);
        return buildDtoWithPhotoUrl(saved);
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
        if (usuarioUpdateDto.getName() != null) {
            usuario.setName(usuarioUpdateDto.getName());
        }
        if (usuarioUpdateDto.getApellidos() != null) {
            usuario.setApellidos(usuarioUpdateDto.getApellidos());
        }
        if (usuarioUpdateDto.getUriFoto() != null) {
            String photoUri = usuarioUpdateDto.getUriFoto();
            if (photoUri.contains("/")) {
                photoUri = photoUri.substring(photoUri.lastIndexOf("/") + 1);
            }
            usuario.setUriFoto(photoUri);
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

        usuario.setPassword(passwordEncoder.encode(usuarioChangePasswordDto.getNewPassword()));
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

    private Set<Rol> resolveRoles(Set<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            Rol defaultRol = rolDao.findByName(ERole.ENCARGADO_EVENTO)
                    .orElseThrow(() -> new NotFoundExceptionResource("Rol", "name", ERole.ENCARGADO_EVENTO));
            return Collections.singleton(defaultRol);
        }

        Set<Rol> roles = new HashSet<>();
        for (String roleName : roleNames) {
            try {
                ERole eRole = ERole.valueOf(roleName);
                Rol rol = rolDao.findByName(eRole)
                        .orElseThrow(() -> new NotFoundExceptionResource("Rol", "name", eRole));
                roles.add(rol);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Rol inválido: " + roleName);
            }
        }
        return roles;
    }

    private UsuarioDtoRes buildDtoWithPhotoUrl(Usuario usuario) {
        UsuarioDtoRes dto = modelMapper.map(usuario, UsuarioDtoRes.class);
        if (dto.getUriFoto() != null) {
            String fileUrl = ServletUriComponentsBuilder
                    .fromCurrentContextPath()
                    .path("/uploads/")
                    .path(USUARIOS_DIR)
                    .path(dto.getUriFoto())
                    .toUriString();
            dto.setUriFoto(fileUrl);
        }
        return dto;
    }
}
