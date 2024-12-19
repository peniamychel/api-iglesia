package com.mcmm.service.impl;

import com.mcmm.model.dao.CargoDao;
import com.mcmm.model.dao.CargoTipoDao;
import com.mcmm.model.dao.IglesiaDao;
import com.mcmm.model.dao.MiembroDao;
import com.mcmm.model.dto.CargoDto;
import com.mcmm.model.entity.Cargo;
import com.mcmm.model.entity.CargoTipo;
import com.mcmm.model.entity.Iglesia;
import com.mcmm.model.entity.Miembro;
import com.mcmm.service.ICargo;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CargoImpl implements ICargo {

    private ModelMapper modelMapper = new ModelMapper();

    @Autowired
    private CargoDao cargoDao;

    @Autowired
    private IglesiaDao iglesiaDao;
    ;

    @Autowired
    private MiembroDao miembroDao;

    @Autowired
    private CargoTipoDao cargoTipoDao;


    @Override
    public Iterable<CargoDto> findAll() {
        List<CargoDto> cargosDtos = new ArrayList<>();
        Iterable<Cargo> cargos = cargoDao.findAll();

        for (Cargo cargo : cargos) {
            CargoDto cargoDto = modelMapper.map(cargo, CargoDto.class);

            cargosDtos.add(cargoDto);
        }
        return cargosDtos;
    }

    @Override
    public CargoDto findById(Long id) {
        return null;
    }

    @Override
    public CargoDto create(CargoDto cargoDto) {
        Cargo cargo = modelMapper.map(cargoDto, Cargo.class);

        //tratar id de iglesia
        if (cargoDto.getIglesiaId() != null) {
            Iglesia iglesia = iglesiaDao.findById(cargoDto.getIglesiaId())
                    .orElseThrow(() -> new RuntimeException("Iglesia no encontrada"));
            if (!iglesia.getEstado()) throw new RuntimeException("Iglesia inactiva");
            cargo.setIglesia(iglesia);
        }

        //tratar id de miembro
        if (cargoDto.getIdMiembro() != null) {
            Miembro miembro = miembroDao.findById(cargoDto.getIdMiembro())
                    .orElseThrow(() -> new RuntimeException("Miembro no encontrada"));
            if (!miembro.getEstado()) throw new RuntimeException("Miembro inactivo");
            cargo.setMiembro(miembro);
        }

        //tratar id tipo cargo
        if (cargoDto.getTipoCargoId() != null) {
            CargoTipo cargoTipo = cargoTipoDao.findById(cargoDto.getTipoCargoId())
                    .orElseThrow(() -> new RuntimeException("Cargo Tipo no encontrada"));
            if (!cargoTipo.getEstado()) throw new RuntimeException("Cargo Tipo inactivo");
            cargo.setTipoCargo(cargoTipo);
        }

        Cargo savedCargo = cargoDao.save(cargo);
        CargoDto cargoDtoSave = modelMapper.map(savedCargo, CargoDto.class);
//        cargoDtoSave.setPersonaId(miembro.getPersona().getId());
        return cargoDtoSave;
    }

    @Override
    public void delete(Long id) {

    }

    @Override
    public CargoDto estado(Long id) {
        return null;
    }

    @Override
    public CargoDto update(CargoDto cargoDto) {
        Cargo cargoR = cargoDao.findById(cargoDto.getId()).orElse(null);
        if (cargoR != null) {

            if (cargoDto.getIglesiaId() != null) {
                Iglesia iglesia = iglesiaDao.findById(cargoDto.getIglesiaId())
                        .orElseThrow(() -> new RuntimeException("Iglesia no encontrada"));
                if (!iglesia.getEstado()) throw new RuntimeException("Iglesia inactiva");
                cargoR.setIglesia(iglesia);
            }

            if (cargoDto.getIdMiembro() != null) {
                Miembro miembro = miembroDao.findById(cargoDto.getIdMiembro())
                        .orElseThrow(() -> new RuntimeException("Miembro no encontrada"));
                if (!miembro.getEstado()) throw new RuntimeException("Miembro inactivo");
                cargoR.setMiembro(miembro);
            }

            if (cargoDto.getTipoCargoId() != null) {
                CargoTipo cargoTipo = cargoTipoDao.findById(cargoDto.getTipoCargoId())
                        .orElseThrow(() -> new RuntimeException("Cargo Tipo no encontrada"));
                if (!cargoTipo.getEstado()) throw new RuntimeException("Cargo Tipo inactivo");
                cargoR.setTipoCargo(cargoTipo);
            }

            cargoR.setDetalle(cargoDto.getDetalle());
            cargoR.setFechaInicio(cargoDto.getFechaInicio());
            cargoR.setFechaFin(cargoDto.getFechaFin());

            cargoR = cargoDao.save(cargoR);
        }
        return modelMapper.map(cargoR, CargoDto.class);
    }
}
