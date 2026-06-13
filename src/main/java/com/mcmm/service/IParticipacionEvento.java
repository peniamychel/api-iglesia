package com.mcmm.service;

import com.mcmm.model.dto.participacionEvento.ParticipacionEventoDto;

import java.util.List;

public interface IParticipacionEvento {
    List<ParticipacionEventoDto> findAll();
    ParticipacionEventoDto findById(Long id);
    ParticipacionEventoDto create(ParticipacionEventoDto participacionEventoDto);
    ParticipacionEventoDto update(ParticipacionEventoDto participacionEventoDto);
    void delete(Long id);
    void estado(Long id);
    void toggleEntregado(Long id, String username);
}