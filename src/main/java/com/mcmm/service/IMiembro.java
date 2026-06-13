package com.mcmm.service;

import com.mcmm.model.dto.MiembroDto.MiembroDto;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface IMiembro {

    public Iterable<MiembroDto> findAll();
    public MiembroDto findById(Long id);
    public MiembroDto create(MiembroDto miembroDto);
    public void delete(Long id);
    public MiembroDto estado(Long id);
    public MiembroDto update(MiembroDto miembroDto);
    
    MiembroDto buscarCi(String ci);
    String updateProfilePhoto(Long id, MultipartFile file) throws IOException;
    void deleteProfilePhoto(Long id);
}
