package com.mcmm.service.impl;

import com.mcmm.exception.DataAccessException;
import com.mcmm.model.dao.UsuarioDao;
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
import java.util.List;

@Service
@Transactional
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UsuarioDao usuarioDao;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
        Usuario usuario = usuarioDao
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("El usuario "+username+" no existe"));

        List<GrantedAuthority> authorities = new ArrayList<>();

        usuario.getRoles().forEach(role -> {
            authorities.add(new SimpleGrantedAuthority("ROLE_".concat(role.getName().name())));
            role.getPrivilegios().forEach(privilegio ->
                authorities.add(new SimpleGrantedAuthority(privilegio.getNombre()))
            );
        });

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
