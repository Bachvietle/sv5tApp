package com.example.SinhVien5T.Exception;

public class EmailExistException extends RuntimeException {

    public EmailExistException(String message){
        super(message);
    }
}
