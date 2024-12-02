package com.hjph.login.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    EXIST_SAME_ID(HttpStatus.CONFLICT, 409, "이미 존재하는 아이디입니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, 401, "비밀번호가 일치하지 않습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, 404, "회원 정보를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final Integer code;
    private final String message;

}
