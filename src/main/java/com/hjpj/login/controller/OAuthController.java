package com.hjpj.login.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hjpj.login.dto.UserDTO;
import com.hjpj.login.dto.UserLogDetail;
import com.hjpj.login.external.KakaoInfo;
import com.hjpj.login.service.KakaoService;
import com.hjpj.login.service.LoginService;
import com.hjpj.login.util.CommonUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
@RequiredArgsConstructor
@RequestMapping("oauth/")
public class OAuthController {

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String kakaoClientId;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String kakaoRedirectUri;

    private final KakaoService kakaoService;
    private final LoginService loginService;

    @GetMapping("kakao/code")
    public void getKakaoCode(HttpServletResponse response) throws IOException {
        System.out.println("카카오 코드 발급");
//        kakaoService.getCodeFromKakao();
//        System.out.println("code : " + code);
        String location = "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id="+kakaoClientId+"&redirect_uri="+kakaoRedirectUri;
        response.sendRedirect(location);
    }

//    @GetMapping("kakao")
//    public void getTokenAndRegisterCheck(@RequestParam("code") String code, HttpServletResponse response) throws IOException {
//        System.out.println("카카오 토큰 발급");
//        UserLogDetail user = kakaoService.getTokenAndRegisterCheck(code, response);
////        return ResponseEntity.ok(user);
//        response.sendRedirect("http://localhost:8080/user/profile");
////        return "/user/profile";
//    }

    @GetMapping("/kakao/callback")
    public String kakaoCallback(String code, HttpSession session, RedirectAttributes redirectAttributes, HttpServletResponse response) {

        System.out.println("콜백 들어오기");

        // SETP1 : 인가코드 받기
        // (카카오 인증 서버는 서비스 서버의 Redirect URI로 인가 코드를 전달합니다.)

        // STEP2: 인가코드를 기반으로 토큰(Access Token) 발급
        String accessToken = null;
        try {
            System.out.println("토큰 받기");
            accessToken = kakaoService.getAccessToken(code);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        // STEP3: 토큰를 통해 사용자 정보 조회
        KakaoInfo kakaoInfo = null;
        try {
            System.out.println("정보 받기");
            kakaoInfo = kakaoService.getKakaoInfo(accessToken);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        // STEP4: 카카오 사용자 정보 확인
        UserDTO kakaoMember = kakaoService.ifNeedKakaoInfo(kakaoInfo, response);

        // STEP5: 강제 로그인
        // 세션에 회원 정보 저장 & 세션 유지 시간 설정
//        if (kakaoMember != null) {
//            session.setAttribute("loginMember", kakaoMembesr);
//            // session.setMaxInactiveInterval( ) : 세션 타임아웃을 설정하는 메서드
//            // 로그인 유지 시간 설정 (1800초 == 30분)
//            session.setMaxInactiveInterval(60 * 30);
//            // 로그아웃 시 사용할 카카오토큰 추가
//            session.setAttribute("kakaoToken", accessToken);
//        }
        if(kakaoMember != null) {
            session.setAttribute("kakaoToken", accessToken);
            redirectAttributes.addFlashAttribute("user", kakaoMember);
            redirectAttributes.addFlashAttribute("social", CommonUtil.KAKAO);
        }

        // 짧은 데이터 전달 : url에 parameter로 넣어서
        // 로그인 반복 정보 전달 :  session에 담아서 타임리프로 session.이름
        // 임시 데이터 전달 : Flash Attributes 사용하기
        // redirect의 경우 model로 전달하면 데이터가 넘어가지 않는다!!

        return "redirect:/user/profile";
    }

    @GetMapping("/kakao/sign-out")
    public ResponseEntity<?> kakaoSignOut(HttpSession session, HttpServletRequest request, HttpServletResponse response) {
        String accessToken = (String) session.getAttribute("kakaoToken");

        if(accessToken != null && !"".equals(accessToken)){
            try {
                loginService.signOut(request, response);
                kakaoService.kakaoDisconnect(accessToken);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            session.removeAttribute("kakaoToken");
        }else{
            System.out.println("accessToken is null");
        }

        return ResponseEntity.ok().build();
    }
}
