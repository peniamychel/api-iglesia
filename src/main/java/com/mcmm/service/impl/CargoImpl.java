package com.mcmm.service.impl;

import com.mcmm.exception.NotFoundExceptionResource;
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
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CargoImpl implements ICargo {

    private final ModelMapper modelMapper;
    private final CargoDao cargoDao;
    private final IglesiaDao iglesiaDao;
    private final MiembroDao miembroDao;
    private final CargoTipoDao cargoTipoDao;

    @Override
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public CargoDto findById(Long id) {
        Cargo cargo = cargoDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("Cargo", "id", id));
        return modelMapper.map(cargo, CargoDto.class);
    }

    @Override
    @Transactional
    public CargoDto create(CargoDto cargoDto) {
        Cargo cargo = modelMapper.map(cargoDto, Cargo.class);

        //tratar id de iglesia
        if (cargoDto.getIglesiaId() != null) {
            Iglesia iglesia = iglesiaDao.findById(cargoDto.getIglesiaId())
                    .orElseThrow(() -> new NotFoundExceptionResource("Iglesia", "id", cargoDto.getIglesiaId()));
            if (!iglesia.getEstado()) throw new IllegalArgumentException("La iglesia proporcionada está inactiva.");
            cargo.setIglesia(iglesia);
        }

        //tratar id de miembro
        if (cargoDto.getIdMiembro() != null) {
            Miembro miembro = miembroDao.findById(cargoDto.getIdMiembro())
                    .orElseThrow(() -> new NotFoundExceptionResource("Miembro", "id", cargoDto.getIdMiembro()));
            if (!miembro.getEstado()) throw new IllegalArgumentException("El miembro proporcionado está inactivo.");
            cargo.setMiembro(miembro);
        }

        //tratar id tipo cargo
        if (cargoDto.getTipoCargoId() != null) {
            CargoTipo cargoTipo = cargoTipoDao.findById(cargoDto.getTipoCargoId())
                    .orElseThrow(() -> new NotFoundExceptionResource("CargoTipo", "id", cargoDto.getTipoCargoId()));
            if (!cargoTipo.getEstado()) throw new IllegalArgumentException("El tipo de cargo proporcionado está inactivo.");
            cargo.setTipoCargo(cargoTipo);
        }

        Cargo savedCargo = cargoDao.save(cargo);
        return modelMapper.map(savedCargo, CargoDto.class);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!cargoDao.existsById(id)) {
            throw new NotFoundExceptionResource("Cargo", "id", id);
        }
        cargoDao.deleteById(id);
    }

    @Override
    @Transactional
    public void estado(Long id) {
        if (!cargoDao.existsById(id)) {
            throw new NotFoundExceptionResource("Cargo", "id", id);
        }
        cargoDao.toggleEstado(id);
    }

    @Override
    @Transactional
    public CargoDto update(CargoDto cargoDto) {
        Cargo cargoR = cargoDao.findById(cargoDto.getId())
                .orElseThrow(() -> new NotFoundExceptionResource("Cargo", "id", cargoDto.getId()));

        if (cargoDto.getIglesiaId() != null) {
            Iglesia iglesia = iglesiaDao.findById(cargoDto.getIglesiaId())
                    .orElseThrow(() -> new NotFoundExceptionResource("Iglesia", "id", cargoDto.getIglesiaId()));
            if (!iglesia.getEstado()) throw new IllegalArgumentException("La iglesia proporcionada está inactiva.");
            cargoR.setIglesia(iglesia);
        }

        if (cargoDto.getIdMiembro() != null) {
            Miembro miembro = miembroDao.findById(cargoDto.getIdMiembro())
                    .orElseThrow(() -> new NotFoundExceptionResource("Miembro", "id", cargoDto.getIdMiembro()));
            if (!miembro.getEstado()) throw new IllegalArgumentException("El miembro proporcionado está inactivo.");
            cargoR.setMiembro(miembro);
        }

        if (cargoDto.getTipoCargoId() != null) {
            CargoTipo cargoTipo = cargoTipoDao.findById(cargoDto.getTipoCargoId())
                    .orElseThrow(() -> new NotFoundExceptionResource("CargoTipo", "id", cargoDto.getTipoCargoId()));
            if (!cargoTipo.getEstado()) throw new IllegalArgumentException("El tipo de cargo proporcionado está inactivo.");
            cargoR.setTipoCargo(cargoTipo);
        }

        cargoR.setDetalle(cargoDto.getDetalle());
        cargoR.setFechaInicio(cargoDto.getFechaInicio());
        cargoR.setFechaFin(cargoDto.getFechaFin());

        Cargo updatedCargo = cargoDao.save(cargoR);
        return modelMapper.map(updatedCargo, CargoDto.class);
    }

    @Override
    @Transactional
    public CargoDto save(CargoDto cargoDto) {
        try {
            Cargo cargo = modelMapper.map(cargoDto, Cargo.class);
            Cargo saveCargo = cargoDao.save(cargo);
            return modelMapper.map(saveCargo, CargoDto.class);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("Error de integridad de datos en la base de datos.");
        }
    }
}
