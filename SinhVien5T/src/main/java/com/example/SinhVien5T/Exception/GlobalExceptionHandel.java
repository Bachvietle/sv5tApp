package com.example.SinhVien5T.Exception;

import com.example.SinhVien5T.Dto.Response.ApiResponse;
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
