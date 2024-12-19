package com.mcmm.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Path;

public interface FileStorageService {
    String storeFile(MultipartFile file, String nameModel, String nameDir) throws IOException;
    void deleteFile(String fileName) throws IOException;
    Path getFilePath(String fileName);
}