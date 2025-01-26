package com.sammo.journalApp.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.lang.model.type.ReferenceType;
import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public <T> T get(String key, TypeReference<T> typeReference){

        try{
            Object o = redisTemplate.opsForValue().get(key);
            if( o != null )
                return objectMapper.readValue(o.toString(), typeReference);
            else
                return null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve value from Redis", e);
        }
    }

    public void set(String key, Object o, Long ttl){

        try{
            String jsonValue = objectMapper.writeValueAsString(o);
            redisTemplate.opsForValue().set(key, jsonValue, ttl, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
