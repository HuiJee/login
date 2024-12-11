package com.hjpj.login.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

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
        System.out.println("여기는 JwtAuthenticationFilter");

        String requestURI = request.getRequestURI();

        System.out.println("현재 URI : " + requestURI);
        System.out.println("예외 경로 ? " + isExcludedPath(requestURI));

        // 인증 예외 경로 및 서버 확인
        if (isExcludedPath(requestURI)) {
            chain.doFilter(request, response);
            return;
        }

//        String token = jwtProvider.getTokenFromCookie(request, "accessToken");   // header의 Authorization에서 토큰 추출
//        System.out.println("현재 access 토큰 :" + token);
//        if (token == null) {
//            System.out.println("토큰 존재하지 않음");
//            // 토큰이 아예 존재하지 않으면 새로 발급 받아야 함 (401 전송)
//            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
//            return;
//        }
//
//        boolean valid = jwtProvider.validateToken(token);
//        System.out.println("토큰 유효성 : " + valid);
//
//        if (StringUtils.hasText(token) && valid) {
//            Claims claims = jwtProvider.parseClaims(token);
//            String userLogIdFromToken = (String) claims.get(CommonUtil.USER_ID_KEY);    // 토큰에서 추출한 id
//            String userLogId = resolveUsersLogId(request);  // header의 UserLogId에서 입력한 id 추출
//
//            if (!Objects.equals(userLogIdFromToken, userLogId)) {
//                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid Login ID");
//                System.out.println("userLogIdFromToken과 usersLogId 불일치 : " + userLogId + "," + userLogIdFromToken);
//                return;
//            }
//
//            Authentication authentication = jwtProvider.getAuthentication(token);
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//        } else {
//            System.out.println("Invalid access token");
//            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid access token");
//            System.out.println("response 상태 : " + response.getStatus());
//            return;
//        }
//        System.out.println("성공적으로 Filter 진행 완료");
//        chain.doFilter(request, response);
    }

    private boolean isExcludedPath(String requestURI) {
        return requestURI.equals("/index") ||
                requestURI.equals("/") ||
                requestURI.equals("/login/generic") ||          // 일반 로그인 페이지
                requestURI.equals("/login/social") ||           // 소셜 로그인 페이지
                requestURI.equals("/login/authentic") ||        // 로그인 검증 api
                requestURI.startsWith("/login/find/") ||        // 정보 찾기 페이지
                requestURI.startsWith("/login/api/") ||    // 정보 찾기 api
                requestURI.equals("/login/result") ||           // 로그인 결과 창
                requestURI.contains("/static/") ||
                requestURI.contains("/css/") ||
                requestURI.contains("/js/") ||
                requestURI.contains("/bootstrap/") ||
                requestURI.contains("/fonts/") ||
                requestURI.contains("/images/") ||
                requestURI.contains("/icons/") ||
                requestURI.contains("/favicon.ico");
    }

//    public static String resolveUsersLogId(HttpServletRequest request) {
//        return request.getHeader(CommonUtil.USER_ID_KEY);
//    }

//    private void addSameSite(HttpServletResponse response, String sameSite) {
//        Collection<String> headers = response.getHeaders(HttpHeaders.SET_COOKIE);
//        boolean firstHeader = true;
//        for (String header : headers) { // there can be multiple Set-Cookie attributes
//            if (firstHeader) {
//                response.setHeader(HttpHeaders.SET_COOKIE, String.format("%s; Secure; %s", header, "SameSite=" + sameSite));
//                firstHeader = false;
//                continue;
//            }
//            response.addHeader(HttpHeaders.SET_COOKIE, String.format("%s; Secure; %s", header, "SameSite=" + sameSite));
//        }
//    }

}
