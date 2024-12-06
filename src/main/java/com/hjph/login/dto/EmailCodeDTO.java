package com.hjph.login.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EmailCodeDTO {

    private String email;
    private String code;

}
