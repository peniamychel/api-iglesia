package com.mcmm.security.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class FileConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Convertimos la ruta del .properties en una ruta absoluta real
        Path path = Paths.get(uploadDir).toAbsolutePath().normalize();
        String absolutePath = path.toUri().toString();

        registry.addResourceHandler("/uploads/**")
                // Usamos la ruta absoluta con el protocolo file:///
                .addResourceLocations(absolutePath)
                .setCachePeriod(3600)
                .resourceChain(true);
    }
}