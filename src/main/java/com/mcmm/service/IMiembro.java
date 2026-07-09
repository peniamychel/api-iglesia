package com.mcmm.service;

import com.mcmm.model.dto.MiembroDto.MiembroDto;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

public interface IMiembro {

    public Iterable<MiembroDto> findAll();
    public org.springframework.data.domain.Page<MiembroDto> findAllPaged(
            String searchText, 
            Boolean estado, 
            String iglesiaNombre, 
            org.springframework.data.domain.Pageable pageable);
    public MiembroDto findById(Long id);
    public MiembroDto create(MiembroDto miembroDto);
    public void delete(Long id);
    public MiembroDto estado(Long id);
    public MiembroDto update(MiembroDto miembroDto);
    
    MiembroDto buscarCi(String ci);
    String updateProfilePhoto(Long id, MultipartFile file) throws IOException;
    void deleteProfilePhoto(Long id);
    List<MiembroDto> findSinIglesia();
    List<MiembroDto> findSinIglesiaParaAsignacion();
    int importFromExcel(MultipartFile file, Long iglesiaId) throws IOException;
    byte[] generateExcelTemplate() throws IOException;
}

