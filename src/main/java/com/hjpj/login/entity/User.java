package com.hjpj.login.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.print.attribute.standard.MediaSize;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
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

    @Column(name = "user_nickname")
    private String userNickname;

    @Column(name = "user_create_date")
    private LocalDateTime userCreateDate;

    @Column(name = "user_update_date")
    private LocalDateTime userUpdateDate;

    @Column(name = "user_status")
    private String userStatus;

    @PrePersist
    protected void onCreate() {
        userStatus = "Y";
        userCreateDate = LocalDateTime.now();
        userUpdateDate = LocalDateTime.now();
    }


}
