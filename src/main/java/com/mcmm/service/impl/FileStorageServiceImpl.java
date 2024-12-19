package com.mcmm.service.impl;

//import com.example.demo.service.FileStorageService;
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


        } catch (IOException ex) {
            throw new RuntimeException("No se pudo crear el directorio de almacenamiento. ", ex);
        }
    }

    @Override
    public String storeFile(MultipartFile file, String nameModel, String nameDir) throws IOException {
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String fileNameNative = nameModel + "-" + UUID.randomUUID().toString() + fileExtension;
        String dirFile = nameDir + fileNameNative;

        Path targetLocation = this.fileStorageLocation.resolve(dirFile);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return fileNameNative;
    }

    @Override
    public void deleteFile(String fileName) throws IOException {
        Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
        Files.deleteIfExists(filePath);
    }

    @Override
    public Path getFilePath(String fileName) {
        return this.fileStorageLocation.resolve(fileName).normalize();
    }
}