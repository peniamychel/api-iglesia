package com.mcmm.service.impl;

import com.mcmm.model.dao.IglesiaDao;
import com.mcmm.model.dao.MiembroDao;
import com.mcmm.model.dao.MiembroIglesiaDao;
import com.mcmm.model.dto.GraficoDataDto;
import com.mcmm.model.dto.IglesiaDto;
import com.mcmm.model.dto.MiembroDto;
import com.mcmm.model.dto.MiembroIglesiaDto;
import com.mcmm.model.entity.Iglesia;
import com.mcmm.model.entity.Miembro;
import com.mcmm.model.entity.MiembroIglesia;
import com.mcmm.service.IMiembroIglesia;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MiembroIglesiaImpl implements IMiembroIglesia {

    @Autowired
    private MiembroIglesiaDao miembroIglesiaDao;
    @Autowired
    private MiembroDao miembroDao;
    @Autowired
    private IglesiaDao iglesiaDao;
    private ModelMapper modelMapper = new ModelMapper();



    @Override
    public Iterable<MiembroIglesiaDto> findAll() {
        List<MiembroIglesiaDto> miembrosIglesiaDtos = new ArrayList<>();
        Iterable<MiembroIglesia> miembrosIglesias = miembroIglesiaDao.findAll();

        for (MiembroIglesia miembroIglesia : miembrosIglesias) {
            MiembroIglesiaDto dto = modelMapper.map(miembroIglesia, MiembroIglesiaDto.class);
            miembrosIglesiaDtos.add(dto);
        }
        return miembrosIglesiaDtos;
    }

    @Override
    public MiembroIglesiaDto save(MiembroIglesiaDto miembroIglesiaDto) {
        Miembro miembro = miembroDao.findById(miembroIglesiaDto.getMiembroId()).orElse(null);
        Iglesia iglesia = iglesiaDao.findById(miembroIglesiaDto.getIglesiaId()).orElse(null);
        MiembroIglesia miembroIglesia = modelMapper.map(miembroIglesiaDto, MiembroIglesia.class);
        if (miembro != null || iglesia != null) {
            miembroIglesia.setIglesia(iglesia);
            miembroIglesia.setMiembro(miembro);
        }
        MiembroIglesia savedMiembroIglesia = miembroIglesiaDao.save(miembroIglesia);
        return modelMapper.map(savedMiembroIglesia, MiembroIglesiaDto.class);
    }

    @Override
    public MiembroIglesiaDto findById(Long id) {
        MiembroIglesia miembroIglesia = miembroIglesiaDao.findById(id).orElse(null);
        if (miembroIglesia != null) {
            return modelMapper.map(miembroIglesia, MiembroIglesiaDto.class); // personaDto
        }
        return null;
    }

    @Override
    public void delete(MiembroIglesiaDto miembroIglesiaDto) {

    }

    @Override
    public MiembroIglesiaDto update(MiembroIglesiaDto miembroIglesiaDto) {
        MiembroIglesia miembroIglesiaE = miembroIglesiaDao.findById(miembroIglesiaDto.getId()).orElse(null);
        MiembroIglesiaDto miembroIglesiaDtoE = modelMapper.map(miembroIglesiaE, MiembroIglesiaDto.class);
        if (miembroIglesiaE != null) {

            miembroIglesiaDtoE.setIglesiaId(miembroIglesiaDto.getIglesiaId());
            miembroIglesiaDtoE.setMiembroId(miembroIglesiaDto.getMiembroId());
            miembroIglesiaDtoE.setFecha(miembroIglesiaDto.getFecha());
            miembroIglesiaDtoE.setMotivoTraspaso(miembroIglesiaDto.getMotivoTraspaso());
            miembroIglesiaDtoE.setFechaTraspaso(miembroIglesiaDto.getFechaTraspaso());
            miembroIglesiaDtoE.setUriCartaTraspaso(miembroIglesiaDto.getUriCartaTraspaso());
            miembroIglesiaDtoE.setEstado(miembroIglesiaDto.getEstado());

            miembroIglesiaE = miembroIglesiaDao.save(modelMapper.map(miembroIglesiaDtoE, MiembroIglesia.class));
            return modelMapper.map(miembroIglesiaE, MiembroIglesiaDto.class); // personaDto
        }
        return null;

    }

    @Override
    public boolean estado(Long id) {
        MiembroIglesia miembroIglesiaE = miembroIglesiaDao.findById(id).orElse(null);
        if (miembroIglesiaE != null) {
            miembroIglesiaE.setEstado(!miembroIglesiaE.getEstado());
            miembroIglesiaDao.save(miembroIglesiaE);
            return miembroIglesiaE.getEstado();
        }
        return false;
    }

    @Override
    public Iterable<MiembroDto> findMiembrosIglesia(Long id) {
        List<MiembroDto> miembrosDtos = new ArrayList<>();
        Iterable<Miembro> miembrosIglesia = miembroIglesiaDao.findMiembrosIglesia(id);
        for (Miembro miembroIglesia : miembrosIglesia) {
            MiembroDto dto = modelMapper.map(miembrosIglesia, MiembroDto.class);
            miembrosDtos.add(dto);
        }
        return miembrosDtos;
    }

    @Override
    public boolean findByIdMiembro(Long id) {
        boolean res = false;
        res = miembroIglesiaDao.findByMiembro(id);
        return res;
    }


//    public List<GraficoDataDto> graficoMiembrosIglesia(Long cant) {
//        List<Object[]> graficoDataDtos = miembroIglesiaDao.obtenerIglesiasConMasMiembros(cant);
//        return graficoDataDtos;
//    }

    @Override
    public List<GraficoDataDto> graficoMiembrosIglesia(Long limite) {
        List<Object[]> resultados = miembroIglesiaDao.obtenerIglesiasConMasMiembros(limite);
        List<GraficoDataDto> lista = new ArrayList<>();

        for (Object[] fila : resultados) {
            GraficoDataDto dto = new GraficoDataDto();
            dto.setId((Long) fila[0]);
            dto.setNombre((String) fila[1]+" - "+(String) fila[2]);
            dto.setValor((Long) fila[3]);
            lista.add(dto);
        }

        return lista;
    }
}
