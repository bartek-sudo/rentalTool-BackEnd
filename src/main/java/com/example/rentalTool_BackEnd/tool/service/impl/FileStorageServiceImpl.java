package com.example.rentalTool_BackEnd.tool.service.impl;

import com.example.rentalTool_BackEnd.tool.exception.InvalidFileTypeException;
import com.example.rentalTool_BackEnd.tool.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageServiceImpl(@Value("${file.upload-dir:./uploads/images}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir)
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    @Override
    public String storeFile(MultipartFile file) {
        // Normalizacja nazwy pliku
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

        // Weryfikacja nazwy pliku - path traversal
        if (originalFilename.contains("..")) {
            throw new RuntimeException("Sorry! Filename contains invalid path sequence " + originalFilename);
        }

        // Walidacja MIME type
        String contentType = file.getContentType();
        List<String> allowedMimeTypes = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
        );

        if (contentType == null || !allowedMimeTypes.contains(contentType.toLowerCase())) {
            throw new InvalidFileTypeException(
                "Invalid file type. Only JPEG, PNG, GIF, and WebP images are allowed. Received: " + contentType
            );
        }

        // Generowanie unikalnej nazwy pliku i walidacja rozszerzenia
        String fileExtension = "";
        int lastDotIndex = originalFilename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            fileExtension = originalFilename.substring(lastDotIndex).toLowerCase();
        }

        // Walidacja rozszerzenia pliku
        List<String> allowedExtensions = Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".webp");
        if (!allowedExtensions.contains(fileExtension)) {
            throw new InvalidFileTypeException(
                "Invalid file extension. Only .jpg, .jpeg, .png, .gif, .webp are allowed. Received: " + fileExtension
            );
        }

        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

        try {
            // Kopiowanie pliku do docelowego katalogu
            Path targetLocation = this.fileStorageLocation.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return uniqueFilename;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + originalFilename, ex);
        }
    }

    @Override
    public Resource loadFileAsResource(String filename) {
        try {
            Path filePath = this.fileStorageLocation.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("File not found " + filename);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("File not found " + filename, ex);
        }
    }

    @Override
    public void deleteFile(String filename) {
        try {
            Path filePath = this.fileStorageLocation.resolve(filename).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new RuntimeException("Could not delete file " + filename, ex);
        }
    }
}
