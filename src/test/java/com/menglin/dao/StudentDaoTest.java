package com.menglin.dao;

import com.menglin.base.DaoBaseTest;
import com.menglin.entity.Student;
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
public class StudentDaoTest extends DaoBaseTest {
    private Logger logger = LoggerFactory.getLogger(StudentDaoTest.class);

    @Resource
    private StudentDao studentDao;

    @Test
    public void testQueryById() {
        long studentId = 1L;
        Student student = studentDao.selectByPrimaryKey(studentId);
        System.out.println(student.getUsername());
        assertEquals("校验 username", "test", student.getUsername());
        logger.info("StudentDaoTest testQueryById， studentId:{} ", studentId);
    }
}
