package com.mcmm.service.impl;

import com.mcmm.exception.NotFoundExceptionResource;
import com.mcmm.model.dao.IglesiaDao;
import com.mcmm.model.dao.MiembroDao;
import com.mcmm.model.dao.MiembroIglesiaDao;
import com.mcmm.model.dto.GraficoDataDto;
import com.mcmm.model.dto.MiembroDto.MiembroDto;
import com.mcmm.model.dto.MiembroIglesiaDto;
import com.mcmm.model.entity.Iglesia;
import com.mcmm.model.entity.Miembro;
import com.mcmm.model.entity.MiembroIglesia;
import com.mcmm.service.IMiembroIglesia;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class MiembroIglesiaImpl implements IMiembroIglesia {

    private final ModelMapper modelMapper;
    private final MiembroIglesiaDao miembroIglesiaDao;
    private final MiembroDao miembroDao;
    private final IglesiaDao iglesiaDao;

    @Override
    @Transactional(readOnly = true)
    public List<MiembroIglesiaDto> findAll() {
        return StreamSupport.stream(miembroIglesiaDao.findAll().spliterator(), false)
                .map(miembroIglesia -> modelMapper.map(miembroIglesia, MiembroIglesiaDto.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MiembroIglesiaDto findById(Long id) {
        MiembroIglesia miembroIglesia = miembroIglesiaDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("MiembroIglesia", "id", id));
        return modelMapper.map(miembroIglesia, MiembroIglesiaDto.class);
    }

    @Override
    @Transactional
    public MiembroIglesiaDto save(MiembroIglesiaDto miembroIglesiaDto) {
        Miembro miembro = miembroDao.findById(miembroIglesiaDto.getMiembroId())
                .orElseThrow(() -> new NotFoundExceptionResource("Miembro", "id", miembroIglesiaDto.getMiembroId()));
        Iglesia iglesia = iglesiaDao.findById(miembroIglesiaDto.getIglesiaId())
                .orElseThrow(() -> new NotFoundExceptionResource("Iglesia", "id", miembroIglesiaDto.getIglesiaId()));

        MiembroIglesia miembroIglesia = modelMapper.map(miembroIglesiaDto, MiembroIglesia.class);
        miembroIglesia.setMiembro(miembro);
        miembroIglesia.setIglesia(iglesia);

        MiembroIglesia saved = miembroIglesiaDao.save(miembroIglesia);
        return modelMapper.map(saved, MiembroIglesiaDto.class);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        MiembroIglesia miembroIglesia = miembroIglesiaDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("MiembroIglesia", "id", id));
        miembroIglesiaDao.delete(miembroIglesia);
    }

    @Override
    @Transactional
    public MiembroIglesiaDto update(MiembroIglesiaDto miembroIglesiaDto) {
        MiembroIglesia miembroIglesiaE = miembroIglesiaDao.findById(miembroIglesiaDto.getId())
                .orElseThrow(() -> new NotFoundExceptionResource("MiembroIglesia", "id", miembroIglesiaDto.getId()));

        if (miembroIglesiaDto.getMiembroId() != null) {
            Miembro miembro = miembroDao.findById(miembroIglesiaDto.getMiembroId())
                    .orElseThrow(() -> new NotFoundExceptionResource("Miembro", "id", miembroIglesiaDto.getMiembroId()));
            miembroIglesiaE.setMiembro(miembro);
        }

        if (miembroIglesiaDto.getIglesiaId() != null) {
            Iglesia iglesia = iglesiaDao.findById(miembroIglesiaDto.getIglesiaId())
                    .orElseThrow(() -> new NotFoundExceptionResource("Iglesia", "id", miembroIglesiaDto.getIglesiaId()));
            miembroIglesiaE.setIglesia(iglesia);
        }

        miembroIglesiaE.setFecha(miembroIglesiaDto.getFecha());
        miembroIglesiaE.setMotivoTraspaso(miembroIglesiaDto.getMotivoTraspaso());
        miembroIglesiaE.setFechaTraspaso(miembroIglesiaDto.getFechaTraspaso());
        miembroIglesiaE.setUriCartaTraspaso(miembroIglesiaDto.getUriCartaTraspaso());
        miembroIglesiaE.setEstado(miembroIglesiaDto.getEstado());

        MiembroIglesia updated = miembroIglesiaDao.save(miembroIglesiaE);
        return modelMapper.map(updated, MiembroIglesiaDto.class);
    }

    @Override
    @Transactional
    public MiembroIglesiaDto estado(Long id) {
        MiembroIglesia miembroIglesiaE = miembroIglesiaDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("MiembroIglesia", "id", id));
        miembroIglesiaE.setEstado(!miembroIglesiaE.getEstado());
        MiembroIglesia updated = miembroIglesiaDao.save(miembroIglesiaE);
        return modelMapper.map(updated, MiembroIglesiaDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MiembroDto> findMiembrosIglesia(Long id) {
        return StreamSupport.stream(miembroIglesiaDao.findMiembrosIglesia(id).spliterator(), false)
                .map(miembro -> modelMapper.map(miembro, MiembroDto.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean findByIdMiembro(Long id) {
        return miembroIglesiaDao.findByMiembro(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GraficoDataDto> graficoMiembrosIglesia(Long limite) {
        List<Object[]> resultados = miembroIglesiaDao.obtenerIglesiasConMasMiembros(limite);
        return resultados.stream().map(fila -> {
            GraficoDataDto dto = new GraficoDataDto();
            dto.setId((Long) fila[0]);
            dto.setNombre((String) fila[1] + " - " + (String) fila[2]);
            dto.setValor((Long) fila[3]);
            return dto;
        }).collect(Collectors.toList());
    }
}
