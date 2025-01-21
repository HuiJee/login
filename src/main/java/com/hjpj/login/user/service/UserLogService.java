package com.hjpj.login.user.service;

import com.hjpj.login.common.CommonUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface UserLogService {

    void deleteTokenFromRedis(String userLogId);

    default void clearSessionAndCookies(HttpServletRequest request, HttpServletResponse response) {
        // Redis 토큰 삭제
        String userLogId = request.getHeader(CommonUtil.USER_LOG_ID_NAME);
        if (userLogId != null) {
            // Redis 삭제 로직 호출
            deleteTokenFromRedis(userLogId);
        }

        // 쿠키 삭제
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(CommonUtil.ACCESS_TOKEN)) {
                    cookie.setValue("");
                    cookie.setPath("/");
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                }
            }
        }
    }
}
