package com.hjpj.login.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableRedisRepositories
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

//    @Value("${spring.data.redis.password}")
//    private String password;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(host);
        redisStandaloneConfiguration.setPort(port);
//        redisStandaloneConfiguration.setPassword(password);
        return new LettuceConnectionFactory(redisStandaloneConfiguration);
    }

    /**
        Spring boot 2.0 이후부터는 Redis Template와 String Template가 자동생성되므로 따로 빈 등록을 하지 않아도 된다.
        Redis Template에는 serializer를 설정해주는 데... 설정하지 않는 경우 직접 redis-cli로 데이터 확인이 어렵다.
        설정하지 않으면 바이너리 형식 그대로 입력되므로 알아보기가 힘들다고 한다!
     * */

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());

        // 일반적인 key:value의 경우 시리얼라이저
        /**
         [[[ setKeySerializer / setKeySerializer ]]]
            - Redis에서 저장되는 키와 값을 String 형태로 직렬화 함
            - 기본적으로 Redis Template은 JdkSerializationRedisSerializer를 사용하여 데이터를 직렬화 하는데...
                해당 방식은 이진 데이터 형태로 저장하는 것
            - 이 설정을 통해 데이터를 가독성 좋은 문자열로 저장하도록 변경함
         
         [[[ StringRedisSerializer ]]]
            - 데이터를 UTF-8 문자열로 직렬화하고 역직렬화 함
            - Redis에 저장된 데이터가 사람이 읽을 수 있는 형태가 됨
         * */
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());


        // Hash를 사용할 경우 시리얼라이저
        /**
         [[[ setHashKeySerializer / setHashValueSerializer ]]]
            - Redis의 Hash 데이터 구조에서 키와 값을 처리할 때 적용할 시리얼라이저를 설정함
            - Hash는 주로 @RedisHash를 사용하거나 redisTemplate.opsForHash()로 저장할 때 쓰임
         * */
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());


        // 모든 경우
        /**
         [[[ setDefaultSerializer ]]]
            - 키, 값, 해시 키, 해시 값 등 모든 데이터의 직렬화 방식을 한 번에 설정함
            - 만약 모든 경우에 동일한 직렬화 방식을 원한다면 setDefaultSerializer만 설정해도 됨
         * */
        redisTemplate.setDefaultSerializer(new StringRedisSerializer());
        
        return redisTemplate;
    }
}
