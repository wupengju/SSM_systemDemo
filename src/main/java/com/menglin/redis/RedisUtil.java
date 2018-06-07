package com.menglin.redis;

import com.alibaba.fastjson.JSONObject;
import com.menglin.enums.ErrorStateEnum;
import com.menglin.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;


@Component
public class RedisUtil {
    private Logger logger = LoggerFactory.getLogger(RedisUtil.class);
    private static final String CACHE_NAME = "VNDais-cache:";
    private static final int EXPIRE_TIME = 3000;

    private RedisTemplate template;

    private RedisCache cache;

    public RedisUtil() {
        init();
    }

    private void init() {

        // RedisCacheConfig中定义了
        template = SpringUtil.getBean("redisTemplate");
        cache = new RedisCache(CACHE_NAME, CACHE_NAME.getBytes(), template, EXPIRE_TIME);
    }

    public void put(String key, Object obj) {
        try {
            cache.put(key, obj);
        } catch (Exception e) {
            logger.info("redis put fail, key:{}, obj:{}, e:{}", key, JSONObject.toJSONString(obj), e);
            throw new ServiceException(ErrorStateEnum.REDIS_ERROR.getState(), "redis 插入记录失败");
        }
    }

    public Object get(String key, Class clazz) {
        Object result;
        try {
            result = cache.get(key) == null ? null : cache.get(key, clazz);
        } catch (Exception e) {
            logger.info("redis get fail, key:{}, clazz:{}, e:{}", key, clazz, e);
            throw new ServiceException(ErrorStateEnum.REDIS_ERROR.getState(), "redis 获取记录失败");
        }
        return result;
    }

    public void del(String key) {
        try {
            cache.evict(key);
        } catch (Exception e) {
            logger.info("redis del fail, key:{}, e:{}", key, e);
            throw new ServiceException(ErrorStateEnum.REDIS_ERROR.getState(), "redis 删除记录失败");
        }
    }
}
