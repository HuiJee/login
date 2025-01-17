package com.hjpj.login.redis;

import com.hjpj.login.auth.jwt.JwtUtil;
import org.springframework.data.annotation.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.redis.core.RedisHash;

@Getter
@AllArgsConstructor
@RedisHash(value = "token", timeToLive = JwtUtil.REFRESH_TOKEN_VALIDITY/1000)
public class TokenRedis {
    
    @Id
    private String userLogId;
    private String refreshToken;

    @Override
    public String toString() {
        return "TokenRedis [userLogId=" + userLogId + ", refreshToken=" + refreshToken + "]";
    }

        /*
        Refresh Token은 Redis에 저장하기 때문에 JPA 의존성이 필요하지 않다.
        따라서 @Id import는 java.persistence.id가 아니라 opg.springframework.data.annotaion.id를 사용해야 한다.

        @RedisHash는 Redis Lettuce를 사용하기 위해 작성해야 한다.
        value는 redis key 값으로  사용된다.
        => redis 저장소에 {value}:{@Id 값}이 저장된다.

        timeToLive는 유효시간을 설정한 값으로 초단위를 의미한다.
    * */
}
