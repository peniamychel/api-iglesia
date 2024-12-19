package com.mcmm.service.impl;

import com.mcmm.model.dao.CargoTipoDao;
import com.mcmm.model.dto.CargoTipoDto;
import com.mcmm.model.entity.CargoTipo;
import com.mcmm.service.ICargoTipo;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CargoTipoImpl implements ICargoTipo {
    private ModelMapper modelMapper = new ModelMapper();

    @Autowired
    private CargoTipoDao cargoTipoDao;
    @Override
    public Iterable<CargoTipoDto> findAll() {
        List<CargoTipoDto> cargosTipoDtos = new ArrayList<>();
        Iterable<CargoTipo> cargosTipo = cargoTipoDao.findAll();

        for (CargoTipo cargoTipo : cargosTipo) {
            CargoTipoDto cargoDto = modelMapper.map(cargoTipo, CargoTipoDto.class);

            cargosTipoDtos.add(cargoDto);
        }
        return cargosTipoDtos;
    }

    @Override
    public CargoTipoDto findById(Long id) {
        return null;
    }

    @Override
    public CargoTipoDto create(CargoTipoDto cargoTipoDto) {
        CargoTipo cargoTipo = modelMapper.map(cargoTipoDto, CargoTipo.class);

        // Convertir el personaId en una entidad de Persona antes de guardar
//        if (cargoDto.getTipoCargoId() != null) {
//            CargoTipo cargoTipo = cargoDao.findById(miembroDto.getPersonaId())
//                    .orElseThrow(() -> new RuntimeException("Persona no encontrada"));
//            miembro.setPersona(persona);
//        }

        CargoTipo savedCargoTipo = cargoTipoDao.save(cargoTipo);
        CargoTipoDto cargoTipoDtoSave = modelMapper.map(savedCargoTipo, CargoTipoDto.class);
//        cargoDtoSave.setPersonaId(miembro.getPersona().getId());
        return cargoTipoDtoSave;
    }

    @Override
    public void delete(Long id) {

    }

    @Override
    public CargoTipoDto estado(Long id) {
        return null;
    }

    @Override
    public CargoTipoDto update(CargoTipoDto cargoTipoDto) {


        return null;
    }
}
