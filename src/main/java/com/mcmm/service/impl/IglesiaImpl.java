package com.mcmm.service.impl;

import com.mcmm.exception.NotFoundExceptionResource;
import com.mcmm.model.dao.IglesiaDao;
import com.mcmm.model.dto.iglesiaDto.IglesiaDto;
import com.mcmm.model.entity.Iglesia;
import com.mcmm.service.IIglesia;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class IglesiaImpl implements IIglesia {

    private final ModelMapper modelMapper;
    private final IglesiaDao iglesiaDao;

    @Override
    @Transactional
    public IglesiaDto save(IglesiaDto iglesiaDto) {
        Iglesia iglesia = modelMapper.map(iglesiaDto, Iglesia.class);
        Iglesia savedIglesia = iglesiaDao.save(iglesia);
        return modelMapper.map(savedIglesia, IglesiaDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IglesiaDto> findAll() {
        return StreamSupport.stream(iglesiaDao.findAllByOrderByCreatedAtDesc().spliterator(), false)
                .map(iglesia -> modelMapper.map(iglesia, IglesiaDto.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public IglesiaDto findById(Long id) {
        Iglesia iglesia = iglesiaDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("Iglesia", "id", id));
        return modelMapper.map(iglesia, IglesiaDto.class);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Iglesia iglesia = iglesiaDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("Iglesia", "id", id));
        iglesiaDao.delete(iglesia);
    }

    @Override
    @Transactional
    public IglesiaDto update(Long id, IglesiaDto iglesiaDto) {
        Iglesia iglesia = iglesiaDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("Iglesia", "id", id));
        iglesia.setNombre(iglesiaDto.getNombre());
        iglesia.setDireccion(iglesiaDto.getDireccion());
        iglesia.setTelefono(iglesiaDto.getTelefono());
        iglesia.setFechaFundacion(iglesiaDto.getFechaFundacion());
        iglesia.setEstado(iglesiaDto.getEstado());
        Iglesia updated = iglesiaDao.save(iglesia);
        return modelMapper.map(updated, IglesiaDto.class);
    }

    @Override
    @Transactional
    public IglesiaDto estado(Long id) {
        Iglesia iglesia = iglesiaDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("Iglesia", "id", id));
        iglesia.setEstado(!iglesia.getEstado());
        Iglesia updated = iglesiaDao.save(iglesia);
        return modelMapper.map(updated, IglesiaDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public IglesiaDto buscarNombreIglesia(String nameIglesia) {
        Iglesia iglesia = iglesiaDao.buscarPorNombreIglesia(nameIglesia);
        if (iglesia != null) {
            return modelMapper.map(iglesia, IglesiaDto.class);
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public IglesiaDto buscarNombreIglesiaExceptoId(Long id, String nameIglesia) {
        Iglesia iglesia = iglesiaDao.buscarPorNombreIglesiaExceptoId(id, nameIglesia);
        if (iglesia != null) {
            return modelMapper.map(iglesia, IglesiaDto.class);
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<IglesiaDto> findByEstadoTrue() {
        return iglesiaDao.findByEstadoTrue().stream()
                .map(iglesia -> modelMapper.map(iglesia, IglesiaDto.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public IglesiaDto findByNombreAndIdNot(String nameIglesia, Long id) {
        Iglesia iglesia = iglesiaDao.findByNombreAndIdNot(nameIglesia, id);
        if (iglesia != null) {
            return modelMapper.map(iglesia, IglesiaDto.class);
        }
        return null;
    }
}
