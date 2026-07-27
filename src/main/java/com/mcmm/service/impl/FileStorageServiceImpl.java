package com.mcmm.service.impl;

import com.mcmm.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageServiceImpl(@Value("${file.upload-dir}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir)
                .toAbsolutePath()
                .normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);

            //SE CREAN TODOS LOS SUB DIRECTORIOS DEL DIRECTORIO PRINCIPAL
            Path subDirectoryMonito = this.fileStorageLocation.resolve("usuarios");
            Files.createDirectories(subDirectoryMonito);

            Path subDirectoryPatito = this.fileStorageLocation.resolve("personas");
            Files.createDirectories(subDirectoryPatito);

            Path subDirectoryCertificados = this.fileStorageLocation.resolve("certificados");
            Files.createDirectories(subDirectoryCertificados);

            Path subDirectoryPlantillas = this.fileStorageLocation.resolve("plantillas");
            Files.createDirectories(subDirectoryPlantillas);

            Path subDirectoryCargos = this.fileStorageLocation.resolve("cargos");
            Files.createDirectories(subDirectoryCargos);

        } catch (IOException ex) {
            throw new RuntimeException("No se pudo crear el directorio de almacenamiento. ", ex);
        }
    }

    @Override
    public String storeFile(MultipartFile file, String nameModel, String nameDir) throws IOException {
        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }
        String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf('.'));
        String fileNameNative = nameModel + "-" + UUID.randomUUID() + fileExtension;
        // Build directory path safely, asegurando que no escape del directorio raiz
        Path directory = resolveSafely(nameDir);
        // Ensure directory exists (it should have been created, but guard just in case)
        Files.createDirectories(directory);
        Path targetLocation = directory.resolve(fileNameNative);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        return fileNameNative;
    }

    @Override
    public void deleteFile(String fileName) throws IOException {
        Path filePath = resolveSafely(fileName);
        Files.deleteIfExists(filePath);
    }

    @Override
    public Path getFilePath(String fileName) {
        return resolveSafely(fileName);
    }

    /**
     * Resuelve una ruta relativa contra el directorio raiz y verifica que el
     * resultado siga contenido dentro de el. Evita path traversal ("../..").
     */
    private Path resolveSafely(String relativePath) {
        if (relativePath == null) {
            throw new IllegalArgumentException("La ruta de archivo no puede ser nula");
        }
        Path resolved = this.fileStorageLocation.resolve(relativePath).normalize();
        if (!resolved.startsWith(this.fileStorageLocation)) {
            throw new IllegalArgumentException("Ruta de archivo no permitida: " + relativePath);
        }
        return resolved;
    }
}
