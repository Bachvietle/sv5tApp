package com.example.SinhVien5T.evidence.service;


import jakarta.transaction.Transactional;
import com.example.SinhVien5T.common.exception.EmptyFileException;
import com.example.SinhVien5T.common.exception.InvalidFileFormatException;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Primary
@Service
@RequiredArgsConstructor
public class LocalFileStorageServiceImpl implements FileStorageService{

    @Value("${app.localStorage}")
    private String storageUrl;

    @Value("${app.baseUrl:}")
    private String baseUrl;

    @Override
    public String storeFile(MultipartFile file) throws IOException {

        Path storageFolderPath = Path.of(storageUrl);

        // kta Directories storage đã tồn tại chưa
        if(Files.notExists(storageFolderPath)){
            Files.createDirectories(storageFolderPath);
        }

        // ktra file có trống ko
        if(file.isEmpty()){
            throw new EmptyFileException("File tải lên không được để trống");
        }

        // Tạo Tika để kiểm tra type thật của File thay vì lấy Content-Type header
       Tika tika = new Tika();

        String trueType = tika.detect(file.getInputStream());

        if(trueType == null || (!trueType.startsWith("image/") && !trueType.equals("application/pdf"))){
            throw new InvalidFileFormatException("File không hợp lệ. Type thật: " + trueType);
        }

        // Tạo thêm UUID ở đầu tránh lặp file
        String newFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

        // Đường dẫn đến thẳng file (thư mục gốc + tên file)
        Path storageNewFilePath = storageFolderPath.resolve(newFileName);

        // Lưu file
        Files.copy(file.getInputStream(), storageNewFilePath , StandardCopyOption.REPLACE_EXISTING);

        // lưu đường dẫn gọi thẳng đến file (FE gọi cho dễ, ko bị lỗi file)
        // Khi gọi đường dẫn này, Ngnix sẽ tự xuống storage vật lý lấy file, ko cần thông qua BE
        String evidenceUrl = baseUrl + "/evidence/uploads/" + newFileName;

        return evidenceUrl;
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            return;
        }

        try {
            int lastSlashIndex = fileUrl.lastIndexOf("/");
            String originFileName = lastSlashIndex >= 0 ? fileUrl.substring(lastSlashIndex + 1) : fileUrl;

            Path storageFilePath = Path.of(storageUrl).resolve(originFileName); // (thư mục gốc + tên file)

            Files.deleteIfExists(storageFilePath);
        } catch (Exception e) {
            // Không làm gián đoạn transaction nếu file vật lý bị khóa hoặc không tìm thấy
        }
    }

}
