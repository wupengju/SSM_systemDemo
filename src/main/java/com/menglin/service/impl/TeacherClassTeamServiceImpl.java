package com.menglin.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.menglin.dao.TeacherClassTeamDao;
import com.menglin.entity.TeacherClassTeam;
import com.menglin.enums.ErrorStateEnum;
import com.menglin.exception.ServiceException;
import com.menglin.service.TeacherClassTeamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

import static com.menglin.common.AssertArguments.*;

@Service("teacherClassTeamService")
public class TeacherClassTeamServiceImpl implements TeacherClassTeamService {
    private Logger logger = LoggerFactory.getLogger(TeacherClassTeamServiceImpl.class);

    @Resource
    private TeacherClassTeamDao teacherClassTeamDao;

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public int addTeacherClassTeam(TeacherClassTeam teacherClassTeam) {
        checkNotNull(teacherClassTeam, "教师班级不能为空");
        checkNotEmpty(teacherClassTeam.getCreator(), "教师班级创建者不能为空");
        checkGreaterThanZero(teacherClassTeam.getTeacherId(), "教师 id 不能小于或等于零");
        checkGreaterThanZero(teacherClassTeam.getClassTeamId(), "班级 id 不能小于或等于零");

        validateIsSameNameTeacherClassTeam(teacherClassTeam.getTeacherId(), teacherClassTeam.getClassTeamId());
        teacherClassTeam.setGmtCreate(new Date());
        int insertId;
        try {
            insertId = teacherClassTeamDao.insertSelective(teacherClassTeam);
        } catch (Exception e) {
            logger.info("insert teacherClassTeam fail, teacherClassTeam:{}, e:{}", JSONObject.toJSONString(teacherClassTeam), e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "插入教师班级记录失败");
        }
        if (insertId > 0) {
            logger.info("insert teacherClassTeam success, save teacherClassTeam to redis, teacherClassTeam:{}", JSONObject.toJSONString(teacherClassTeam));
            return insertId;
        }

        return 0;
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public int updateTeacherClassTeam(TeacherClassTeam teacherClassTeam) {
        checkNotNull(teacherClassTeam, "教师班级不能为空");
        checkNotEmpty(teacherClassTeam.getModifier(), "教师班级修改者不能为空");
        checkGreaterThanZero(teacherClassTeam.getId(), "教师班级 id 不能小于或等于零");

        TeacherClassTeam updateTeacherClassTeam;
        int updateId;
        try {
            updateTeacherClassTeam = teacherClassTeamDao.selectByPrimaryKey(teacherClassTeam.getId());
            updateTeacherClassTeam(updateTeacherClassTeam, teacherClassTeam);
            updateId = teacherClassTeamDao.updateByPrimaryKey(updateTeacherClassTeam);
        } catch (Exception e) {
            logger.info("update teacherClassTeam fail, teacherClassTeam:{}, e:{}", JSONObject.toJSONString(teacherClassTeam), e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "修改教师班级记录失败");
        }
        if (updateId > 0) {
            logger.info("update teacherClassTeam success, teacherClassTeam:{}", JSONObject.toJSONString(updateTeacherClassTeam));
            return updateId;
        }

        return 0;
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void deleteTeacherClassTeamById(Long id) {
        checkGreaterThanZero(id, "教师班级 id 不能小于或等于零");

        int deleteId;
        try {
            deleteId = teacherClassTeamDao.deleteByPrimaryKey(id);
        } catch (Exception e) {
            logger.info("delete teacherClassTeam fail, id:{}, e:{}", id, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "删除教师班级记录失败");
        }
        if (deleteId > 0) {
            logger.info("delete teacherClassTeam success, id:{}", deleteId);
        } else {
            logger.info("delete teacherClassTeam fail, id:{}", id);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "删除教师班级记录不存在");
        }
    }

    @Override
    public TeacherClassTeam getTeacherClassTeamById(Long id) {
        checkGreaterThanZero(id, "教师班级 id 不能小于或等于零");

        logger.info("get teacherClassTeam by id:{}", id);
        TeacherClassTeam teacherClassTeamFromMysql;
        try {
            teacherClassTeamFromMysql = teacherClassTeamDao.selectByPrimaryKey(id);
        } catch (Exception e) {
            logger.info("get teacherClassTeam by primary key fail, id:{}, e:{}", id, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据 id 查询教师班级记录失败");
        }
        if (teacherClassTeamFromMysql != null) {
            logger.info("get teacherClassTeam from mysql and save teacherClassTeam to redis, teacherClassTeam:{}", JSONObject.toJSONString(teacherClassTeamFromMysql));
            return teacherClassTeamFromMysql;
        }

        return null;
    }

    private void validateIsSameNameTeacherClassTeam(Long teacherId, Long classTeamId) {
        TeacherClassTeam teacherClassTeam;
        try {
            teacherClassTeam = teacherClassTeamDao.selectByTeacherIdAndClassTeamId(teacherId, classTeamId);
        } catch (Exception e) {
            logger.info("selectByMajorIdAndCourseId fail, teacherId:{}, classTeamId:{}, e:{}", teacherId, classTeamId, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据教师ID和班级ID查询教师班级记录失败");
        }
        if (teacherClassTeam != null) {
            throw new ServiceException(ErrorStateEnum.DB_EXIST_SAME_RECORD.getState(), "添加的教师班级记录已存在");
        }
    }

    private void updateTeacherClassTeam(TeacherClassTeam updateTeacherClassTeam, TeacherClassTeam teacherClassTeam) {
        if (teacherClassTeam.getTeacherId() > 0) {
            updateTeacherClassTeam.setTeacherId(teacherClassTeam.getTeacherId());
        }
        if (teacherClassTeam.getClassTeamId() > 0) {
            updateTeacherClassTeam.setClassTeamId(teacherClassTeam.getClassTeamId());
        }
        updateTeacherClassTeam.setModifier(teacherClassTeam.getModifier());
        updateTeacherClassTeam.setGmtModify(new Date());
    }
}
