package com.hjpj.login.social;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public interface SocialService {

    String GENERIC = "GENERIC";
    String KAKAO = "KAKAO";
    String NAVER = "NAVER";
    String GOOGLE = "GOOGLE";

    /** 소셜 로그인 관련 redirect callback 통합 메서드 */
    void callbackProcess(String code, HttpSession session, RedirectAttributes redirectAttributes, HttpServletResponse response) throws Exception;

    /** 소셜 로그아웃 */
    void signOut(HttpSession session, HttpServletRequest request, HttpServletResponse response);

}
