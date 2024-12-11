package com.hjpj.login;


import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

public class RedisLettuceTest {

    public static void main(String[] args) {
        // Redis 연결
        RedisClient redisClient = RedisClient.create("redis://127.0.0.1:6379");
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisCommands<String, String> commands = connection.sync();

        // 데이터 쓰기
        commands.set("token", "example-value");

        // 데이터 읽기
        String value = commands.get("token");
        System.out.println("Redis에서 읽은 값: " + value);

        // 연결 닫기
        connection.close();
        redisClient.shutdown();
    }
}
