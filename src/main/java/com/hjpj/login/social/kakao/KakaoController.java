package com.hjpj.login.social.kakao;

import com.hjpj.login.user.service.LoginService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
@RequiredArgsConstructor
@RequestMapping("/oauth/kakao/")
public class KakaoController {

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String kakaoClientId;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @Value("${spring.security.oauth2.client.provider.kakao.authorization-uri}")
    private String kakaoAuthorizationUri;

    private final KakaoService kakaoService;
    private final LoginService loginService;

    @GetMapping("code")
    public void getKakaoCode(HttpServletResponse response) throws IOException {
        System.out.println("카카오 코드 발급");
        // 연결 끊기 없이 로그아웃 시 재로그인을 돕고자... prompt = login 집어넣기
        String location = kakaoAuthorizationUri + "?response_type=code&client_id="+kakaoClientId+"&redirect_uri="+kakaoRedirectUri+"&prompt=login";
        response.sendRedirect(location);
    }

    @GetMapping("callback")
    public String kakaoCallback(String code, HttpSession session, RedirectAttributes redirectAttributes, HttpServletResponse response) {
        System.out.println("콜백 들어오기");
        kakaoService.callbackProcess(code, session, redirectAttributes, response);
        return "redirect:/user/profile";
    }

    @GetMapping("sign-out")
    public ResponseEntity<?> kakaoSignOut(HttpSession session, HttpServletRequest request, HttpServletResponse response) {
        String accessToken = (String) session.getAttribute("kakaoToken");

        System.out.println("카카오 accessToken : " + accessToken);

        if(accessToken != null && !"".equals(accessToken)){
            loginService.signOut(request, response);
            session.removeAttribute("kakaoToken");
        }else{
            System.out.println("accessToken is null");
        }

        return ResponseEntity.ok().build();
    }
}
