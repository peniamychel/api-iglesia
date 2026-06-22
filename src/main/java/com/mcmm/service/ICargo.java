package com.mcmm.service;

import com.mcmm.model.dto.CargoDto;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Date;

public interface ICargo {
    public Iterable<CargoDto> findAll();
    public CargoDto findById(Long id);
    public CargoDto create(CargoDto cargoDto);
    public void delete(Long id);
    public void estado(Long id);
    public boolean estadoConFecha(Long id, Date fechaFin);
    public CargoDto update( CargoDto cargoDto);
    public CargoDto save(CargoDto cargoDto);
    String saveActaAsignacion(Long id, MultipartFile file) throws IOException;
    String saveActaDeslindacion(Long id, MultipartFile file) throws IOException;
}
