package com.hjpj.login.social.kakao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hjpj.login.social.SocialService;
import com.hjpj.login.social.SocialUtils;
import com.hjpj.login.user.dto.UserDTO;
import com.hjpj.login.user.dto.UserLogDetail;
import com.hjpj.login.user.entity.User;
import com.hjpj.login.user.repository.UserRepository;
import com.hjpj.login.common.CommonUtils;
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
public class KakaoServiceImpl implements SocialService {

    @Value("${spring.social.oauth.common.content-type.form-urlencoded}")
    private String contentTypeUrlEncoded;
    @Value("${spring.social.oauth.kakao.client-id}")
    private String clientId;
    @Value("${spring.social.oauth.kakao.client-secret}")
    private String clientSecret;
    @Value("${spring.social.oauth.kakao.token-uri}")
    private String kakaoTokenUri;
    @Value("${spring.social.oauth.kakao.user-info-uri}")
    private String kakaoUserInfoUri;
    @Value("${spring.social.oauth.kakao.signout-uri}")
    private String kakaoSignoutUri;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /** 카카오 콜백 이후 메서드 통합 */
    @Override
    public void callbackProcess(String code, HttpSession session, RedirectAttributes redirectAttributes, HttpServletResponse response) {

        // 코드 기반으로 토큰 발급
        String kakaoAccessToken = null;
        try {
            kakaoAccessToken = SocialUtils.getAccessToken(kakaoTokenUri, clientId, clientSecret, code);
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
            session.setAttribute(CommonUtils.KAKAO_TOKEN, kakaoAccessToken);
            redirectAttributes.addFlashAttribute("user", kakaoMember);
            redirectAttributes.addFlashAttribute("social", KAKAO);
        }
    }

    public KakaoInfoDTO getKakaoInfo(String accessToken) throws JsonProcessingException {

        String infoResponse = WebClient.create(kakaoUserInfoUri).post()
                .uri("")
                .header("Authorization", CommonUtils.TOKEN_BEARER + accessToken)
                .header("Content-Type", contentTypeUrlEncoded)
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
        Optional<UserDTO> kakaoMember = userRepository.findUserBySocialInfo(kakaoId, KAKAO);

        // 회원가입
        if (kakaoMember.isEmpty()) {
            String tempPassword = passwordEncoder.encode(UUID.randomUUID().toString());

            User newUser = new User(kakaoId, tempPassword, kakaoInfo.getNickname(), KAKAO);
            userRepository.save(newUser);

            kakaoMember = userRepository.findUserBySocialInfo(kakaoId, KAKAO);
        }

        SocialUtils.makeAuthAndSaveToken(new UserLogDetail(kakaoMember.get()), response);

        return kakaoMember.get();
    }

    @Override
    public void signOut(HttpSession session, HttpServletRequest request, HttpServletResponse response) {

        String accessToken = (String) session.getAttribute(CommonUtils.KAKAO_TOKEN);
        String userLogId = request.getHeader(CommonUtils.USER_LOG_ID_NAME);

        if(accessToken != null && !"".equals(accessToken)){
            String logoutId = WebClient.create(kakaoSignoutUri).post()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .path("")
                            .queryParam("target_id_type", "user_id")
                            .queryParam("target_id", userLogId)
                            .build(true))
                    .header("Authorization", CommonUtils.TOKEN_BEARER + accessToken)
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
