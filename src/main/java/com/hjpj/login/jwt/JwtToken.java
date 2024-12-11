package com.hjpj.login.jwt;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class JwtToken {

    private String grantType;
    private String accessToken;
    private String refreshToken;

    public JwtToken(String grantType, String accessToken, String refreshToken) {
        this.grantType = grantType;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
