package com.mcmm.service.impl;

import com.mcmm.exception.DataAccessResourceException;
import com.mcmm.model.dao.UsuarioDao;
import com.mcmm.model.dao.PrivilegioDao;
import com.mcmm.model.entity.Cargo;
import com.mcmm.model.entity.RolCargo;
import com.mcmm.model.entity.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UsuarioDao usuarioDao;

    @Autowired
    private PrivilegioDao privilegioDao;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessResourceException {
        Usuario usuario = usuarioDao
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("El usuario "+username+" no existe"));

        List<GrantedAuthority> authorities = new ArrayList<>();

        // Privilegios directos asignados al usuario
        if (usuario.getPrivilegios() != null) {
            usuario.getPrivilegios().forEach(p -> {
                if (p.getNombre() != null) {
                    authorities.add(new SimpleGrantedAuthority(p.getNombre()));
                }
            });
        }

        if (usuario.getMiembro() == null) {
            // Super-administrador global
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            privilegioDao.findAll().forEach(p -> {
                if (p.getNombre() != null) {
                    authorities.add(new SimpleGrantedAuthority(p.getNombre()));
                }
            });
        } else {
            // Miembro con cargos
            Date now = new Date();
            if (usuario.getMiembro().getCargos() != null) {
                usuario.getMiembro().getCargos().forEach(cargo -> {
                    if (Boolean.TRUE.equals(cargo.getEstado()) &&
                            (cargo.getFechaFin() == null || cargo.getFechaFin().after(now))) {
                        RolCargo rc = cargo.getRolCargo();
                        if (rc != null) {
                            if (rc.getNombreRol() != null) {
                                authorities.add(new SimpleGrantedAuthority("ROLE_" + rc.getNombreRol()));
                            }
                            if (rc.getPrivilegios() != null) {
                                rc.getPrivilegios().forEach(p -> {
                                    if (p.getNombre() != null) {
                                        authorities.add(new SimpleGrantedAuthority(p.getNombre()));
                                    }
                                });
                            }
                        }
                    }
                });
            }
        }

        return new User(
                usuario.getUsername(),
                usuario.getPassword(),
                true,
                true,
                true,
                true,
                authorities);
    }

}
