package com.menglin.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.menglin.dao.StudentCourseDao;
import com.menglin.entity.StudentCourse;
import com.menglin.enums.ErrorStateEnum;
import com.menglin.exception.ServiceException;
import com.menglin.service.StudentCourseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

import static com.menglin.common.AssertArguments.*;

@Service("studentCourseService")
public class StudentCourseServiceImpl implements StudentCourseService {
    private Logger logger = LoggerFactory.getLogger(StudentCourseServiceImpl.class);

    @Resource
    private StudentCourseDao studentCourseDao;

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public int addStudentCourse(StudentCourse studentCourse) {
        checkNotNull(studentCourse, "学生课程不能为空");
        checkNotEmpty(studentCourse.getCreator(), "学生课程创建者不能为空");
        checkGreaterThanZero(studentCourse.getStudentId(), "学生 id 不能小于或等于零");
        checkGreaterThanZero(studentCourse.getCourseId(), "课程 id 不能小于或等于零");

        validateIsSameNameStudentCourse(studentCourse.getStudentId(), studentCourse.getCourseId());
        studentCourse.setGmtCreate(new Date());
        int insertId;
        try {
            insertId = studentCourseDao.insertSelective(studentCourse);
        } catch (Exception e) {
            logger.info("insert studentCourse fail, studentCourse:{}, e:{}", JSONObject.toJSONString(studentCourse), e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "插入学生课程记录失败");
        }
        if (insertId > 0) {
            logger.info("insert studentCourse success, save studentCourse to redis, studentCourse:{}", JSONObject.toJSONString(studentCourse));
            return insertId;
        }

        return 0;
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public int updateStudentCourse(StudentCourse studentCourse) {
        checkNotNull(studentCourse, "学生课程不能为空");
        checkNotEmpty(studentCourse.getModifier(), "学生课程修改者不能为空");
        checkGreaterThanZero(studentCourse.getId(), "学生课程 id 不能小于或等于零");

        StudentCourse updateStudentCourse;
        int updateId;
        try {
            updateStudentCourse = studentCourseDao.selectByPrimaryKey(studentCourse.getId());
            updateStudentCourse(updateStudentCourse, studentCourse);
            updateId = studentCourseDao.updateByPrimaryKey(updateStudentCourse);
        } catch (Exception e) {
            logger.info("update studentCourse fail, studentCourse:{}, e:{}", JSONObject.toJSONString(studentCourse), e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "修改学生课程记录失败");
        }
        if (updateId > 0) {
            logger.info("update studentCourse success, studentCourse:{}", JSONObject.toJSONString(updateStudentCourse));
            return updateId;
        }

        return 0;
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void deleteStudentCourseById(Long id) {
        checkGreaterThanZero(id, "学生课程 id 不能小于或等于零");

        int deleteId;
        try {
            deleteId = studentCourseDao.deleteByPrimaryKey(id);
        } catch (Exception e) {
            logger.info("delete studentCourse fail, id:{}, e:{}", id, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "删除学生课程记录失败");
        }
        if (deleteId > 0) {
            logger.info("delete studentCourse success, id:{}", deleteId);
        } else {
            logger.info("delete studentCourse fail, id:{}", id);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "删除学生课程记录不存在");
        }
    }

    @Override
    public StudentCourse getStudentCourseById(Long id) {
        checkGreaterThanZero(id, "学生课程 id 不能小于或等于零");

        logger.info("get studentCourse by id:{}", id);
        StudentCourse studentCourseFromMysql;
        try {
            studentCourseFromMysql = studentCourseDao.selectByPrimaryKey(id);
        } catch (Exception e) {
            logger.info("get studentCourse by primary key fail, id:{}, e:{}", id, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据 id 查询学生课程记录失败");
        }
        if (studentCourseFromMysql != null) {
            logger.info("get studentCourse from mysql and save studentCourse to redis, studentCourse:{}", JSONObject.toJSONString(studentCourseFromMysql));
            return studentCourseFromMysql;
        }

        return null;
    }

    private void validateIsSameNameStudentCourse(Long studentId, Long courseId) {
        StudentCourse studentCourse;
        try {
            studentCourse = studentCourseDao.selectByStudentIdAndCourseId(studentId, courseId);
        } catch (Exception e) {
            logger.info("selectByMajorIdAndCourseId fail, studentId:{}, courseId:{}, e:{}", studentId, courseId, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据学生ID和课程ID查询学生课程记录失败");
        }
        if (studentCourse != null) {
            throw new ServiceException(ErrorStateEnum.DB_EXIST_SAME_RECORD.getState(), "添加的学生课程记录已存在");
        }
    }

    private void updateStudentCourse(StudentCourse updateStudentCourse, StudentCourse studentCourse) {
        if (studentCourse.getStudentId() > 0) {
            updateStudentCourse.setStudentId(studentCourse.getStudentId());
        }
        if (studentCourse.getCourseId() > 0) {
            updateStudentCourse.setCourseId(studentCourse.getCourseId());
        }
        updateStudentCourse.setModifier(studentCourse.getModifier());
        updateStudentCourse.setGmtModify(new Date());
    }
}
