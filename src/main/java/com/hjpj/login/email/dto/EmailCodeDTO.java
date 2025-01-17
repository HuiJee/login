package com.hjpj.login.email.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EmailCodeDTO {

    private String email;
    private String code;

}
