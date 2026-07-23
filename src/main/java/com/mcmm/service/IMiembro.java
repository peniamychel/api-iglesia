package com.mcmm.service;

import com.mcmm.model.dto.MiembroDto.MiembroDto;
import com.mcmm.model.dto.MiembroDto.MiembroImportResultDto;
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
    /** Carga masiva con iglesia fija (pastor/obrero: todos los miembros a su iglesia). */
    MiembroImportResultDto importFromExcel(MultipartFile file, Long iglesiaId) throws IOException;
    /** Carga masiva del admin: la iglesia de cada miembro se lee por nombre desde la columna "Iglesia". */
    MiembroImportResultDto importFromExcelPorNombreIglesia(MultipartFile file) throws IOException;
    /** Genera la plantilla; si {@code includeIglesia} es true agrega la columna "Iglesia" (uso del admin). */
    byte[] generateExcelTemplate(boolean includeIglesia) throws IOException;
}

