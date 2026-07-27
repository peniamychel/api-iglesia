package com.mcmm.service;

import com.mcmm.model.dto.GraficoDataDto;
import com.mcmm.model.dto.MiembroDto.MiembroDto;
import com.mcmm.model.dto.MiembroIglesiaDto;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface IMiembroIglesia {

    List<MiembroIglesiaDto> findAll();

    MiembroIglesiaDto findById(Long id);

    MiembroIglesiaDto save(MiembroIglesiaDto miembroIglesiaDto);

    void delete(Long id);

    MiembroIglesiaDto update(MiembroIglesiaDto miembroIglesiaDto);

    MiembroIglesiaDto estado(Long id);

    List<MiembroDto> findMiembrosIglesia(Long id);

    boolean findByIdMiembro(Long id);

    List<GraficoDataDto> graficoMiembrosIglesia(Long cant);

    MiembroIglesiaDto solicitarTraspaso(MiembroIglesiaDto dto);
    MiembroIglesiaDto aceptarTraspaso(Long id);
    MiembroIglesiaDto rechazarTraspaso(Long id);
    List<MiembroIglesiaDto> getSolicitudesPendientes(Long iglesiaId);
    List<MiembroIglesiaDto> obtenerHistorialMiembro(Long miembroId);
    String subirCartaTraspaso(Long id, MultipartFile file) throws IOException;
}
