package com.hjph.login.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.print.attribute.standard.MediaSize;

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
    private Character userRole;

    @Column(name = "user_email")
    private String userEmail;

    @Column(name = "user_tel")
    private String userTel;

    @Column(name = "user_nickname")
    private String userNickname;
}
