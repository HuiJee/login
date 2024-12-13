package com.hjpj.login.repository;

import com.hjpj.login.entity.TokenRedis;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RedisRepository extends CrudRepository<TokenRedis, String> {

    void deleteByUserLogId(String userLogId);

}
