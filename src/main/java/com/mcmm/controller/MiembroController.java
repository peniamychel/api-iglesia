package com.mcmm.controller;


import com.mcmm.service.IMiembro;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/miembro/v1")
@PreAuthorize("hasRole('ENCARGADO_IGLESIA')")
public class MiembroController {

    @Autowired
    private IMiembro miembrosService;


}
