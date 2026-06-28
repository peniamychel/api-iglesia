package com.mcmm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class WebIglesiaApplication {

	@Bean
	public ModelMapper modelMapper() {
		ModelMapper modelMapper = new ModelMapper();
		modelMapper.getConfiguration()
			.setAmbiguityIgnored(true);
		return modelMapper;
	}

	public static void main(String[] args) {
		SpringApplication.run(WebIglesiaApplication.class, args);
	}

}
