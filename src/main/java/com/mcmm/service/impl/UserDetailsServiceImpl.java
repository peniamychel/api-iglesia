package com.mcmm.service.impl;

import com.mcmm.exception.DataAccessResourceException;
import com.mcmm.model.dao.UsuarioDao;
import com.mcmm.model.dao.AccionDao;
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
    private AccionDao accionDao;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException, DataAccessResourceException {
        Usuario usuario = usuarioDao
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("El usuario " + username + " no existe"));

        List<GrantedAuthority> authorities = new ArrayList<>();

        if (Boolean.TRUE.equals(usuario.getEsAdmin())) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            accionDao.findAll().forEach(a -> {
                if (a.getAuthorityCode() != null) {
                    authorities.add(new SimpleGrantedAuthority(a.getAuthorityCode()));
                }
            });
        } else if (usuario.getMiembro() != null) {
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
                            if (rc.getAcciones() != null) {
                                rc.getAcciones().forEach(a -> {
                                    if (a.getAuthorityCode() != null) {
                                        authorities.add(new SimpleGrantedAuthority(a.getAuthorityCode()));
                                    }
                                });
                            }
                        }
                    }
                });
            }
        }

        boolean enabled = Boolean.TRUE.equals(usuario.getEstado());

        return new User(
                usuario.getUsername(),
                usuario.getPassword(),
                enabled,
                true,
                true,
                true,
                authorities);
    }

}
