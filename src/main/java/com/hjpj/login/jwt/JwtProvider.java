package com.hjpj.login.jwt;

import com.hjpj.login.dto.UserLogDetail;
import com.hjpj.login.util.CommonUtil;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
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
        if (claims.get(CommonUtil.AUTH_NAME) == null) {
            throw new RuntimeException("권한 정보가 존재하지 않습니다.");
        }

        // 권한 정보 가져오기(Collection 형태)
        Collection<? extends GrantedAuthority> authorities = Arrays.stream(claims.get(CommonUtil.AUTH_NAME).toString().split(","))
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

    /** 쿠키에서 accessToken 가져오기 */
    public String getTokenFromCookie(HttpServletRequest request) {
        if(request.getCookies() != null) {
            for(Cookie cookie : request.getCookies()) {
                if(cookie.getName().equals(CommonUtil.ACCESS_TOKEN)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /** 토큰 유효성 검사하기 */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT Token", e);
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT Token", e);
        }catch (IllegalArgumentException e) {
            log.info("JWT claims string is empty", e);
        }catch (SecurityException | MalformedJwtException e) {
            log.warn("Invalid JWT Token", e);
        }catch (Exception e) {
            log.warn("JWT claims string is empty", e);
        }
        return false;
    }
}
