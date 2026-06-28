package com.mcmm.service.impl;

import com.mcmm.model.dao.BitacoraDao;
import com.mcmm.model.dao.UsuarioDao;
import com.mcmm.model.dto.BitacoraDto;
import com.mcmm.model.entity.Bitacora;
import com.mcmm.model.entity.Usuario;
import com.mcmm.service.IBitacora;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BitacoraImpl implements IBitacora {

    private final BitacoraDao bitacoraDao;
    private final UsuarioDao usuarioDao;
    private final ModelMapper modelMapper;

    @Override
    @Transactional(readOnly = true)
    public List<BitacoraDto> findAll() {
        List<BitacoraDto> dtos = new ArrayList<>();
        bitacoraDao.findAllByOrderByFechaDesc().forEach(b -> dtos.add(convertToDto(b)));
        return dtos;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BitacoraDto> findByModulo(String modulo) {
        List<BitacoraDto> dtos = new ArrayList<>();
        bitacoraDao.findByModuloOrderByFechaDesc(modulo).forEach(b -> dtos.add(convertToDto(b)));
        return dtos;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BitacoraDto> findByUser(String username) {
        List<BitacoraDto> dtos = new ArrayList<>();
        bitacoraDao.findByUsernameOrderByFechaDesc(username).forEach(b -> dtos.add(convertToDto(b)));
        return dtos;
    }

    @Override
    @Transactional
    public void registrar(Long usuarioId, String username, String accion, String modulo, String descripcion, String ipAddress) {
        Usuario usuario = null;
        if (usuarioId != null) {
            usuario = usuarioDao.findById(usuarioId).orElse(null);
        } else if (username != null && !username.isBlank()) {
            usuario = usuarioDao.findByUsername(username).orElse(null);
        }

        Bitacora bitacora = Bitacora.builder()
                .usuario(usuario)
                .username(usuario != null ? usuario.getUsername() : username)
                .accion(accion)
                .modulo(modulo)
                .descripcion(descripcion)
                .fecha(LocalDateTime.now())
                .ipAddress(ipAddress)
                .build();

        bitacoraDao.save(bitacora);
    }

    private BitacoraDto convertToDto(Bitacora bitacora) {
        BitacoraDto dto = modelMapper.map(bitacora, BitacoraDto.class);
        if (bitacora.getUsuario() != null) {
            dto.setUsuarioId(bitacora.getUsuario().getId());
            dto.setUserFullName(bitacora.getUsuario().getName() + " " + bitacora.getUsuario().getApellidos());
        } else {
            dto.setUserFullName(bitacora.getUsername() != null ? bitacora.getUsername() : "Sistema");
        }
        return dto;
    }
}
