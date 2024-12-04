package com.hjph.login.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ErrorResponseDTO {

    private final Integer code;
    private final String message;
    private final LocalDateTime timestamp;

    public ErrorResponseDTO(CustomException e) {
        this.code = e.getErrorCode().getCode();
        this.message = e.getErrorCode().getMessage();
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponseDTO(Integer code, String message) {
        this.code = code;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

}




