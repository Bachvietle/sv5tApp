package com.example.SinhVien5T.evidence.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileStorageService {

    String storeFile(MultipartFile file) throws IOException;

    void deleteFile(String fileUrl) throws IOException;
}
