package com.mcmm;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class WebIglesiaApplicationTests {

	@Autowired
	private com.mcmm.model.dao.UsuarioDao usuarioDao;

	@Autowired
	private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

	@Test
	@org.springframework.test.annotation.Commit
	void contextLoads() throws java.io.IOException {
		java.io.File file = new java.io.File("C:/Users/mayki/github repositorios/db_debug.txt");
		try (java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(file))) {
			writer.println("====== DEPURATION TEST: RESET PASSWORD ======");
			usuarioDao.findByUsername("romina").ifPresent(u -> {
				u.setPassword(passwordEncoder.encode("1234"));
				usuarioDao.save(u);
				writer.println("Contraseña de romina restablecida a '1234'");
			});
			writer.println("================================================");
		}
	}

}
