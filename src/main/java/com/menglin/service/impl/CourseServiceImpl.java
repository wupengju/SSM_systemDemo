package com.menglin.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.menglin.dao.CourseDao;
import com.menglin.dto.CourseDto;
import com.menglin.dto.IdAndNameDto;
import com.menglin.dto.SearchConditionsDto;
import com.menglin.entity.Course;
import com.menglin.enums.ErrorStateEnum;
import com.menglin.exception.ServiceException;
import com.menglin.redis.RedisKeys;
import com.menglin.redis.RedisUtil;
import com.menglin.service.CourseService;
import com.menglin.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;

import static com.menglin.common.AssertArguments.*;

@Service("courseService")
public class CourseServiceImpl implements CourseService {
    private Logger logger = LoggerFactory.getLogger(CourseServiceImpl.class);

    @Resource
    private RedisUtil redisUtil;
    @Resource
    private CourseDao courseDao;
    @Resource
    private CourseDto courseDto;
    @Resource
    private IdAndNameDto idAndNameDto;

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public int addCourse(CourseDto courseDto) {
        checkNotNull(courseDto, "课程不能为空");
        checkNotEmpty(courseDto.getName(), "课程名字不能为空");
        checkNotEmpty(courseDto.getCreator(), "课程创建者不能为空");
        checkNotEmpty(courseDto.getDescription(), "课程描述不能为空");

        validateIsSameNameCourse(courseDto.getName());
        Course newCourse = courseDto.convertToCourse();
        newCourse.setGmtCreate(new Date());
        int insertId;
        try {
            insertId = courseDao.insertSelective(newCourse);
        } catch (Exception e) {
            logger.info("insert course fail, course:{}, e:{}", JSONObject.toJSONString(newCourse), e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "插入课程记录失败");
        }
        if (insertId > 0) {
            logger.info("insert course success, save course to redis, course:{}", JSONObject.toJSONString(newCourse));
            redisUtil.put(RedisKeys.COURSE_CACHE_KEY + newCourse.getId(), newCourse);
            return insertId;
        }

        return 0;
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public int updateCourse(CourseDto courseDto) {
        checkNotNull(courseDto, "课程不能为空");
        checkNotEmpty(courseDto.getModifier(), "课程修改者不能为空");
        checkGreaterThanZero(courseDto.getId(), "课程 id 不能小于或等于零");

        Course updateCourse;
        int updateId;
        try {
            updateCourse = courseDao.selectByPrimaryKey(courseDto.getId());
            updateCourse(updateCourse, courseDto);
            updateId = courseDao.updateByPrimaryKey(updateCourse);
        } catch (Exception e) {
            logger.info("update course fail, course:{}, e:{}", JSONObject.toJSONString(courseDto.convertToCourse()), e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "修改课程记录失败");
        }
        if (updateId > 0) {
            logger.info("update course success, save course to redis, course:{}", JSONObject.toJSONString(updateCourse));
            redisUtil.del(RedisKeys.COURSE_CACHE_KEY + updateCourse.getId());
            redisUtil.put(RedisKeys.COURSE_CACHE_KEY + updateCourse.getId(), updateCourse);
            return updateId;
        }

        return 0;
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void deleteCourseById(Long id) {
        checkGreaterThanZero(id, "课程 id 不能小于或等于零");

        int deleteId;
        try {
            deleteId = courseDao.deleteByPrimaryKey(id);
        } catch (Exception e) {
            logger.info("delete course fail, id:{}, e:{}", id, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "删除课程记录失败");
        }
        if (deleteId > 0) {
            logger.info("delete course success, id:{}", deleteId);
            redisUtil.del(RedisKeys.COURSE_CACHE_KEY + deleteId);
        } else {
            logger.info("delete course fail, id:{}", id);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "删除课程记录不存在");
        }
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void batchDeleteCoursesByIds(String ids) {
        checkNotEmpty(ids, "ids 不能为空");

        String[] idsStr = ids.split(",");
        for (String anIdsStr : idsStr) {
            deleteCourseById(Long.parseLong(anIdsStr));
        }
    }

    @Override
    public CourseDto getCourseById(Long id) {
        checkGreaterThanZero(id, "课程 id 不能小于或等于零");

        logger.info("get course by id:{}", id);
        Course course = (Course) redisUtil.get(RedisKeys.COURSE_CACHE_KEY + id, Course.class);
        if (course != null) {
            logger.info("course in redis, course:{}", JSONObject.toJSONString(course));
            return setCourseDtoGmtCreateAndGmtModify(course);
        }
        Course courseFromMysql;
        try {
            courseFromMysql = courseDao.selectByPrimaryKey(id);
        } catch (Exception e) {
            logger.info("get course by primary key fail, id:{}, e:{}", id, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据 id 查询课程记录失败");
        }
        if (courseFromMysql != null) {
            logger.info("get course from mysql and save course to redis, course:{}", JSONObject.toJSONString(courseFromMysql));
            redisUtil.put(RedisKeys.COURSE_CACHE_KEY + courseFromMysql.getId(), courseFromMysql);
            return setCourseDtoGmtCreateAndGmtModify(courseFromMysql);
        }

        return null;
    }

    @Override
    public CourseDto getCourseByName(String name) {
        checkNotEmpty(name, "课程名不能为空");

        logger.info("get course by name:{}", name);
        Course course = (Course) redisUtil.get(RedisKeys.COURSE_CACHE_KEY + name, Course.class);
        if (course != null) {
            logger.info("course in redis, course:{}", course);
            return setCourseDtoGmtCreateAndGmtModify(course);
        }
        Course courseFromMysql;
        try {
            courseFromMysql = courseDao.selectByName(name);
        } catch (Exception e) {
            logger.info("get course by primary key fail, name:{}, e:{}", name, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据课程名查询课程记录失败");
        }
        if (courseFromMysql != null) {
            logger.info("get course from mysql and save course to redis, course:{}", courseFromMysql);
            redisUtil.put(RedisKeys.COURSE_CACHE_KEY + courseFromMysql.getName(), courseFromMysql);
            return setCourseDtoGmtCreateAndGmtModify(courseFromMysql);
        }

        return null;
    }

    @Override
    public List<IdAndNameDto> getCourseIdAndNameListByTeacherId(Long teacherId) {
        checkGreaterThanZero(teacherId, "教师 id 不能小于或等于零");

        List<Course> courseList = courseDao.getCoursesListByTeacherId(teacherId);
        List<IdAndNameDto> idAndNameDtoList = new ArrayList<>();
        for (Course course : courseList) {
            idAndNameDtoList.add(idAndNameDto.createNewIdAndNameDto(course.getId(), course.getName()));
        }
        return idAndNameDtoList;
    }

    @Override
    public PageInfo<CourseDto> getCoursesByPage(int start, int pageSize, SearchConditionsDto searchConditionsDto) {
        checkNotNull(searchConditionsDto, "分页查询的条件对象不能为空");
        checkGreaterThanOrIsZero(start, "分页查询的开始数必须大于零");
        checkGreaterThanZero(pageSize, "分页查询的单页总数必须大于零");

        Map<String, Object> map = new HashMap<>();
        Page page = PageHelper.startPage(start, pageSize);
        if (!StringUtils.isEmpty(searchConditionsDto.getName())) {
            map.put("name", searchConditionsDto.getName());
        }
        List<Course> coursesList;
        try {
            coursesList = courseDao.queryCoursesByPage(map);
        } catch (Exception e) {
            logger.info("queryCoursesByPage fail, map:{}, e:{}", map, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据条件分页查询课程记录失败");
        }
        if (coursesList != null) {
            List<CourseDto> courseDtosList = new Page<>(start, pageSize, true);
            Page courseDtosListPage = (Page) courseDtosList;
            courseDtosListPage.setTotal(page.getTotal());
            courseDtosList = queryCourseDtoList(courseDtosList, coursesList);
            return new PageInfo<>(courseDtosList);
        }

        return null;
    }

    private void validateIsSameNameCourse(String name) {
        Course course;
        try {
            course = courseDao.selectByName(name);
        } catch (Exception e) {
            logger.info("selectByUsername fail, name:{}, e:{}", name, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据课程名查询课程记录失败");
        }
        if (course != null) {
            throw new ServiceException(ErrorStateEnum.DB_EXIST_SAME_RECORD.getState(), "添加的课程名为 " + name + " 的课程记录已存在");
        }
    }

    private void updateCourse(Course updateCourse, CourseDto courseDto) {
        if (!StringUtils.isEmpty(courseDto.getName())) {
            updateCourse.setName(courseDto.getName());
        }
        if (!StringUtils.isEmpty(courseDto.getDescription())) {
            updateCourse.setDescription(courseDto.getDescription());
        }
        updateCourse.setModifier(courseDto.getModifier());
        updateCourse.setGmtModify(new Date());
    }

    private CourseDto setCourseDtoGmtCreateAndGmtModify(Course course) {
        CourseDto newCourseDto = courseDto.convertFor(course);
        newCourseDto.setGmtCreate(DateUtil.formatDate(course.getGmtCreate()));
        newCourseDto.setGmtModify(DateUtil.formatDate(course.getGmtModify()));
        return newCourseDto;
    }

    private List<CourseDto> queryCourseDtoList(List<CourseDto> courseDtosList, List<Course> coursesList) {
        for (Course course : coursesList) {
            courseDtosList.add(setCourseDtoGmtCreateAndGmtModify(course));
        }
        return courseDtosList;
    }
}