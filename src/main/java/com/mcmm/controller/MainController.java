package com.mcmm.controller;

import com.mcmm.model.dao.RolDao;
import com.mcmm.model.dao.UsuarioDao;
import com.mcmm.model.dto.UsuarioDto;
import com.mcmm.model.entity.ERole;
import com.mcmm.model.entity.Rol;
import com.mcmm.model.entity.Usuario;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/main/v1")
@PreAuthorize("hasRole('ADMIN')")
public class MainController {


}
