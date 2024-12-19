package com.hjpj.login.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    private Long userId;
    private String userLogId;
    private String userLogPw;
    private String userNickname;
    @Setter
    private String userRole;
}
