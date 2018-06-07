package com.menglin.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.menglin.dao.MajorCourseDao;
import com.menglin.entity.MajorCourse;
import com.menglin.enums.ErrorStateEnum;
import com.menglin.exception.ServiceException;
import com.menglin.service.MajorCourseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

import static com.menglin.common.AssertArguments.*;

@Service("majorCourseService")
public class MajorCourseServiceImpl implements MajorCourseService {
    private Logger logger = LoggerFactory.getLogger(MajorCourseServiceImpl.class);

    @Resource
    private MajorCourseDao majorCourseDao;

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public int addMajorCourse(MajorCourse majorCourse) {
        checkNotNull(majorCourse, "专业课程不能为空");
        checkNotEmpty(majorCourse.getCreator(), "专业课程创建者不能为空");
        checkGreaterThanZero(majorCourse.getMajorId(), "专业 id 不能小于或等于零");
        checkGreaterThanZero(majorCourse.getCourseId(), "课程 id 不能小于或等于零");

        validateIsSameNameMajorCourse(majorCourse.getMajorId(), majorCourse.getCourseId());
        MajorCourse newMajorCourse = new MajorCourse();
        newMajorCourse.setMajorId(majorCourse.getMajorId());
        newMajorCourse.setCourseId(majorCourse.getCourseId());
        newMajorCourse.setCreator(majorCourse.getCreator());
        newMajorCourse.setGmtCreate(new Date());
        int insertId;
        try {
            insertId = majorCourseDao.insertSelective(newMajorCourse);
        } catch (Exception e) {
            logger.info("insert majorCourse fail, majorCourse:{}, e:{}", JSONObject.toJSONString(newMajorCourse), e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "插入专业课程记录失败");
        }
        if (insertId > 0) {
            logger.info("insert majorCourse success, save majorCourse to redis, majorCourse:{}", JSONObject.toJSONString(newMajorCourse));
            return insertId;
        }

        return 0;
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public int updateMajorCourse(MajorCourse majorCourse) {
        checkNotNull(majorCourse, "专业课程不能为空");
        checkNotEmpty(majorCourse.getModifier(), "专业课程修改者不能为空");
        checkGreaterThanZero(majorCourse.getId(), "专业课程 id 不能小于或等于零");

        MajorCourse updateMajorCourse;
        int updateId;
        try {
            updateMajorCourse = majorCourseDao.selectByPrimaryKey(majorCourse.getId());
            updateMajorCourse(updateMajorCourse, majorCourse);
            updateId = majorCourseDao.updateByPrimaryKey(updateMajorCourse);
        } catch (Exception e) {
            logger.info("update majorCourse fail, majorCourse:{}, e:{}", JSONObject.toJSONString(majorCourse), e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "修改专业课程记录失败");
        }
        if (updateId > 0) {
            logger.info("update majorCourse success, majorCourse:{}", JSONObject.toJSONString(updateMajorCourse));
            return updateId;
        }

        return 0;
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void deleteMajorCourseById(Long id) {
        checkGreaterThanZero(id, "专业课程 id 不能小于或等于零");

        int deleteId;
        try {
            deleteId = majorCourseDao.deleteByPrimaryKey(id);
        } catch (Exception e) {
            logger.info("delete majorCourse fail, id:{}, e:{}", id, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "删除专业课程记录失败");
        }
        if (deleteId > 0) {
            logger.info("delete majorCourse success, id:{}", deleteId);
        } else {
            logger.info("delete majorCourse fail, id:{}", id);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "删除专业课程记录不存在");
        }
    }

    @Override
    public MajorCourse getMajorCourseById(Long id) {
        checkGreaterThanZero(id, "专业课程 id 不能小于或等于零");

        logger.info("get majorCourse by id:{}", id);
        MajorCourse majorCourseFromMysql;
        try {
            majorCourseFromMysql = majorCourseDao.selectByPrimaryKey(id);
        } catch (Exception e) {
            logger.info("get majorCourse by primary key fail, id:{}, e:{}", id, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据 id 查询专业课程记录失败");
        }
        if (majorCourseFromMysql != null) {
            logger.info("get majorCourse from mysql and save majorCourse to redis, majorCourse:{}", JSONObject.toJSONString(majorCourseFromMysql));
            return majorCourseFromMysql;
        }

        return null;
    }

    private void validateIsSameNameMajorCourse(Long majorId, Long courseId) {
        MajorCourse majorCourse;
        try {
            majorCourse = majorCourseDao.selectByMajorIdAndCourseId(majorId, courseId);
        } catch (Exception e) {
            logger.info("selectByMajorIdAndCourseId fail, majorId:{}, courseId:{}, e:{}", majorId, courseId, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据专业ID和课程ID查询专业课程记录失败");
        }
        if (majorCourse != null) {
            throw new ServiceException(ErrorStateEnum.DB_EXIST_SAME_RECORD.getState(), "添加的专业课程记录已存在");
        }
    }

    private void updateMajorCourse(MajorCourse updateMajorCourse, MajorCourse majorCourse) {
        if (majorCourse.getMajorId() > 0) {
            updateMajorCourse.setMajorId(majorCourse.getMajorId());
        }
        if (majorCourse.getCourseId() > 0) {
            updateMajorCourse.setMajorId(majorCourse.getCourseId());
        }
        updateMajorCourse.setModifier(majorCourse.getModifier());
        updateMajorCourse.setGmtModify(new Date());
    }
}
