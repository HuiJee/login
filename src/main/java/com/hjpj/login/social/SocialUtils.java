package com.hjpj.login.social;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hjpj.login.auth.service.TokenService;
import com.hjpj.login.user.dto.UserLogDetail;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public final class SocialUtils {

    private static TokenService tokenService;

    private SocialUtils() {
        // 인스턴스화 방지
    }

    @Autowired
    public void setTokenService(TokenService tokenService) {
        SocialUtils.tokenService = tokenService;
    }

    /** 소셜 토큰 가져오기 */
    public static String getAccessToken(String uri, String clientId, String clientSecret, String code) throws JsonProcessingException {
        String tokenResponse = WebClient.create(uri).post()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .path("")
                        .queryParam("grant_type", "authorization_code")
                        .queryParam("client_id", clientId)
                        .queryParam("client_secret", clientSecret)
                        .queryParam("code", code)
                        .build(true))
                .header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.error(new RuntimeException("Invalid Parameter")))
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> Mono.error(new RuntimeException("Internal Server Error")))
                .bodyToMono(String.class)
                .block();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(tokenResponse);

        return jsonNode.get("access_token").asText();
    }

    /** 소셜 로그인 용 인증정보 */
    public static void makeAuthAndSaveToken(UserLogDetail userLogDetail, HttpServletResponse response) {
        // 인증 정보 만들기
        Authentication auth = new UsernamePasswordAuthenticationToken(userLogDetail, userLogDetail.getUserLogId(), userLogDetail.getAuthorities());

        // SecurityContext에 설정
        SecurityContextHolder.getContext().setAuthentication(auth);

        // 자체 토큰 생성 및 저장
        tokenService.makeTokenAndCookie(auth, userLogDetail, response);
    }

}
