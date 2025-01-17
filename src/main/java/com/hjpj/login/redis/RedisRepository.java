package com.hjpj.login.redis;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RedisRepository extends CrudRepository<TokenRedis, String> {

    Optional<TokenRedis> findById(String userLogId);

}
