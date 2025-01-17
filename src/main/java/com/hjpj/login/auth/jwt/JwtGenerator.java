package com.hjpj.login.auth.jwt;

import com.hjpj.login.user.dto.UserLogDetail;
import com.hjpj.login.common.CommonUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtGenerator {

    private final Key key;

    /** application.yaml 에서 secret 값 가져와 key에 저장 */
    public JwtGenerator(@Value("${jwt.secret}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /** 로그인 시 받아온 header 정보를 통해 AccessToken, RefreshToken 생성 */
    public JwtToken generateToken(Authentication authentication, Boolean autoLogin) {
        // 권한 가져오기
        String authoritites = getAuthorities(authentication);

        // authentication에 담아놓은 계정 정보 추출
        UserLogDetail userLogDetail = (UserLogDetail) authentication.getPrincipal();

        // 최초 accessToken 생성
        String accessToken = generateToken(authoritites, userLogDetail, JwtUtil.ACCESS_TOKEN_VALIDITY);

        // RefreshToken 생성 (자동 로그인 체크 여부에 따라 만료 별도 설정)
        long refreshTokenValid = autoLogin ? JwtUtil.REFRESH_TOKEN_VALIDITY : JwtUtil.SHORT_REFRESH_VALIDITY;
        String refreshToken = generateToken(authoritites, userLogDetail, refreshTokenValid);

        return JwtToken.builder()
                .grantType(JwtUtil.GRANT_TYPE)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /** 토큰 생성하기 */
    public String generateToken(String authorities, UserLogDetail userLogDetail, long validTime) {
        return Jwts.builder()
                .setSubject(userLogDetail.getUserLogId())
                .claim(CommonUtil.AUTH_NAME, authorities)
                .claim(CommonUtil.USER_LOG_ID_NAME, userLogDetail.getUserLogId())
                .setExpiration(new Date(System.currentTimeMillis() + validTime))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /** 쿠키 세팅하는 메서드 */
    public Cookie setTokenCookie(Cookie tokenCookie) {
        // http 전용 쿠키로 설정하는 옵션 - 자바스크립트에서 접근 불가 & 클라이언트 사이드 공격(xss 등) 방지
        tokenCookie.setHttpOnly(true);

        // https 연결에서만 쿠키가 전송되도록 하는 설정 - 배포 환경에서 풀어주기
        // refreshTokenCookie.setSecure(true); //https 요청만 허용

        // 쿠키의 유효 경로 설정 - '/'로만 정하면 도메인의 모든 하위 경로에서 해당 쿠키 전송
        tokenCookie.setPath("/");

        // 쿠키 유효기간 설정
        tokenCookie.setMaxAge(24 * 60 * 60);
        /*
            보통 일주일에서 1달을 잡는다고 한다.
            자동 로그인 쿠키가 있더라도 해당 RefreshToken 쿠키가 무효할 경우 다시 로그인 폼으로 이동!
            하지만 RefreshToken 쿠키가 남아있는 경우 해당 토큰으로 AccessToken을 다시 발급받아 자동 로그인!
        * */

        // SameSite 속성 추가하기 - 쿠키가 같은 사이트에서만 전송됨
        // 직접 사이트를 방문할 때만 쿠키가 전송되며, 외부 링크를 통해 접속 시 전송되지 않는다.
        /*
            SameSite=Strict : 쿠키가 오직 같은 사이트에서만 전송됨.
                                사용자가 직접 사이트를 방문할 때만 쿠키가 포함되어 전송되며, 외부 링크 접속 시 전송X
            SameSite=Lax : 기본적으로 외부 사이트에서의 요청에는 쿠키가 전송되지 않지만,
                            예외적으로 GET 방식의 안전한 요청에서는 전송 허용
            SameSite=None : 쿠키가 모든 상황에서 전송되지만, Secure 속성을 함께 설정해야 함.
                            주로 크로스 사이트 요청 시에도 쿠키가 필요할 때 사용함.
        * */
        tokenCookie.setAttribute("SameSite", "Strict");

        return tokenCookie;
    }
    
    /** 권한 추출 메서드 */
    public String getAuthorities(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
    }
}
