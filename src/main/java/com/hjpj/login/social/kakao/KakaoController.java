package com.hjpj.login.social.kakao;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
@RequiredArgsConstructor
@RequestMapping("/oauth/kakao/")
public class KakaoController {

    @Value("${spring.social.oauth.kakao.client-id}")
    private String clientId;
    @Value("${spring.social.oauth.kakao.redirect-uri}")
    private String redirectUri;
    @Value("${spring.social.oauth.kakao.authorization-uri}")
    private String authorizationUri;

    private final KakaoServiceImpl kakaoService;


    @GetMapping("code")
    public void getKakaoCode(HttpServletResponse response) throws IOException {
        // 계정과 함께 로그아웃인 경우
        String location = authorizationUri + "?response_type=code&client_id="+clientId+"&redirect_uri="+redirectUri;
        // 그냥 로그아웃인 경우 로그인 재입력창 나오도록
        // String location = authorizationUri + "?response_type=code&client_id="+clientId+"&redirect_uri="+redirectUri+"&prompt=login";
        response.sendRedirect(location);
    }

    @GetMapping("callback")
    public String kakaoCallback(String code, HttpSession session, RedirectAttributes redirectAttributes, HttpServletResponse response) {
        kakaoService.callbackProcess(code, session, redirectAttributes, response);
        return "redirect:/user/profile";
    }
}
