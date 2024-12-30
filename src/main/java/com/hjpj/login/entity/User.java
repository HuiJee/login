package com.hjpj.login.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.print.attribute.standard.MediaSize;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false, name = "user_id")
    private Long userId;

    @Column(name = "user_log_id")
    private String userLogId;

    @Column(name = "user_log_pw")
    private String userLogPw;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "user_role")
    private String userRole;

    @Column(name = "user_email")
    private String userEmail;

    @Column(name = "user_tel")
    private String userTel;

    @Column(name = "user_addr")
    private String userAddr;

    @Column(name = "user_nickname")
    private String userNickname;

    @Column(name = "user_create_date")
    private LocalDateTime userCreateDate;

    @Column(name = "user_update_date")
    private LocalDateTime userUpdateDate;

    @Column(name = "user_status")
    private Boolean userStatus;

    @PrePersist
    protected void onCreate() {
        userRole = "G"; // 초기값은 전부 게스트
        userStatus = true;
        userCreateDate = LocalDateTime.now();
        userUpdateDate = LocalDateTime.now();
    }

    public User(String userLogId, String userLogPw, String name) {
        this.userLogId = userLogId;
        this.userLogPw = userLogPw;
        this.userName = name;
        this.userNickname = name;
    }


}
