package com.hjph.login.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** HTTP 요청이 지원되지 않는 메서드인 경우 */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponseDTO> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.error("HttpRequestMethodNotSupportedException: ", e);

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(405, "지원하지 않는 HTTP 메서드입니다.");

        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(errorResponse);
    }

    /** 접근 권한 거부 */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDeniedException(AccessDeniedException e) {
        log.error("AccessDeniedException: ", e);

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(403, "접근 권한이 존재하지 않습니다.");

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(errorResponse);
    }

    /** 데이터 베이스 접근 시 발생하는 예외 */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponseDTO> handleDataAccessException(DataAccessException e) {
        log.error("DataAccessException: ", e);

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(500, "데이터베이스 처리 중 문제가 발생했습니다.");

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }

    /** 예상치 못한 모든 예외 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGlobalException(Exception e) {
        log.error("Unexpected error: ", e);

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(500, "예상치 못한 오류가 발생했습니다.");

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }

    /** 파라미터 부족 */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponseDTO> handleMissingServletRequestParameterException(Exception e) {
        log.error("MissingServletRequestParameterException: ", e);

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(4001, "필수 요청 파라미터가 부족합니다.");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /** 경로 변수 부족 */
    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<ErrorResponseDTO> handleMissingPathVariableException(Exception e) {
        log.error("MissingPathVariableException: ", e);

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(4002, "필수 경로 파라미터가 부족합니다.");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /** 커스텀 예외 */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponseDTO> handleCustomException(CustomException e) {
        log.error("CustomException: ", e);

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(e);

        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(errorResponse);
    }

}