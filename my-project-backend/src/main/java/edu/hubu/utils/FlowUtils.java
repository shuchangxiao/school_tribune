package edu.hubu.utils;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class FlowUtils {
    @Resource
    StringRedisTemplate stringredisTemplate;
    public boolean limitOneCheck(String key ,int blockTime){
        if(Boolean.TRUE.equals(stringredisTemplate.hasKey(key))){
            return false;
        } else {
            stringredisTemplate.opsForValue()
                    .set(key,"",blockTime, TimeUnit.SECONDS);
            return true;
        }
    }
}
