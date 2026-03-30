package com.example.SinhVien5T.Service;


import com.example.SinhVien5T.Exception.InvalidFileException;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service

public class FileStorageService {
    @Value("${app.upload.dir}")
    private String uploadDir;

    private static final List<String> ALLOWED_TYPES = List.of(
            "image/jpeg", "image/jpg", "image/png", "image/webp"
    );

    private static final long MAX_SIZE = 5 * 1024 * 1024;

    public String saveAvatar(MultipartFile file) {

        // Validate null and empty
        if(file == null || file.isEmpty()) {
            throw new InvalidFileException("File ảnh không được để trống");
        }

        // Validate định dạng file
        if(!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new InvalidFileException("Chỉ chấp nhận file có định dạng .jpg, .png, .webp");

        }
        // Validate kích thước file
        if(file.getSize() > MAX_SIZE) {
            throw new InvalidFileException("Kích thước file không vượt quá 5MB");
        }

        try {
            Path uploadPath = Paths.get(uploadDir);
            if(!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String extension = getExtension(file.getOriginalFilename());
            String fileName = UUID.randomUUID().toString() + "." + extension;

            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return uploadDir + "/" + fileName;
        } catch(IOException e) {
            throw new InvalidFileException("Fail to save the file: " + e.getMessage());
        }


    }

    public void deleteAvatar(String filePath) {
        if(filePath == null || filePath.isBlank()) return;

        try {
            Path path = Paths.get(filePath);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new InvalidFileException("Fail to delete the file: " + e.getMessage());
        }
    }

    public String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) return "jpg";
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }
}
