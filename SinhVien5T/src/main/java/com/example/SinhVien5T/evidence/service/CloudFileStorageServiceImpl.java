package com.example.SinhVien5T.evidence.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CloudFileStorageServiceImpl implements FileStorageService{
    @Override
    public String storeFile(MultipartFile file) {
        return "";
    }

    @Override
    public void deleteFile(String fileUrl) {

    }
}
