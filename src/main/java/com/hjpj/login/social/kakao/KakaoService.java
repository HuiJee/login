package com.hjpj.login.social.kakao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hjpj.login.social.SocialLogService;
import com.hjpj.login.user.dto.UserDTO;
import com.hjpj.login.user.dto.UserLogDetail;
import com.hjpj.login.user.entity.User;
import com.hjpj.login.user.repository.UserRepository;
import com.hjpj.login.auth.service.TokenService;
import com.hjpj.login.common.CommonUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
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
public class KakaoService implements SocialLogService {

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String clientSecret;
    @Value("${spring.security.oauth2.client.registration.kakao.authorization-grant-type}")
    private String authorizationGrantType;
    @Value("${spring.security.oauth2.client.provider.kakao.token-uri}")
    private String kakaoTokenUri;
    @Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}")
    private String kakaoUserInfoUri;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @Override
    public TokenService getTokenService() {
        return tokenService;
    }

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
        KakaoInfoDTO kakaoInfo = null;
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
    }

    public String getAccessToken(String code) throws JsonProcessingException {
        String tokenResponse = WebClient.create(kakaoTokenUri).post()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .path("")
                        .queryParam("grant_type", authorizationGrantType)
                        .queryParam("client_id", clientId)
                        .queryParam("client_secret", clientSecret)
                        .queryParam("code", code)
                        .build(true))
                .header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.error(new RuntimeException("Invalid Parameter")))
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> Mono.error(new RuntimeException("Internal Server Error")))
                .bodyToMono(String.class)
                .block();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(tokenResponse);

        return jsonNode.get("access_token").asText();
    }

    public KakaoInfoDTO getKakaoInfo(String accessToken) throws JsonProcessingException {

        String infoResponse = WebClient.create(kakaoUserInfoUri).post()
                .uri("")
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

        return new KakaoInfoDTO(id, nickname, null);
    }

    public UserDTO ifNeedKakaoInfo (KakaoInfoDTO kakaoInfo, HttpServletResponse response) {
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

    @Override
    public void signOut(HttpSession session, HttpServletRequest request, HttpServletResponse response) {

        String accessToken = (String) session.getAttribute("kakaoToken");
        String userLogId = request.getHeader(CommonUtil.USER_LOG_ID_NAME);

        if(accessToken != null && !"".equals(accessToken)){
            String logoutId = WebClient.create("https://kapi.kakao.com/v1/user/logout").post()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .path("")
                            .queryParam("target_id_type", "user_id")
                            .queryParam("target_id", userLogId)
                            .build(true))
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.error(new RuntimeException("Invalid Parameter")))
                    .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> Mono.error(new RuntimeException("Internal Server Error")))
                    .bodyToMono(String.class)
                    .block();

            if(userLogId.equals(logoutId)){
                session.removeAttribute("kakaoToken");
            }

        }else{
            System.out.println("accessToken is null");
        }
    }

}
