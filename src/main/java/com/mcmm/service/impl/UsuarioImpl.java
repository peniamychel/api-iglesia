package com.mcmm.service.impl;

import com.mcmm.model.dao.RolDao;
import com.mcmm.model.dao.UsuarioDao;
import com.mcmm.model.dto.usuarioDto.UpdateUserDto;
import com.mcmm.model.dto.usuarioDto.UsuarioDto;
import com.mcmm.model.entity.ERole;
import com.mcmm.model.entity.Rol;
import com.mcmm.model.entity.Usuario;
import com.mcmm.service.FileStorageService;
import com.mcmm.service.IUsuario;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.mcmm.model.dto.usuarioDto.ChangePasswordDto;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.*;

@Service
@Transactional
public class UsuarioImpl implements IUsuario {

    @Autowired
    private UsuarioDao usuarioDao;

    @Autowired
    private RolImpl rolService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RolDao rolDao;

    private ModelMapper modelMapper = new ModelMapper();

    @Autowired
    private  FileStorageService fileStorageService;

    @Value("${file.base-url}")
    private String baseUrl;

    public UsuarioImpl() {
    }

    @Override
    public Iterable<UsuarioDto> findAll() {
        List<UsuarioDto> usuarioDto = new ArrayList<>();
        Iterable<Usuario> usuarios = usuarioDao.findAll();

        for (Usuario usuario : usuarios) {
            UsuarioDto dto = modelMapper.map(usuario, UsuarioDto.class);
            usuarioDto.add(dto);
        }
        return usuarioDto;
    }

    @Override
    public UsuarioDto findById(Long id) {
        Usuario usuario = usuarioDao.findById(id).orElse(null);
        if (usuario != null) {
            return modelMapper.map(usuario, UsuarioDto.class);
        }
        return null;
    }

    @Override
    public UsuarioDto create(Usuario usuario) {
        Usuario guardado = usuarioDao.save(usuario);
        UsuarioDto usuarioDtores = modelMapper.map(guardado, UsuarioDto.class);
        usuarioDtores.setPassword("pass oculto");
        return usuarioDtores;
    }

    @Override
    public void delete(Long id) {

    }

    @Override
    public Usuario updateUserRoles(UsuarioDto usuarioDto) {
        Usuario usuario = usuarioDao.findById(usuarioDto.getId())
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + usuarioDto.getId()));

        Set<Rol> newRoles = new HashSet<>();

        for (String roleName : usuarioDto.getRoles()) {
            try {
                ERole eRole = ERole.valueOf(roleName);
                Rol rol = rolDao.findByNameWithLock(eRole)
                        .orElseThrow(() -> new EntityNotFoundException("Rol no encontrado: " + roleName));
                newRoles.add(rol);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Rol inválido: " + roleName);
            }
        }

        // Actualizar los roles del usuario
        usuario.getRoles().clear();
        usuario.getRoles().addAll(newRoles);

        return usuarioDao.save(usuario);
    }


    @Override
    @Transactional
    public Usuario updateUser(UpdateUserDto updateUserDto) {
        Usuario usuario = usuarioDao.findById(updateUserDto.getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + updateUserDto.getId()));

        if (updateUserDto.getUsername() != null &&
                !updateUserDto.getUsername().equals(usuario.getUsername()) &&
                usuarioDao.existsByUsername(updateUserDto.getUsername())) {
            throw new IllegalArgumentException("Username is already taken");
        }

        if (updateUserDto.getEmail() != null &&
                !updateUserDto.getEmail().equals(usuario.getEmail()) &&
                usuarioDao.existsByEmail(updateUserDto.getEmail())) {
            throw new IllegalArgumentException("Email is already in use");
        }

        if (updateUserDto.getUsername() != null) {
            usuario.setUsername(updateUserDto.getUsername());
        }
        if (updateUserDto.getEmail() != null) {
            usuario.setEmail(updateUserDto.getEmail());
        }
        if(updateUserDto.getName() != null) {
            usuario.setName(updateUserDto.getName());
        }
        if(updateUserDto.getApellidos() != null) {
            usuario.setApellidos(updateUserDto.getApellidos());
        }
        if(updateUserDto.getUriFoto() != null) {
            usuario.setUriFoto(updateUserDto.getUriFoto());
        }
        if(updateUserDto.getEstado() != null) {
            usuario.setEstado(updateUserDto.getEstado());
        }

        return usuarioDao.save(usuario);
    }


//    @Override
//    @Transactional
//    public void changePassword(ChangePasswordDto changePasswordDto) {
//        Usuario usuario = usuarioDao.findById(changePasswordDto.getId())
//                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + changePasswordDto.getId()));
//
//        if (!passwordEncoder.matches(changePasswordDto.getCurrentPassword(), usuario.getPassword())) {
//            throw new IllegalArgumentException("Current password is incorrect");
//        }
//
//        usuario.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
//        usuarioDao.save(usuario);
//    }


    @Override
    @Transactional
    public void changePassword(ChangePasswordDto changePasswordDto, String currentUsername) {
        Usuario usuario = usuarioDao.findById(changePasswordDto.getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + changePasswordDto.getId()));

        // Verify that the authenticated user is changing their own password
//        if (!usuario.getUsername().equals(currentUsername)) {
//            throw new AccessDeniedException("You can only change your own password");
//        }

        if (!passwordEncoder.matches(changePasswordDto.getCurrentPassword(), usuario.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        usuario.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
        usuarioDao.save(usuario);
    }
    @Override
    @Transactional
    public String updateProfilePhoto(Long userId, MultipartFile file) throws IOException {
        Usuario usuario = usuarioDao.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        // Si existe una foto anterior, la eliminamos
        if (usuario.getUriFoto() != null) {
            String oldFileName = usuario.getUriFoto().substring(usuario.getUriFoto().lastIndexOf("/") + 1);
            fileStorageService.deleteFile(oldFileName);
        }

        // Guardamos la nueva foto
        String fileName = fileStorageService.storeFile(file, usuario.getUsername());

        // Construimos la URL completa
        String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/")
                .path(fileName)
                .toUriString();

        usuario.setUriFoto(fileUrl);
        usuarioDao.save(usuario);

        return fileUrl;
    }


}
