package com.hjpj.login.jwt;

import com.hjpj.login.exception.CustomException;
import com.hjpj.login.exception.ErrorCode;
import com.hjpj.login.util.CommonUtil;
import io.jsonwebtoken.Claims;
import io.lettuce.core.ScriptOutputType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    /*
        JWT 인증 필터를 구현한 클래스
        Spring Security의 필터 체인에서 사용되며, HTTP 요청에 포함된 JWT 토큰을 검사하여
        유효성을 확인하고 인증을 처리하는 역할을 한다.

        OncePerRequestFilter는 Spring의 필터로, HTTP 요청이 들어올 때마다 한 번만 실행되도록 보장!
    * */

    private final JwtProvider jwtProvider;

    // OncePerRequestFilter에서 제공하는 메서드로
    // 요청을 필터링하는 핵심 메서드! 토큰 유효성 검사를 수행하고 인증을 설정함
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();

        // 인증 예외 경로 및 서버 확인
        if (isExcludedPath(requestURI)) {
            chain.doFilter(request, response);
            return;
        }

        // 인증 예외 경로가 아닌 경우 access 토큰 확인
        String accessToken = jwtProvider.getTokenFromCookie(request);
        System.out.println("accessToken: " + accessToken);
        if(accessToken == null) {
            System.out.println("AccessToken 존재하지 않음");
            // AccessToken 발급을 위해 401 전송
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing access token");
        }

        // StringUtils.hasText : NULL 이거나 "" 이거나 공백만 포함하고 있으면 false 반환
        if(StringUtils.hasText(accessToken) && jwtProvider.validateToken(accessToken)) {
            Claims claims = jwtProvider.parseClaims(accessToken);
            String userLogIdFromToken = (String) claims.get(CommonUtil.USER_LOG_ID_NAME);
            String userLogId = request.getHeader(CommonUtil.USER_LOG_ID_NAME);

            if(!Objects.equals(userLogIdFromToken, userLogId)) {
                System.out.println("토큰 ID : " + userLogIdFromToken + " / 헤더 ID : " + userLogId + " => 불일치!");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not matching token info");
            }

            Authentication authentication = jwtProvider.getAuthentication(accessToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }else {
            System.out.println("만료된 토큰...");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Access Token");
            return;
        }

        System.out.println("Filter 진행 완료");
        chain.doFilter(request, response);
    }

    private boolean isExcludedPath(String requestURI) {
        return requestURI.equals("/index") ||
                requestURI.equals("/") ||
                requestURI.equals("/login/generic") ||          // 일반 로그인 페이지
                requestURI.equals("/login/social") ||           // 소셜 로그인 페이지
                requestURI.startsWith("/api/login/") ||        // 로그인 검증, 자동 로그인, 로그아웃
                requestURI.startsWith("/login/find/") ||        // 정보 찾기 페이지
                requestURI.startsWith("/social/") ||
                requestURI.equals("/user/profile") ||           // 프로필(로그인 결과 창)
                requestURI.equals("/user/register") ||           // 회원 가입 창
                requestURI.equals("/api/user/refresh-token") ||     // 리프레시 토큰으로 재발급
                requestURI.contains("/static/") ||
                requestURI.contains("/css/") ||
                requestURI.contains("/js/") ||
                requestURI.contains("/bootstrap/") ||
                requestURI.contains("/fonts/") ||
                requestURI.contains("/images/") ||
                requestURI.contains("/icons/") ||
                requestURI.contains("/favicon.ico");
    }

}
