package com.example.SinhVien5T.common.exception;

import com.example.SinhVien5T.application.exception.ApplicationNotFoundException;
import com.example.SinhVien5T.application.exception.ApplicationStatusInvalidException;
import com.example.SinhVien5T.auth.exception.InvalidEmailDomainException;
import com.example.SinhVien5T.auth.exception.InvalidTokenException;
import com.example.SinhVien5T.common.dto.response.ApiResponse;
import com.example.SinhVien5T.evidence.exception.EvidenceLockedException;
import com.example.SinhVien5T.evidence.exception.EvidenceNotFoundException;
import com.example.SinhVien5T.evidence.exception.EvidencePermissionDeniedException;
import com.example.SinhVien5T.evidence.exception.EvidenceValidationException;
import com.example.SinhVien5T.evidence.exception.InvalidEvidenceStatusException;
import com.example.SinhVien5T.user.exception.EmailExistException;
import com.example.SinhVien5T.user.exception.ProfileUpdateException;
import com.example.SinhVien5T.user.exception.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandel {

    @ExceptionHandler(EmailExistException.class)
    public ResponseEntity<ApiResponse> handleEmailExistException(EmailExistException ex){
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ProfileUpdateException.class)
    public ResponseEntity<ApiResponse> handleProfileUpdateException(ProfileUpdateException ex){
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidEmailDomainException.class)
    public ResponseEntity<ApiResponse> handleInvalidEmailDomainException(InvalidEmailDomainException ex){
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse> handleConflictException(ConflictException ex){
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(EvidenceNotFoundException.class)
    public ResponseEntity<ApiResponse> handleEvidenceNotFoundException(EvidenceNotFoundException ex){
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EvidencePermissionDeniedException.class)
    public ResponseEntity<ApiResponse> handleEvidencePermissionDeniedException(EvidencePermissionDeniedException ex){
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(EvidenceLockedException.class)
    public ResponseEntity<ApiResponse> handleEvidenceLockedException(EvidenceLockedException ex){
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidEvidenceStatusException.class)
    public ResponseEntity<ApiResponse> handleInvalidEvidenceStatusException(InvalidEvidenceStatusException ex){
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EvidenceValidationException.class)
    public ResponseEntity<ApiResponse> handleEvidenceValidationException(EvidenceValidationException ex){
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ApplicationNotFoundException.class)
    public ResponseEntity<ApiResponse> handleApplicationNotFoundException(ApplicationNotFoundException ex){
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ApplicationStatusInvalidException.class)
    public ResponseEntity<ApiResponse> handleApplicationStatusInvalidException(ApplicationStatusInvalidException ex){
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse> handleIllegalArgumentException(IllegalArgumentException ex){
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiResponse> handleInvalidTokenException(InvalidTokenException ex){
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse> handleUserNotFoundException(UserNotFoundException ex){
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse> handleResourceNotFoundException(ResourceNotFoundException ex){
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EmptyFileException.class)
    public ResponseEntity<ApiResponse> handleEmptyFileException(EmptyFileException ex){
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidFileFormatException.class)
    public ResponseEntity<ApiResponse> handleInvalidFileFormatException(InvalidFileFormatException ex){
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse> handleBadCredentialsException(BadCredentialsException ex){
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    // Xử lý lỗi Validation (@Valid) -> 400
    // Cái này cực quan trọng để FE biết user nhập thiếu trường nào
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        // Trả về chuỗi lỗi đầu tiên hoặc list lỗi tùy bạn
        String errorMessage = "Dữ liệu không hợp lệ";

        if(!errors.isEmpty()){
            errorMessage = errors.values().iterator().next(); // Lấy lỗi đầu tiên
        }

        return new ResponseEntity<>(ApiResponse.error(errorMessage), HttpStatus.BAD_REQUEST);
    }

    // Xử lý lỗi hệ thống
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGlobalException(Exception ex){
        ex.printStackTrace(); // Log lỗi ra console sever để fix bug

        return new ResponseEntity<>(ApiResponse.error("Lỗi hệ thống" + ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
