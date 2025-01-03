package com.hjpj.login.repository;

import com.hjpj.login.entity.TokenRedis;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RedisRepository extends CrudRepository<TokenRedis, String> {

    Optional<TokenRedis> findById(String userLogId);

}
