package com.hjpj.login.dto;

import com.hjpj.login.entity.User;
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

    public UserDTO(User user) {
        this.userId = user.getUserId();
        this.userLogId = user.getUserLogId();
        this.userLogPw = user.getUserLogPw();
        this.userNickname = user.getUserNickname();
        this.userRole = user.getUserRole();
    }
}
