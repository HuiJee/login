package com.hjpj.login.dto;

import lombok.*;

@Getter
@Builder
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
