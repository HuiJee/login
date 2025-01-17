package com.hjpj.login.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hjpj.login.dto.UserDTO;
import com.hjpj.login.dto.UserLogDetail;
import com.hjpj.login.entity.User;
import com.hjpj.login.external.KakaoInfo;
import com.hjpj.login.repository.UserRepository;
import com.hjpj.login.util.CommonUtil;
import io.netty.handler.codec.http.HttpHeaderValues;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoService {

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String clientSecret;
    @Value("${spring.security.oauth2.client.registration.kakao.authorization-grant-type}")
    private String authorizationGrantType;

    private final String KAUTH_TOKEN_URL_HOST = "https://kauth.kakao.com";
    private final String KAUTH_USER_URL_HOST = "https://kapi.kakao.com";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    /** 카카오 콜백 이후 메서드 통합 */
    public void callbackProcess(String code, HttpSession session, RedirectAttributes redirectAttributes, HttpServletResponse response) {

        // 코드 기반으로 토큰 발급
        String kakaoAccessToken = null;
        try {
            kakaoAccessToken = this.getAccessToken(code);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        // 사용자의 카카오 정보 받기
        KakaoInfo kakaoInfo = null;
        try {
            kakaoInfo = this.getKakaoInfo(kakaoAccessToken);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        // 사용자 정보 확인
        UserDTO kakaoMember = this.ifNeedKakaoInfo(kakaoInfo, response);

        if(kakaoMember != null) {
            session.setAttribute("kakaoToken", kakaoAccessToken);
            redirectAttributes.addFlashAttribute("user", kakaoMember);
            redirectAttributes.addFlashAttribute("social", CommonUtil.KAKAO);
        }

        // 짧은 데이터 전달 : url에 parameter로 넣어서
        // 로그인 반복 정보 전달 :  session에 담아서 타임리프로 session.이름
        // 임시 데이터 전달 : Flash Attributes 사용하기
        // redirect의 경우 model로 전달하면 데이터가 넘어가지 않는다!!
    }

    public String getAccessToken(String code) throws JsonProcessingException {

        String tokenResponse = WebClient.create(KAUTH_TOKEN_URL_HOST).post()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .path("/oauth/token")
                        .queryParam("grant_type", authorizationGrantType)
                        .queryParam("client_id", clientId)
                        .queryParam("client_secret", clientSecret)
                        .queryParam("code", code)
                        .build(true))
                .header(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.error(new RuntimeException("Invalid Parameter")))
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> Mono.error(new RuntimeException("Internal Server Error")))
                .bodyToMono(String.class)
                .block();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(tokenResponse);

        return jsonNode.get("access_token").asText();
    }

    public KakaoInfo getKakaoInfo(String accessToken) throws JsonProcessingException {

        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        String infoResponse = WebClient.create(KAUTH_USER_URL_HOST).post()
                .uri("/v2/user/me")
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.error(new RuntimeException("Invalid Parameter")))
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> Mono.error(new RuntimeException("Internal Server Error")))
                .bodyToMono(String.class)
                .block();


        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(infoResponse);

        String id = jsonNode.get("id").asText();
        String nickname = jsonNode.get("properties")
                .get("nickname").asText();

        return new KakaoInfo(id, nickname, null);
    }

    public UserDTO ifNeedKakaoInfo (KakaoInfo kakaoInfo, HttpServletResponse response) {
        System.out.println("카카오 회원 정보 확인");
        String kakaoId = kakaoInfo.getId();
        Optional<UserDTO> kakaoMember = userRepository.findUserBySocialInfo(kakaoId, CommonUtil.KAKAO);

        // 회원가입
        if (kakaoMember.isEmpty()) {
            String tempPassword = passwordEncoder.encode(UUID.randomUUID().toString());

            User newUser = new User(kakaoId, tempPassword, kakaoInfo.getNickname(), CommonUtil.KAKAO);
            userRepository.save(newUser);

            kakaoMember = userRepository.findUserBySocialInfo(kakaoId, CommonUtil.KAKAO);
        }

        makeAuthAndSaveToken(new UserLogDetail(kakaoMember.get()), response);

        return kakaoMember.get();
    }

    private void makeAuthAndSaveToken(UserLogDetail userLogDetail, HttpServletResponse response) {
        // 인증 정보 만들기
        Authentication auth = new UsernamePasswordAuthenticationToken(userLogDetail, userLogDetail.getUserLogId(), userLogDetail.getAuthorities());

        // SecurityContext에 설정
        SecurityContextHolder.getContext().setAuthentication(auth);

        // 자체 토큰 생성 및 저장
        tokenService.makeTokenAndCookie(auth, userLogDetail, response);
    }

}
