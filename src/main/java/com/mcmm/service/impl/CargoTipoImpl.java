package com.mcmm.service.impl;

import com.mcmm.exception.NotFoundExceptionResource;
import com.mcmm.model.dao.CargoTipoDao;
import com.mcmm.model.dto.CargoTipoDto;
import com.mcmm.model.entity.CargoTipo;
import com.mcmm.service.ICargoTipo;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class CargoTipoImpl implements ICargoTipo {

    private final ModelMapper modelMapper;
    private final CargoTipoDao cargoTipoDao;

    @Override
    @Transactional(readOnly = true)
    public List<CargoTipoDto> findAll() {
        return StreamSupport.stream(cargoTipoDao.findAll().spliterator(), false)
                .map(cargoTipo -> modelMapper.map(cargoTipo, CargoTipoDto.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CargoTipoDto findById(Long id) {
        CargoTipo cargoTipo = cargoTipoDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("CargoTipo", "id", id));
        return modelMapper.map(cargoTipo, CargoTipoDto.class);
    }

    @Override
    @Transactional
    public CargoTipoDto create(CargoTipoDto cargoTipoDto) {
        CargoTipo cargoTipo = modelMapper.map(cargoTipoDto, CargoTipo.class);
        CargoTipo savedCargoTipo = cargoTipoDao.save(cargoTipo);
        return modelMapper.map(savedCargoTipo, CargoTipoDto.class);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        CargoTipo cargoTipo = cargoTipoDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("CargoTipo", "id", id));
        cargoTipoDao.delete(cargoTipo);
    }

    @Override
    @Transactional
    public CargoTipoDto estado(Long id) {
        CargoTipo cargoTipo = cargoTipoDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("CargoTipo", "id", id));
        cargoTipo.setEstado(!cargoTipo.getEstado());
        CargoTipo updated = cargoTipoDao.save(cargoTipo);
        return modelMapper.map(updated, CargoTipoDto.class);
    }

    @Override
    @Transactional
    public CargoTipoDto update(CargoTipoDto cargoTipoDto) {
        CargoTipo cargoTipoR = cargoTipoDao.findById(cargoTipoDto.getId())
                .orElseThrow(() -> new NotFoundExceptionResource("CargoTipo", "id", cargoTipoDto.getId()));
        cargoTipoR.setTipo(cargoTipoDto.getTipo());
        cargoTipoR.setNombre(cargoTipoDto.getNombre());
        CargoTipo updated = cargoTipoDao.save(cargoTipoR);
        return modelMapper.map(updated, CargoTipoDto.class);
    }

    @Override
    @Transactional
    public CargoTipoDto save(CargoTipoDto cargoTipoDto) {
        CargoTipo cargoTipo = modelMapper.map(cargoTipoDto, CargoTipo.class);
        CargoTipo saved = cargoTipoDao.save(cargoTipo);
        return modelMapper.map(saved, CargoTipoDto.class);
    }
}
