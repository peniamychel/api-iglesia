package com.mcmm;

import com.mcmm.model.dao.UsuarioDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class WebIglesiaApplication {

	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}

	public static void main(String[] args) {
		SpringApplication.run(WebIglesiaApplication.class, args);
	}

	@Autowired
	UsuarioDao usuarioDao;

	@Autowired
	PasswordEncoder passwordEncoder;

	// @Bean
	// public CommandLineRunner init(){
	// return args ->{
	// Usuario usuario = Usuario.builder()
	// .email("5I5wF@example.com")
	// .username("admin")
	// .password(passwordEncoder.encode("1234"))
	// .roles(Set.of(Rol.builder()
	// .name(ERole.valueOf(ERole.ADMIN.name()))
	// .build()))
	// .build();
	// usuarioDao.save(usuario);
	// };
	// }
}
