package com.hjpj.login.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hjpj.login.dto.KakaoResponseDTO;
import com.hjpj.login.dto.KakaoUserInfoDTO;
import com.hjpj.login.dto.UserDTO;
import com.hjpj.login.dto.UserLogDetail;
import com.hjpj.login.entity.User;
import com.hjpj.login.external.KakaoInfo;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
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
    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String redirectUri;
    @Value("${spring.security.oauth2.client.provider.kakao.authorization-uri}")
    private String authorizationUri;
    @Value("${spring.security.oauth2.client.provider.kakao.token-uri}")
    private String tokenUri;
    @Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}")
    private String userInfoUri;


    private final String KAUTH_TOKEN_URL_HOST = "https://kauth.kakao.com";
    private final String KAUTH_USER_URL_HOST = "https://kapi.kakao.com";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    // 카카오 로그인 및 회원가입 절차
//    public UserLogDetail getTokenAndRegisterCheck(String code, HttpServletResponse response) {
//        // 발급된 토큰 정보 가져오기
//        KakaoResponseDTO kakaoToken = this.getTokenFromKakao(code);
//
//        // 로그인 또는 회원 가입
//        UserLogDetail userLogDetail = this.getUserInfo(kakaoToken.getAccessToken());
//
//        // 자체 토큰 발급 및 저장
//        this.makeAuthAndSaveToken(userLogDetail, response);
//
//        return userLogDetail;
//    }

    public void getCodeFromKakao() {
//        String code = WebClient.create(KAUTH_TOKEN_URL_HOST).get()
//                .uri(uriBuilder -> uriBuilder
//                        .scheme("https")
//                        .path("/oauth/authorize")
//                        .queryParam("response_type", "code")
//                        .queryParam("client_id", clientId)
//                        .queryParam("redirect_uri", redirectUri)
//                        .build(true))
//                .header(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString())
//                .retrieve()
//                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.error(new RuntimeException("Invalid Parameter")))
//                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> Mono.error(new RuntimeException("Internal Server Error")))
//                .bodyToMono(String.class)
//                .block();

        RestTemplate restTemplate = new RestTemplate();
        String tokenUrl = KAUTH_TOKEN_URL_HOST + "/oauth/authorize";

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("response_type", "code");
        params.add("client_id", clientId);
//        params.add("redirect_uri", redirectUri);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

        Map responseBody = response.getBody();
        System.out.println("코드 결과 : " + responseBody);
//        String accessToken = (String) responseBody.get("access_token");

//        System.out.println("실행 중 코드... " + code);

//        return code;
    }

//    private String getKakaoAccessToken(String code) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("Content-Type", "application/x-www-form-urlencoded;charset-utf-8");
//
//        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
//        params.add("grant_type", authorizationGrantType);
//        params.add("client_id", clientId);
//        params.add("client_secret", clientSecret);
//        params.add("code", code);
//
//        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
//        RestTemplate restTemplate = new RestTemplate();
//        ResponseEntity<String> response = restTemplate.exchange(
//                "https://kauth.kakao.com/oauth/token",
//                HttpMethod.POST,
//                KakaoResponseDTO,
//                String.class
//        )
//    }

    public String getAccessToken(String code) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP Body 생성
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("redirect_uri", redirectUri);
        body.add("code", code);
        body.add("client_secret", clientSecret);

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest = new HttpEntity<>(body, headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                tokenUri,
                HttpMethod.POST,
                kakaoTokenRequest,
                String.class
        );

        // HTTP 응답 (JSON) -> 액세스 토큰 파싱
        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);

        // 자동 로그인을 하면 refresh 토큰이 1달이라고 함.. 그거에 따라 자동로그인 처리 필요!
        System.out.println(jsonNode.get("refresh_token_expires_in"));
        System.out.println(JwtUtil.REFRESH_TOKEN_VALIDITY);

        return jsonNode.get("access_token").asText();
    }

    public KakaoInfo getKakaoInfo(String accessToken) throws JsonProcessingException {
        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> kakaoUserInfoRequest = new HttpEntity<>(headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                userInfoUri,
                HttpMethod.POST,
                kakaoUserInfoRequest,
                String.class
        );

        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);

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


    private String getTokenFromKakao(String code) {

        KakaoResponseDTO kakaoResponseDTO = WebClient.create(KAUTH_TOKEN_URL_HOST).post()
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
                .bodyToMono(KakaoResponseDTO.class)
                .block();

        log.info(" [Kakao Service] Access Token ------> {}", kakaoResponseDTO.getAccessToken());
        log.info(String.valueOf(kakaoResponseDTO.getExpiresIn()));
        log.info(" [Kakao Service] Refresh Token ------> {}", kakaoResponseDTO.getRefreshToken());
        log.info(String.valueOf(kakaoResponseDTO.getRefreshTokenExpiresIn()));
        //제공 조건: OpenID Connect가 활성화 된 앱의 토큰 발급 요청인 경우 또는 scope에 openid를 포함한 추가 항목 동의 받기 요청을 거친 토큰 발급 요청인 경우
        log.info(" [Kakao Service] Id Token ------> {}", kakaoResponseDTO.getIdToken());
        log.info(" [Kakao Service] Scope ------> {}", kakaoResponseDTO.getScope());

        return kakaoResponseDTO.getAccessToken();
    }

