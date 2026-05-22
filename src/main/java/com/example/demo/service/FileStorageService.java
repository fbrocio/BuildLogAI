package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    //Carpeta física donde se guardarán las imágenes
    //****REVISAR****
    private final String uploadDir = "upload/records";

    public String saveImage(MultipartFile file) {
        try {
            Path uploadPath = Paths.get(uploadDir);

            if(!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = file.getOriginalFilename();

            String extension = "";

            if(originalFilename != null &&
            originalFilename.contains(".")){
                extension = originalFilename.substring(
                        originalFilename.lastIndexOf(".")
                );
            }
            String filename = UUID.randomUUID() + extension;

            // Ruta final
            Path filePath = uploadPath.resolve(filename);

            // Guardar archivo
            Files.copy(
                    file.getInputStream(),
                    filePath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            // Devolver URL accesible
            return "/upload/records/" + filename;
        } catch (IOException e) {
            throw new RuntimeException(
                    "Error saving image" ,
                    e
            );
        }
    }
}
