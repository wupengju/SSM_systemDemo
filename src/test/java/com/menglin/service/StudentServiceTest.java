package com.menglin.service;

import com.menglin.base.ServiceBaseTest;
import com.menglin.dao.StudentDaoTest;
import com.menglin.dto.UserDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import static org.junit.Assert.assertEquals;

/**
 * 配置spring和junit整合，junit启动时加载springIOC容器 spring-test,junit
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class StudentServiceTest extends ServiceBaseTest {
    private Logger logger = LoggerFactory.getLogger(StudentDaoTest.class);

    @Resource
    private StudentService studentService;

    @Test
    public void getStudentById() {
        Long studentId = 1L;
        UserDto userDto = studentService.getStudentById(studentId);
        assertEquals("校验 id", studentId, userDto.getId());
        logger.info("StudentService getStudentById， studentId:{}", studentId);
    }

    @Test
    public void getStudentByUsername() {
        String studentUsername = "test";
        UserDto userDto = studentService.getStudentByUsername(studentUsername);
        assertEquals("校验 username", "test", userDto.getUsername());
        logger.info("StudentService getStudentById， studentUsername:{}", studentUsername);
    }
}
