package com.hjpj.login.jwt;

import com.hjpj.login.dto.UserLogDetail;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;

@Component
@Slf4j
public class JwtProvider {

    private final Key key;

    public JwtProvider(@Value("${jwt.secret}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /** Jwt 토큰 복호화를 통해 정보 꺼내기*/
    public Authentication getAuthentication(String token) {
        // Jwt 토큰 복호화
        Claims claims = parseClaims(token);

        // 권한 존재 여부 판단
        if (claims.get("auth") == null) {
            throw new RuntimeException("권한 정보가 존재하지 않습니다.");
        }

        // 권한 정보 가져오기(Collection 형태)
        Collection<? extends GrantedAuthority> authorities = Arrays.stream(claims.get("auth").toString().split(","))
                .map(SimpleGrantedAuthority::new)
                .toList();

        // UserLogDetail 만들기
        UserLogDetail userLogDetail = new UserLogDetail(claims.getSubject());

        // authorities를 userLogDetail 객체에 명확하게 넣어주기
//        userLogDetail.getRoles().addAll(authorities.stream()
//                .map(GrantedAuthority::getAuthority)
//                .toList());

        return new UsernamePasswordAuthenticationToken(userLogDetail, "", authorities);
    }

    /** 트콘 복호화 작업 */
    public Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            // 토큰이 만료된 경우, 만료된 클레임을 반환
            log.warn("Expired JWT Token", e);
            return e.getClaims();
        } catch (MalformedJwtException | SecurityException | UnsupportedJwtException e) {
            // 잘못된 형식의 JWT 토큰에 대해 경고 로그를 남기고 null 반환
            log.warn("Invalid JWT Token", e);
            return null;
        } catch (IllegalArgumentException e) {
            // 비어있는 토큰인 경우 경고 로그
            log.warn("JWT claims string is empty", e);
            return null;
        }
    }

    /** 토큰 정보 검증하는 메서드 */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            // 토큰 자체가 유효하지 않거나 형식이 잘못된 경우
            log.info("Invalid JWT Token", e);
        } catch (ExpiredJwtException e) {
            // 토큰이 만료된 경우
            log.info("Expired JWT Token", e);
        } catch (UnsupportedJwtException e) {
            // 지원되지 않는 JWT 인 경우
            log.info("Unsupported JWT Token", e);
        } catch (IllegalArgumentException e) {
            // 토큰에 포함된 클레임이 비었을 경우(정보가 없는 상황)
            log.info("JWT claims string is empty.", e);
        }
        return false;
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
}
