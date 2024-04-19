package edu.hubu.utils;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class FlowUtils {
    @Resource
    RedisTemplate redisTemplate;
    public boolean limitOneCheck(String key ,int blockTime){
        if(Boolean.TRUE.equals(redisTemplate.hasKey(key))){
            return false;
        } else {
            redisTemplate.opsForValue()
                    .set(key,"",blockTime, TimeUnit.SECONDS);
            return true;
        }
    }
}
