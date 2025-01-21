package com.hjpj.login.social;

import com.hjpj.login.auth.service.TokenService;
import com.hjpj.login.user.dto.UserLogDetail;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public interface SocialLogService {

    void signOut(HttpSession session, HttpServletRequest request, HttpServletResponse response);

    default void makeAuthAndSaveToken(UserLogDetail userLogDetail, HttpServletResponse response) {
        // 인증 정보 만들기
        Authentication auth = new UsernamePasswordAuthenticationToken(userLogDetail, userLogDetail.getUserLogId(), userLogDetail.getAuthorities());

        // SecurityContext에 설정
        SecurityContextHolder.getContext().setAuthentication(auth);

        // 자체 토큰 생성 및 저장
        getTokenService().makeTokenAndCookie(auth, userLogDetail, response);
    }

    // TokenService 주입을 위한 추상 메서드
    TokenService getTokenService();
}
