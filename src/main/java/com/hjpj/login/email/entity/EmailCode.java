package com.hjpj.login.email.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class EmailCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "email_code_id")
    private Long emailCodeId;

    @Column(name = "email")
    private String email;

    @Column(name = "code")
    private String code;

    @Column(name = "create_date")
    private LocalDateTime createDate;

    @Column(name = "expire_date")
    private LocalDateTime expireDate;

    @PrePersist
    protected void onCreate() {
        createDate = LocalDateTime.now();
        expireDate = LocalDateTime.now().plusMinutes(5);
    }

    public EmailCode(String email, String code) {
        this.email = email;
        this.code = code;
    }

}
