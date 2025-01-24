package com.hjpj.login.user.service;

import com.hjpj.login.auth.jwt.JwtUtil;
import com.hjpj.login.common.CommonUtils;
import com.hjpj.login.exception.CustomException;
import com.hjpj.login.exception.ErrorCode;
import com.hjpj.login.user.dto.LoginInfoDTO;
import com.hjpj.login.user.dto.UserDTO;
import com.hjpj.login.user.dto.UserLogDetail;
import com.hjpj.login.user.entity.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

public interface LoginService {

    /** 로그인 처리 */
    UserLogDetail authUserLogin(HttpServletRequest request, LoginInfoDTO loginInfoDTO, HttpServletResponse response);

    /** 아이디 찾기 */
    Map<String, String> findUserId(User user);

    /** 비밀번호 찾기 */
    Map<String, String> findUserPw(User user);

    /** 로그아웃 (리다이렉트용)*/
    Map<String, String> signOutAndRedirect(HttpServletRequest request, HttpServletResponse response);

    /** 로그아웃 (일반)*/
    void signOut(HttpSession session, HttpServletRequest request, HttpServletResponse response);

    /** 자동 로그인 정보 가져오기 */
    UserDTO findUserForAuthLogin(HttpServletRequest request, HttpServletResponse response);

    /** RedisRepository 주입 후 토큰 삭제를 위한 추상 메서드 */
    void deleteTokenFromRedis(String userLogId);

    /** 로그 아웃 시 토큰 정리 */
    default void clearSessionAndCookies(HttpServletRequest request, HttpServletResponse response) {
        // Redis 토큰 삭제
        String userLogId = request.getHeader(CommonUtils.USER_LOG_ID_NAME);
        if (userLogId != null) {
            // Redis 삭제 로직 호출
            deleteTokenFromRedis(userLogId);
        }

        // 쿠키 삭제
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(CommonUtils.ACCESS_TOKEN)) {
                    cookie.setValue("");
                    cookie.setPath("/");
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                }
            }
        }
    }

    /** 초기 헤더 정보 확인 및 userLogId 추출 */
    default String initialHeaderCheckAndGetLogId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        // 헤더 정보가 없는 경우
        if(authHeader == null || !authHeader.startsWith(JwtUtil.INITIAL_TYPE)) {
            throw new CustomException(ErrorCode.MISSING_TOKEN);
        }

        // 앞에 Basic을 제외한 정보 받기(회원 아이디 인코딩한 정보)
        String base64Credentials = authHeader.substring(JwtUtil.INITIAL_TYPE.length()).trim();

        return new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
    }
}
