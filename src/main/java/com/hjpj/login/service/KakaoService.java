package com.hjpj.login.service;

import com.hjpj.login.dto.KakaoResponseDTO;
import com.hjpj.login.dto.KakaoUserInfoDTO;
import com.hjpj.login.dto.UserDTO;
import com.hjpj.login.dto.UserLogDetail;
import com.hjpj.login.entity.TokenRedis;
import com.hjpj.login.entity.User;
import com.hjpj.login.jwt.JwtUtil;
import com.hjpj.login.repository.RedisRepository;
import com.hjpj.login.repository.UserRepository;
import com.hjpj.login.util.CommonUtil;
import io.netty.handler.codec.http.HttpHeaderValues;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoService {

    @Value("${kakao.client-id}")
    private String clientId;
    @Value("${kakao.client-secret}")
    private String clientSecret;

    private final String KAUTH_TOKEN_URL_HOST = "https://kauth.kakao.com";
    private final String KAUTH_USER_URL_HOST = "https://kapi.kakao.com";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final RedisRepository redisRepository;

    // 카카오 로그인 및 회원가입 절차
    public UserLogDetail getTokenAndRegisterCheck(String code, HttpServletResponse response) {
        // 발급된 토큰 정보 가져오기
        KakaoResponseDTO kakaoToken = this.getTokenFromKakao(code);
        
        // 로그인 또는 회원 가입
        UserLogDetail userLogDetail = this.getUserInfo(kakaoToken.getAccessToken());
        
        // 토큰 저장 절차
        this.makeAuthAndSaveToken(userLogDetail, kakaoToken, response);

        return userLogDetail;
    }

    private KakaoResponseDTO getTokenFromKakao(String code) {

        KakaoResponseDTO kakaoResponseDTO = WebClient.create(KAUTH_TOKEN_URL_HOST).post()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .path("/oauth/token")
                        .queryParam("grant_type", "authorization_code")
                        .queryParam("client_id", clientId)
                        .queryParam("client_secret", clientSecret)
                        .queryParam("code", code)
                        .build(true))
                .header(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.error(new RuntimeException("Invalid Parameter")))
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> Mono.error(new RuntimeException("Internal Server Error")))
                .bodyToMono(KakaoResponseDTO.class)
                .block();

        log.info(" [Kakao Service] Access Token ------> {}", kakaoResponseDTO.getAccessToken());
        log.info(String.valueOf(kakaoResponseDTO.getExpiresIn()));
        log.info(" [Kakao Service] Refresh Token ------> {}", kakaoResponseDTO.getRefreshToken());
        log.info(String.valueOf(kakaoResponseDTO.getRefreshTokenExpiresIn()));
        //제공 조건: OpenID Connect가 활성화 된 앱의 토큰 발급 요청인 경우 또는 scope에 openid를 포함한 추가 항목 동의 받기 요청을 거친 토큰 발급 요청인 경우
        log.info(" [Kakao Service] Id Token ------> {}", kakaoResponseDTO.getIdToken());
        log.info(" [Kakao Service] Scope ------> {}", kakaoResponseDTO.getScope());

        return kakaoResponseDTO;
    }

    private UserLogDetail getUserInfo(String accessToken) {

        KakaoUserInfoDTO userInfo = WebClient.create(KAUTH_USER_URL_HOST)
                .get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .path("/v2/user/me")
                        .build(true))
                .header(HttpHeaders.AUTHORIZATION, JwtUtil.GRANT_TYPE + " " + accessToken) // access token 인가
                .header(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString())
                .retrieve()
                //TODO : Custom Exception
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.error(new RuntimeException("Invalid Parameter")))
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> Mono.error(new RuntimeException("Internal Server Error")))
                .bodyToMono(KakaoUserInfoDTO.class)
                .block();

        log.info("[ Kakao Service ] Auth ID ---> {} ", userInfo.getId());
        log.info("[ Kakao Service ] NickName ---> {} ", userInfo.getKakaoAccount().getProfile().getNickName());
        log.info("[ Kakao Service ] ProfileImageUrl ---> {} ", userInfo.getKakaoAccount().getProfile().getProfileImageUrl());

        return this.checkKakaoUser(userInfo);
    }

    private UserLogDetail checkKakaoUser(KakaoUserInfoDTO userInfo) {
        String kakaoId = CommonUtil.KAKAO + "_" + userInfo.getId();

        System.out.println("카카오 회원 ID : " + kakaoId);

//        userRepository.findUserByUserLogId(kakaoId)
//                .ifPresentOrElse(
//                        user -> log.info("카카오 회원 확인 완료: {}", kakaoId), // 이미 회원이 존재함
//                        () -> {
//                            System.out.println("해당 카카오 정보 존재하지 않음");
//                            String name = userInfo.getKakaoAccount().getProfile().getNickName();
//                            userRepository.save(new User(kakaoId, passwordEncoder.encode("KAKAO"), name));
//                            log.info("카카오 회원 신규 등록 완료: {}", kakaoId);
//                        }
//                );

//        return userRepository.findUserByUserLogId(kakaoId)
//                .orElseGet(() -> {
//                    String name = userInfo.getKakaoAccount().getProfile().getNickName();
//                    User savedUser = userRepository.save(new User(kakaoId, passwordEncoder.encode("KAKAO"), name));
//
//                    log.info("카카오 회원 신규 등록 완료: {}", kakaoId);
//                    return savedUser; // 새로 저장한 User 반환
//                });

        Optional<UserDTO> existingUser = userRepository.findUserByUserLogId(kakaoId);

        // 이미 존재하는 경우
        if (existingUser.isPresent()) {
            log.info("카카오 회원 확인 완료: {}", kakaoId);
            return new UserLogDetail(existingUser.get()); // 기존 사용자 반환
        }

        // 존재하지 않으면 새로 등록
        String name = userInfo.getKakaoAccount().getProfile().getNickName();
        User savedUser = userRepository.save(new User(kakaoId, passwordEncoder.encode("KAKAO"), name));  // 새로 등록한 사용자 저장

        log.info("카카오 회원 신규 등록 완료: {}", kakaoId);

        return new UserLogDetail(savedUser);
    }

    private void makeAuthAndSaveToken(UserLogDetail userLogDetail, KakaoResponseDTO kakaoResponseDTO, HttpServletResponse response) {
        // 인증 정보 만들기
        Authentication auth = new UsernamePasswordAuthenticationToken(userLogDetail, userLogDetail.getUserLogId(), userLogDetail.getAuthorities());

        // SecurityContext에 설정
        SecurityContextHolder.getContext().setAuthentication(auth);

        // AccessToken은 쿠키 저장
        tokenService.setTokenCookie(response, kakaoResponseDTO.getAccessToken());

        // refresh 토큰은 redis 서버에 저장
        redisRepository.save(new TokenRedis(userLogDetail.getUserLogId(), kakaoResponseDTO.getRefreshToken()));
    }

}
