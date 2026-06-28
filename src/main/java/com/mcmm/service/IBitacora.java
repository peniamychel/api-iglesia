package com.mcmm.service;

import com.mcmm.model.dto.BitacoraDto;
import java.util.List;

public interface IBitacora {
    List<BitacoraDto> findAll();
    List<BitacoraDto> findByModulo(String modulo);
    List<BitacoraDto> findByUser(String username);
    void registrar(Long usuarioId, String username, String accion, String modulo, String descripcion, String ipAddress);
}
