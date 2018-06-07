package com.menglin.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.menglin.dao.TeacherCourseDao;
import com.menglin.entity.TeacherCourse;
import com.menglin.enums.ErrorStateEnum;
import com.menglin.exception.ServiceException;
import com.menglin.service.TeacherCourseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

import static com.menglin.common.AssertArguments.*;

@Service("teacherCourseService")
public class TeacherCourseServiceImpl implements TeacherCourseService {
    private Logger logger = LoggerFactory.getLogger(TeacherCourseServiceImpl.class);

    @Resource
    private TeacherCourseDao teacherCourseDao;

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public int addTeacherCourse(TeacherCourse teacherCourse) {
        checkNotNull(teacherCourse, "教师课程不能为空");
        checkNotEmpty(teacherCourse.getCreator(), "教师课程创建者不能为空");
        checkGreaterThanZero(teacherCourse.getTeacherId(), "教师 id 不能小于或等于零");
        checkGreaterThanZero(teacherCourse.getCourseId(), "课程 id 不能小于或等于零");

        validateIsSameNameTeacherCourse(teacherCourse.getTeacherId(), teacherCourse.getCourseId());
        TeacherCourse newTeacherCourse = new TeacherCourse();
        newTeacherCourse.setTeacherId(teacherCourse.getTeacherId());
        newTeacherCourse.setCourseId(teacherCourse.getCourseId());
        newTeacherCourse.setCreator(teacherCourse.getCreator());
        newTeacherCourse.setGmtCreate(new Date());
        int insertId;
        try {
            insertId = teacherCourseDao.insertSelective(newTeacherCourse);
        } catch (Exception e) {
            logger.info("insert teacherCourse fail, teacherCourse:{}, e:{}", JSONObject.toJSONString(newTeacherCourse), e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "插入教师课程记录失败");
        }
        if (insertId > 0) {
            logger.info("insert teacherCourse success, save teacherCourse to redis, teacherCourse:{}", JSONObject.toJSONString(newTeacherCourse));
            return insertId;
        }

        return 0;
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public int updateTeacherCourse(TeacherCourse teacherCourse) {
        checkNotNull(teacherCourse, "教师课程不能为空");
        checkNotEmpty(teacherCourse.getModifier(), "教师课程修改者不能为空");
        checkGreaterThanZero(teacherCourse.getId(), "教师课程 id 不能小于或等于零");

        TeacherCourse updateTeacherCourse;
        int updateId;
        try {
            updateTeacherCourse = teacherCourseDao.selectByPrimaryKey(teacherCourse.getId());
            updateTeacherCourse(updateTeacherCourse, teacherCourse);
            updateId = teacherCourseDao.updateByPrimaryKey(updateTeacherCourse);
        } catch (Exception e) {
            logger.info("update teacherCourse fail, teacherCourse:{}, e:{}", JSONObject.toJSONString(teacherCourse), e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "修改教师课程记录失败");
        }
        if (updateId > 0) {
            logger.info("update teacherCourse success, teacherCourse:{}", JSONObject.toJSONString(updateTeacherCourse));
            return updateId;
        }

        return 0;
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void deleteTeacherCourseById(Long id) {
        checkGreaterThanZero(id, "教师课程 id 不能小于或等于零");

        int deleteId;
        try {
            deleteId = teacherCourseDao.deleteByPrimaryKey(id);
        } catch (Exception e) {
            logger.info("delete teacherCourse fail, id:{}, e:{}", id, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "删除教师课程记录失败");
        }
        if (deleteId > 0) {
            logger.info("delete teacherCourse success, id:{}", deleteId);
        } else {
            logger.info("delete teacherCourse fail, id:{}", id);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "删除教师课程记录不存在");
        }
    }

    @Override
    public TeacherCourse getTeacherCourseById(Long id) {
        checkGreaterThanZero(id, "教师课程 id 不能小于或等于零");

        logger.info("get teacherCourse by id:{}", id);
        TeacherCourse teacherCourseFromMysql;
        try {
            teacherCourseFromMysql = teacherCourseDao.selectByPrimaryKey(id);
        } catch (Exception e) {
            logger.info("get teacherCourse by primary key fail, id:{}, e:{}", id, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据 id 查询教师课程记录失败");
        }
        if (teacherCourseFromMysql != null) {
            logger.info("get teacherCourse from mysql and save teacherCourse to redis, teacherCourse:{}", JSONObject.toJSONString(teacherCourseFromMysql));
            return teacherCourseFromMysql;
        }

        return null;
    }

    private void validateIsSameNameTeacherCourse(Long teacherId, Long courseId) {
        TeacherCourse teacherCourse;
        try {
            teacherCourse = teacherCourseDao.selectByTeacherIdIdAndCourseId(teacherId, courseId);
        } catch (Exception e) {
            logger.info("selectByMajorIdAndCourseId fail, teacherId:{}, courseId:{}, e:{}", teacherId, courseId, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据教师ID和课程ID查询教师课程记录失败");
        }
        if (teacherCourse != null) {
            throw new ServiceException(ErrorStateEnum.DB_EXIST_SAME_RECORD.getState(), "添加的教师课程记录已存在");
        }
    }

    private void updateTeacherCourse(TeacherCourse updateTeacherCourse, TeacherCourse teacherCourse) {
        if (teacherCourse.getTeacherId() > 0) {
            updateTeacherCourse.setTeacherId(teacherCourse.getTeacherId());
        }
        if (teacherCourse.getCourseId() > 0) {
            updateTeacherCourse.setCourseId(teacherCourse.getCourseId());
        }
        updateTeacherCourse.setModifier(teacherCourse.getModifier());
        updateTeacherCourse.setGmtModify(new Date());
    }
}
