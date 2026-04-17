package com.example.SinhVien5T.evidence.service;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.ConnectException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Primary
@Service
@RequiredArgsConstructor
public class LocalFileStorageServiceImpl implements FileStorageService{

    @Value("${app.localStorage}")
    private String storageUrl;



    @Override
    public String storeFile(MultipartFile file) throws IOException {

        Path storageFolderPath = Path.of(storageUrl);

        // kta Directories storage đã tồn tại chưa
        if(Files.notExists(storageFolderPath)){
            Files.createDirectories(storageFolderPath);
        }

        // ktra file có trống ko
        if(file.isEmpty()){
            throw new RuntimeException("");
        }

        if(file.getContentType() == null || (!file.getContentType().startsWith("image/") && !file.getContentType().equals("application/pdf"))){
            throw new ConnectException("Evidence đã được nộp");
        }

        // Tạo thêm UUID ở đầu tránh lặp file
        String newFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

        // Đường dẫn đến thẳng file
        Path storageNewFilePath = storageFolderPath.resolve(newFileName);

        // Lưu file
        Files.copy(file.getInputStream(), storageNewFilePath , StandardCopyOption.REPLACE_EXISTING);

        // lưu đường dẫn gọi thẳng đến file (FE gọi cho dễ, ko bị lỗi file)
        String evidenceUrl = "http://localhost:8080/evidence/uploads/" + newFileName;

        return evidenceUrl;
    }

    @Override
    public void deleteFile(String fileUrl) throws IOException {

        String originFileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);

        Path path = Path.of(originFileName);

        Path storageFolderPath = Path.of(storageUrl);
        Path storageFilePath = storageFolderPath.resolve(path);

        Files.delete(storageFilePath);
    }

}
