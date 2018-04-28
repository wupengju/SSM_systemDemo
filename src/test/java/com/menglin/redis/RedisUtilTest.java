package com.menglin.redis;

import com.menglin.base.BaseTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(SpringJUnit4ClassRunner.class)
public class RedisUtilTest extends BaseTest {
    private Logger logger = LoggerFactory.getLogger(RedisUtilTest.class);

    @Test
    public void redisStringOperation() {
        del();
        put();
        get();
        del();
    }

    @Test
    public void redisListOperation() {
        delList();
        putList();
        getList();
        delList();
    }

    public void put() {
        String key = "name";
        String value = "VNDais";
        redisPut(key, value);
        assertEquals("校验 redis put:", "VNDais", redisGet("name", String.class));
        logger.info("redis put, key:{}, value:{}", key, value);
    }

    public void get() {
        String key = "name";
        String value = (String) redisGet(key, String.class);
        assertEquals("校验 redis get:", "VNDais", value);
        logger.info("redis get, key:{}, value:{}", key, value);
    }

    public void del() {
        String key = "name";
        redisDel(key);
        assertNull("校验 redis del:", redisGet("name", String.class));
        logger.info("redis del, key:{}", key);
    }

    public void putList() {
        String key = "stringList";
        List<String> stringList = new ArrayList<>();
        stringList.add("1");
        stringList.add("2");
        stringList.add("3");
        redisPut(key, stringList);
        List<String> stringListValue = (List<String>) redisGet(key, List.class);
        assertEquals("校验 redis putList:", stringList.size(), stringListValue.size());
        logger.info("redis putList, key:{}, stringList:{}", key, stringList.toString());
    }

    public void getList() {
        String key = "stringList";
        List<String> stringListValue = (List<String>) redisGet(key, List.class);
        assertEquals("校验 redis putList:", 3, stringListValue.size());
        logger.info("redis getList, key:{}, stringList:{}", key, stringListValue.toString());
    }

    public void delList() {
        String key = "stringList";
        redisDel(key);
        assertNull("校验 redis delList:", redisGet(key, List.class));
        logger.info("redis delList, key:{}", key);
    }

}