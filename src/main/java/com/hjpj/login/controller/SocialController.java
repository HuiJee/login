package com.hjpj.login.controller;

import com.hjpj.login.dto.UserLogDetail;
import com.hjpj.login.service.KakaoService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("social/")
public class SocialController {

    @Value("${kakao.client-id}")
    private String kakaoClientId;

    @Value("${kakao.redirect-uri}")
    private String kakaoRedirectUri;

    private final KakaoService kakaoService;

    @GetMapping("kakao/code")
    public void getKakaoCode(HttpServletResponse response) throws IOException {
        System.out.println("카카오 코드 발급");
        String location = "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id="+kakaoClientId+"&redirect_uri="+kakaoRedirectUri;
        response.sendRedirect(location);
    }

    @GetMapping("kakao/token")
    public ResponseEntity<?> getTokenAndRegisterCheck(@RequestParam("code") String code, HttpServletResponse response) throws IOException {
        System.out.println("카카오 토큰 발급");
        UserLogDetail user = kakaoService.getTokenAndRegisterCheck(code, response);
        return ResponseEntity.ok(user);
    }


}
