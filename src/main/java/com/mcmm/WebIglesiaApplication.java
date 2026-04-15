package com.mcmm;

import com.mcmm.model.dao.UsuarioDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class WebIglesiaApplication {

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