//    private UserLogDetail getUserInfo(String accessToken) {
//
//        KakaoUserInfoDTO userInfo = WebClient.create(KAUTH_USER_URL_HOST)
//                .get()
//                .uri(uriBuilder -> uriBuilder
//                        .scheme("https")
//                        .path("/v2/user/me")
//                        .build(true))
//                .header(HttpHeaders.AUTHORIZATION, JwtUtil.GRANT_TYPE + " " + accessToken) // access token 인가
//                .header(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString())
//                .retrieve()
//                //TODO : Custom Exception
//                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.error(new RuntimeException("Invalid Parameter")))
//                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> Mono.error(new RuntimeException("Internal Server Error")))
//                .bodyToMono(KakaoUserInfoDTO.class)
//                .block();
//
//        log.info("[ Kakao Service ] Auth ID ---> {} ", userInfo.getId());
//        log.info("[ Kakao Service ] NickName ---> {} ", userInfo.getKakaoAccount().getProfile().getNickName());
//        log.info("[ Kakao Service ] ProfileImageUrl ---> {} ", userInfo.getKakaoAccount().getProfile().getProfileImageUrl());
//
//        return this.checkKakaoUser(userInfo);
//    }

//    private UserLogDetail checkKakaoUser(KakaoUserInfoDTO userInfo) {
//        String kakaoId = CommonUtil.KAKAO + "_" + userInfo.getId();
//
//        System.out.println("카카오 회원 ID : " + kakaoId);

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

//        Optional<UserDTO> existingUser = userRepository.findUserByUserLogId(kakaoId);
//
//        // 이미 존재하는 경우
//        if (existingUser.isPresent()) {
//            log.info("카카오 회원 확인 완료: {}", kakaoId);
//            return new UserLogDetail(existingUser.get()); // 기존 사용자 반환
//        }
//
//        // 존재하지 않으면 새로 등록
//        String name = userInfo.getKakaoAccount().getProfile().getNickName();
//        User savedUser = userRepository.save(new User(kakaoId, passwordEncoder.encode("KAKAO"), name));  // 새로 등록한 사용자 저장
//
//        log.info("카카오 회원 신규 등록 완료: {}", kakaoId);
//
//        return new UserLogDetail(savedUser);
//    }

    private void makeAuthAndSaveToken(UserLogDetail userLogDetail, HttpServletResponse response) {
        // 인증 정보 만들기
        Authentication auth = new UsernamePasswordAuthenticationToken(userLogDetail, userLogDetail.getUserLogId(), userLogDetail.getAuthorities());

        // SecurityContext에 설정
        SecurityContextHolder.getContext().setAuthentication(auth);

        // 자체 토큰 생성 및 저장
        tokenService.makeTokenAndCookie(auth, userLogDetail, response);
    }

    public void kakaoDisconnect(String accessToken) throws JsonProcessingException {
        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "KakaoAK " + clientId);
//        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded");

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> kakaoLogoutRequest = new HttpEntity<>(headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                "https://kapi.kakao.com/v1/user/unlink",
                HttpMethod.POST,
                kakaoLogoutRequest,
                String.class
        );

        // responseBody에 있는 정보를 꺼냄
        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);

        Long id = jsonNode.get("id").asLong();
        System.out.println("반환된 id: "+id);
    }
}
