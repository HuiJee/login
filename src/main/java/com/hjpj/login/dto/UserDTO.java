package com.hjpj.login.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    private Long userId;
    private String userLogId;
    private String userLogPw;
    private String userNickname;
    private String userRole;
}
