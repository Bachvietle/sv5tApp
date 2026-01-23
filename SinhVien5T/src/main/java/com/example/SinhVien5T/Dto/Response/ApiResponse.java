package com.example.SinhVien5T.Dto.Response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiResponse <T>{

    private String message;

    private T data;

    private boolean success;

    // Factory method
    public static <T> ApiResponse<T> success(String message, T data){
        return new ApiResponse<>(message, data, true);
    }

    public static <T> ApiResponse<T> error(String message){
        return new ApiResponse<>(message, null, false);
    }

}
