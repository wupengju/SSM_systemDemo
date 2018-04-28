package com.menglin.service.impl;

import com.menglin.dao.StudentDao;
import com.menglin.entity.Student;
import com.menglin.service.StudentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static com.menglin.common.AssertArguments.*;

@Service("studentService")
public class StudentServiceImpl implements StudentService {
    private Logger logger = LoggerFactory.getLogger(StudentServiceImpl.class);

    @Resource
    private StudentDao studentDao;

    @Override
    public Student getStudentById(Long id) {
        checkGreaterThanZero(id, "用户 id 不能小于或等于零");
        return studentDao.selectByPrimaryKey(id);
    }

    @Override
    public Student getStudentByUsername(String username) {
        checkNotEmpty(username, "用户名不能为空");
        return studentDao.selectByUsername(username);
    }

    @Override
    public Student getStudentByUsernameAndPassword(String username, String password) {
        checkNotEmpty(username, "用户名不能为空");
        checkNotEmpty(password, "密码不能为空");
        Student student = studentDao.selectByUsernameAndPassword(username, password);
        checkNotNull(student, "用户名或密码错误");
        return student;
    }

}
