package com.menglin.base;

import com.menglin.entity.Student;
import com.menglin.entity.Task;
import com.menglin.redis.RedisUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.Resource;

// 加载 spring 配置文件
@ContextConfiguration({"classpath:spring/spring-dao.xml", "classpath:spring/spring-service.xml", "classpath:spring/spring-context.xml", "classpath:mybatis-config.xml"})
public class BaseTest {

    @Resource
    private RedisUtil redisUtil;

    @BeforeClass
    public static void startRunUnitTest() {
        System.out.println("开始执行单元测试.");
    }

    @AfterClass
    public static void endRunUnitTest() {
        System.out.println("单元测试执行完成.");
    }

    protected void redisPut(String key, Object obj) {
        redisUtil.put(key, obj);
    }

    protected Object redisGet(String key, Class clazz) {
        return redisUtil.get(key, clazz);
    }

    protected void redisDel(String key) {
        redisUtil.del(key);
    }

    /*
     * Mock new Entity ClassTeam Object
     * */
    /*
     * Student
     * */
    protected Student createStudent(Long id, String username, String password) {
        Student student = new Student();
        student.setId(id);
        student.setUsername(username);
        student.setPassword(password);
        return student;
    }

    /*
     * Task
     * */
    protected Task createTask(Long id, String name, String creator) {
        Task task = new Task();
        task.setId(id);
        task.setName(name);
        task.setCreator(creator);
        return task;
    }
}

