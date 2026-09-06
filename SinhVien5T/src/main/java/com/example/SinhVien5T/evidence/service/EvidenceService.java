package com.example.SinhVien5T.evidence.service;

import com.example.SinhVien5T.campaign.entity.Criteria;
import com.example.SinhVien5T.campaign.repository.CriteriaRepository;
import com.example.SinhVien5T.evidence.dto.UploadEvidenceRequest;
import com.example.SinhVien5T.evidence.dto.UploadEvidenceResponse;
import com.example.SinhVien5T.evidence.entity.Evidence;
import com.example.SinhVien5T.evidence.repository.EvidenceRepository;
import com.example.SinhVien5T.user.entity.CustomUserDetails;
import com.example.SinhVien5T.user.entity.User;
import com.example.SinhVien5T.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.CachingUserDetailsService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor 
public class EvidenceService {

    private final FileStorageService fileStorageService;

    private final CriteriaRepository criteriaRepository;

    private final UserRepository userRepository;

    private final EvidenceRepository evidenceRepository;


    public UploadEvidenceResponse uploadFile(UploadEvidenceRequest request) throws IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();


        Criteria criteriaRef = criteriaRepository.getReferenceById(request.getCriteriaId());

        User userRef = userRepository.getReferenceById(user.getId());

        /*
        Ktra xem criteria này user đã upload chưa:
        - nếu rồi, xóa file cũ, lưu file mới, cập nhật lại evidenceUrl cũ.
        - nếu chưa, lưu file mới, tạo evidence mới.
         */
        Evidence evidenceExist = evidenceRepository.findByUserAndCriteria(userRef, criteriaRef);

        if (evidenceExist != null) {
            fileStorageService.deleteFile(evidenceExist.getEvidenceUrl());

            String evidenceUrl = fileStorageService.storeFile(request.getFile());

            evidenceExist.setEvidenceUrl(evidenceUrl);

            evidenceRepository.save(evidenceExist);

            return UploadEvidenceResponse.builder()
                    .evidenceId(evidenceExist.getId())
                    .criteriaId(criteriaRef.getId())
                    .evidenceUrl(evidenceUrl)
                    .status(true)
                    .build();
        } else {

            String evidenceUrl = fileStorageService.storeFile(request.getFile());

            Evidence evidence = Evidence.builder()
                    .criteria(criteriaRef)
                    .user(userRef)
                    .evidenceUrl(evidenceUrl)
                    .status(true)
                    .reviewerComment(null)
                    .build();

            Evidence evidenceSave = evidenceRepository.save(evidence);

            // build response
            return UploadEvidenceResponse.builder()
                    .evidenceId(evidenceSave.getId())
                    .criteriaId(criteriaRef.getId())
                    .evidenceUrl(evidenceUrl)
                    .status(true)
                    .build();
        }
    }


}


