package com.mcmm.service;

import com.mcmm.model.dto.PrivilegioDto;

public interface IPrivilegio {

    Iterable<PrivilegioDto> findAll();

    PrivilegioDto findById(Long id);

    PrivilegioDto save(PrivilegioDto privilegioDto);

    void delete(PrivilegioDto privilegioDto);

    PrivilegioDto update(Long id, PrivilegioDto privilegioDto);
}
