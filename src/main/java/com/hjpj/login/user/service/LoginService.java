package com.hjpj.login.user.service;

import com.hjpj.login.social.kakao.KakaoService;
import com.hjpj.login.user.dto.LoginInfoDTO;
import com.hjpj.login.user.dto.UserDTO;
import com.hjpj.login.user.dto.UserLogDetail;
import com.hjpj.login.user.entity.User;
import com.hjpj.login.exception.CustomException;
import com.hjpj.login.exception.ErrorCode;
import com.hjpj.login.auth.jwt.JwtUtil;
import com.hjpj.login.redis.RedisRepository;
import com.hjpj.login.user.repository.UserRepository;
import com.hjpj.login.auth.service.TokenService;
import com.hjpj.login.common.CommonUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LoginService implements UserLogService{

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String clientId;

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final RedisRepository redisRepository;
    private final KakaoService kakaoService;

    /** 로그인 처리 */
    public UserLogDetail authUserLogin(HttpServletRequest request, LoginInfoDTO logInfo, HttpServletResponse response) {

        // 헤더 확인 후 userLogId 추출하기
        String credentials = initialHeaderCheckAndGetLogId(request);
        System.out.println("userLogId : " + credentials);

        // 해당 ID를 가진 사용자 찾아서 없는 경우 에러 날리기
        UserDTO user = userRepository.findUserByUserLogId(credentials).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 해당 사용자를 AUTH에 담을 UserLogDetail 형태로 만들기
        UserLogDetail userLogDetail = new UserLogDetail(user);
        userLogDetail.setAutoLogin(logInfo.getAutoLogin());

        // 인증 정보 만들기
        Authentication auth = new UsernamePasswordAuthenticationToken(userLogDetail, credentials, userLogDetail.getAuthorities());
        
        // SecurityContext에 설정
        SecurityContextHolder.getContext().setAuthentication(auth);

        // 토큰 및 쿠키 생성 후 access만 전달받기
        tokenService.makeTokenAndCookie(auth, userLogDetail,response);

        System.out.println(passwordEncoder.matches(logInfo.getUserLogPw(), userLogDetail.getUserLogPw()));

        if (passwordEncoder.matches(logInfo.getUserLogPw(), userLogDetail.getUserLogPw())) {
            System.out.println("성공적으로 로그인 로직 실행!");
            return userLogDetail;
        } else {
            System.out.println("비밀번호 일치하지 않음");
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }
    }

    /** 아이디 찾기 */
    public Map<String, String> findUserId(User user) {
        String userLogId = userRepository.findIdByUserInfo(user.getUserName(), user.getUserEmail(), user.getUserTel());

        if(userLogId == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        Map<String, String> result = new HashMap<>();
        result.put("userInfo", userLogId);

        return result;
    }

    /** 비밀번호 찾기 - 이메일 전달 */
    public Map<String, String> findUserPw(User user) {
        String userEmail = userRepository.findEmailByUserInfo(user.getUserLogId(), user.getUserEmail(), user.getUserTel());

        if(userEmail == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        Map<String, String> result = new HashMap<>();
        result.put("userInfo", userEmail);

        return result;
    }

    /** 자동 로그인 시 해당 정보 가져가기 */
    public UserDTO findUserForAuthLogin(HttpServletRequest request, HttpServletResponse response) {
        // 헤더 확인 후 userLogId 추출하기
        String userLogId = initialHeaderCheckAndGetLogId(request);

        UserDTO user = userRepository.findUserByUserLogId(userLogId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 토큰 찾아서 작업하기 (문제 있는 경우 throw 됨)
        tokenService.getAccessFromRefresh(user.getUserLogId(), response);

        return user;
    }

    /** 초기 헤더 정보 확인 및 userLogId 추출 */
    public String initialHeaderCheckAndGetLogId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        // 헤더 정보가 없는 경우
        if(authHeader == null || !authHeader.startsWith(JwtUtil.INITIAL_TYPE)) {
            throw new CustomException(ErrorCode.MISSING_TOKEN);
        }

        // 앞에 Basic을 제외한 정보 받기(회원 아이디 인코딩한 정보)
        String base64Credentials = authHeader.substring(JwtUtil.INITIAL_TYPE.length()).trim();

        return new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
    }

    @Override
    public void deleteTokenFromRedis(String userLogId) {
        redisRepository.deleteById(userLogId);
    }

    /** 로그아웃 (자체 후 redirect) */
    public Map<String, String> signOutAndRedirect(HttpSession session, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String loginType = request.getHeader("LoginType"); // 소셜 로그인 타입

        // 공통 로그아웃 처리 (Redis 및 쿠키 삭제)
        clearSessionAndCookies(request, response);

        // 리다이렉트 URL 설정
        String redirectUrl;
        switch (loginType) {
            case "KAKAO":
                redirectUrl = "https://kauth.kakao.com/oauth/logout?client_id=" + clientId + "&logout_redirect_uri=http://localhost:8080/login/generic";
                break;
            case "GOOGLE":
                redirectUrl = "추후 반영";
                break;
            case "NAVER":
                redirectUrl = "추후 반영";
                break;
            default:
                redirectUrl = "/login/generic";
        }

        // JSON 응답 생성
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("redirectUrl", redirectUrl);

        return responseBody;
    }

    /** 단순 로그아웃 (내부적으로 다 처리) */
    public void signOut(HttpSession session, HttpServletRequest request, HttpServletResponse response) {
        String loginType = request.getHeader("LoginType"); // 소셜 로그인 타입

        // 공통 로그아웃 처리 (Redis 및 쿠키 삭제)
        clearSessionAndCookies(request, response);

        switch (loginType) {
            case "KAKAO":
                kakaoService.signOut(session, request, response);
                break;
            case "GOOGLE":
                break;
            case "NAVER":
                break;
            default:
                break;
        }
    }

}
