package com.menglin.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.menglin.common.CommonConst;
import com.menglin.dao.*;
import com.menglin.dto.ModifyPasswordInfoDto;
import com.menglin.dto.PublishTaskDto;
import com.menglin.dto.SearchConditionsDto;
import com.menglin.dto.TeacherDto;
import com.menglin.entity.*;
import com.menglin.enums.ErrorStateEnum;
import com.menglin.exception.ServiceException;
import com.menglin.redis.RedisKeys;
import com.menglin.redis.RedisUtil;
import com.menglin.service.TeacherService;
import com.menglin.util.DateUtil;
import com.menglin.util.MD5Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;

import static com.menglin.common.AssertArguments.*;

@Service("teacherService")
public class TeacherServiceImpl implements TeacherService {
    private Logger logger = LoggerFactory.getLogger(TeacherServiceImpl.class);

    @Resource
    private RedisUtil redisUtil;
    @Resource
    private TeacherDao teacherDao;
    @Resource
    private TeacherClassTeamDao teacherClassTeamDao;
    @Resource
    private TaskDao taskDao;
    @Resource
    private StudentDao studentDao;
    @Resource
    private StudentTaskDao studentTaskDao;
    @Resource
    private RoleDao roleDao;
    @Resource
    private CollegeDao collegeDao;
    @Resource
    private TeacherDto teacherDto;

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public int addTeacher(TeacherDto teacherDto) {
        checkNotNull(teacherDto, "教师不能为空");
        checkNotEmpty(teacherDto.getUsername(), "教师用户名不能为空");
        checkNotEmpty(teacherDto.getPassword(), "教师密码不能为空");
        checkNotEmpty(teacherDto.getName(), "教师名字不能为空");
        checkNotEmpty(teacherDto.getCreator(), "教师创建者不能为空");
        checkGreaterThanZero(teacherDto.getCollegeId(), "学院 id 不能小于或等于零");

        validateIsSameUsernameTeacher(teacherDto.getUsername());

        Teacher newTeacher = teacherDto.convertToTeacher();
        newTeacher.setPassword(encryptionPassword(newTeacher.getPassword()));
        // 添加教师的默认角色
        newTeacher.setRoleId(2L);
        newTeacher.setGmtCreate(new Date());
        int insertId;
        try {
            insertId = teacherDao.insertSelective(newTeacher);
        } catch (Exception e) {
            logger.info("insert teacher fail, teacher:{}, e:{}", JSONObject.toJSONString(newTeacher), e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "插入教师记录失败");
        }
        if (insertId > 0) {
            logger.info("insert teacher success, save teacher to redis, teacher:{}", JSONObject.toJSONString(newTeacher));
            redisUtil.put(RedisKeys.TEACHER_CACHE_KEY + newTeacher.getId(), newTeacher);
            return insertId;
        }

        return 0;
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public int updateTeacher(TeacherDto teacherDto) {
        checkNotNull(teacherDto, "教师不能为空");
        checkNotEmpty(teacherDto.getModifier(), "教师修改者不能为空");
        checkGreaterThanZero(teacherDto.getId(), "教师 id 不能小于或等于零");

        Teacher updateTeacher;
        int updateId;
        try {
            updateTeacher = teacherDao.selectByPrimaryKey(teacherDto.getId());
            updateTeacher(updateTeacher, teacherDto);
            updateId = teacherDao.updateByPrimaryKey(updateTeacher);
        } catch (Exception e) {
            logger.info("update teacher fail, teacher:{}, e:{}", JSONObject.toJSONString(teacherDto.convertToTeacher()), e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "修改教师记录失败");
        }
        if (updateId > 0) {
            logger.info("update teacher success, save teacher to redis, teacher:{}", JSONObject.toJSONString(updateTeacher));
            redisUtil.del(RedisKeys.TEACHER_CACHE_KEY + updateTeacher.getId());
            redisUtil.put(RedisKeys.TEACHER_CACHE_KEY + updateTeacher.getId(), updateTeacher);
            return updateId;
        }

        return 0;
    }

    @Override
    public int modifyPassword(ModifyPasswordInfoDto modifyPasswordInfoDto) {
        checkNotNull(modifyPasswordInfoDto, "密码不能为空");
        checkNotEmpty(modifyPasswordInfoDto.getModifier(), "教师修改者不能为空");
        checkNotEmpty(modifyPasswordInfoDto.getOldPassword(), "原密码不能为空");
        checkNotEmpty(modifyPasswordInfoDto.getNewPassword(), "新密码不能为空");
        checkGreaterThanZero(modifyPasswordInfoDto.getId(), "教师 id 不能小于或等于零");

        Teacher updateTeacher;
        int updateId;
        try {
            updateTeacher = teacherDao.selectByPrimaryKey(modifyPasswordInfoDto.getId());
            if (!encryptionPassword(modifyPasswordInfoDto.getOldPassword()).equals(updateTeacher.getPassword())) {
                throw new ServiceException(ErrorStateEnum.BUSINESS_ERROR.getState(), "原密码有误");
            }
            updateTeacher.setPassword(encryptionPassword(modifyPasswordInfoDto.getNewPassword()));
            updateTeacher.setModifier(modifyPasswordInfoDto.getModifier());
            updateTeacher.setGmtModify(new Date());
            updateId = teacherDao.updateByPrimaryKey(updateTeacher);
        } catch (ServiceException e) {
            logger.info("update teacher password fail, modifyPasswordInfoDto:{}, e:{}", JSONObject.toJSONString(modifyPasswordInfoDto), e);
            throw new ServiceException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            logger.info("update teacher password fail, modifyPasswordInfoDto:{}, e:{}", JSONObject.toJSONString(modifyPasswordInfoDto), e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "修改密码失败");
        }
        if (updateId > 0) {
            logger.info("update teacher password success, save teacher to redis, teacher:{}", JSONObject.toJSONString(updateTeacher));
            redisUtil.del(RedisKeys.TEACHER_CACHE_KEY + updateTeacher.getId());
            redisUtil.put(RedisKeys.TEACHER_CACHE_KEY + updateTeacher.getId(), updateTeacher);
            return updateId;
        }

        return 0;
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void deleteTeacherById(Long id) {
        checkGreaterThanZero(id, "教师 id 不能小于或等于零");

        int deleteId;
        try {
            deleteId = teacherDao.deleteByPrimaryKey(id);
        } catch (Exception e) {
            logger.info("delete teacher fail, id:{}, e:{}", id, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "删除教师记录失败");
        }
        if (deleteId > 0) {
            logger.info("delete teacher success, id:{}", deleteId);
            redisUtil.del(RedisKeys.TEACHER_CACHE_KEY + deleteId);
        } else {
            logger.info("delete teacher fail, id:{}", id);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "删除教师记录不存在");
        }
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void batchDeleteTeachersByIds(String ids) {
        checkNotEmpty(ids, "ids 不能为空");

        String[] idsStr = ids.split(",");
        for (String anIdsStr : idsStr) {
            deleteTeacherById(Long.parseLong(anIdsStr));
        }
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void publishTask(PublishTaskDto publishTaskDto, Long teacherId, String username) {
        checkNotNull(publishTaskDto, "发布的作业不能为空");
        checkNotEmpty(publishTaskDto.getClassTeamIds(), "班级ids不能为空");
        checkNotEmpty(username, "用户名不能为空");
        checkGreaterThanZero(publishTaskDto.getTaskId(), "作业 id 不能小于或等于零");

        String[] classTeamIdsStr = publishTaskDto.getClassTeamIds().split(",");
        List<Student> studentList = new ArrayList<>();
        try {
            Task task = taskDao.selectByPrimaryKey(publishTaskDto.getTaskId());
            for (String classTeamIdStr : classTeamIdsStr) {
                Long classTeamId = Long.parseLong(classTeamIdStr);
                TeacherClassTeam teacherClassTeam = teacherClassTeamDao.selectByTeacherIdAndClassTeamId(teacherId, classTeamId);
                if (teacherClassTeam == null) {
                    throw new ServiceException(ErrorStateEnum.BUSINESS_ERROR.getState(), "发布的作业班级：" + classTeamId + "，没在教师：" + username + " 的授课班级中");
                }
                studentList.addAll(studentDao.selectStudentsByClassTeamIdAndCourseId(classTeamId, task.getCourseId()));
            }
            for (Student student : studentList) {
                StudentTask studentTask = new StudentTask();
                studentTask.setStudentId(student.getId());
                studentTask.setTaskId(task.getId());
                studentTask.setCreator(username);
                studentTaskDao.insert(studentTask);
            }
        } catch (ServiceException e) {
            logger.info("publishTask fail, publishTaskDto:{}, username:{}", JSONObject.toJSONString(publishTaskDto), username);
            throw new ServiceException(e.getCode(), "作业发布失败，" + e.getMessage());
        } catch (Exception e) {
            logger.info("publishTask fail, publishTaskDto:{}, username:{}", JSONObject.toJSONString(publishTaskDto), username);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "作业发布失败");
        }
    }

    @Override
    public TeacherDto getTeacherById(Long id) {
        checkGreaterThanZero(id, "教师 id 不能小于或等于零");

        logger.info("get teacher by id:{}", id);
        Teacher teacher = (Teacher) redisUtil.get(RedisKeys.TEACHER_CACHE_KEY + id, Teacher.class);
        if (teacher != null) {
            logger.info("teacher in redis, teacher:{}", JSONObject.toJSONString(teacherDto));
            return setTeacherDtoGmtCreateAndGmtModify(setTeacherDtoNameInfo(teacher), teacher);
        }
        Teacher teacherFromMysql;
        try {
            teacherFromMysql = teacherDao.selectByPrimaryKey(id);
        } catch (Exception e) {
            logger.info("get teacher by primary key fail, id:{}, e:{}", id, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据 id 查询教师记录失败");
        }
        if (teacherFromMysql != null) {
            logger.info("get teacher from mysql and save teacher to redis, teacher:{}", JSONObject.toJSONString(teacherFromMysql));
            redisUtil.put(RedisKeys.TEACHER_CACHE_KEY + teacherFromMysql.getId(), teacherFromMysql);
            return setTeacherDtoGmtCreateAndGmtModify(setTeacherDtoNameInfo(teacherFromMysql), teacherFromMysql);
        }

        return null;
    }

    @Override
    public TeacherDto getTeacherByUsername(String username) {
        checkNotEmpty(username, "用户名不能为空");

        logger.info("get teacher by username:{}", username);
        Teacher teacher = (Teacher) redisUtil.get(RedisKeys.TEACHER_CACHE_KEY + username, Teacher.class);
        if (teacher != null) {
            logger.info("teacher in redis, teacher:{}", teacher);
            return setTeacherDtoGmtCreateAndGmtModify(setTeacherDtoNameInfo(teacher), teacher);
        }
        Teacher teacherFromMysql;
        try {
            teacherFromMysql = teacherDao.selectByUsername(username);
        } catch (Exception e) {
            logger.info("get teacher by primary key fail, username:{}, e:{}", username, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据用户名查询教师记录失败");
        }
        if (teacherFromMysql != null) {
            logger.info("get teacher from mysql and save teacher to redis, teacher:{}", teacherFromMysql);
            redisUtil.put(RedisKeys.TEACHER_CACHE_KEY + teacherFromMysql.getUsername(), teacherFromMysql);
            return setTeacherDtoGmtCreateAndGmtModify(setTeacherDtoNameInfo(teacherFromMysql), teacherFromMysql);
        }

        return null;
    }

    @Override
    public PageInfo<TeacherDto> getTeachersByPage(int start, int pageSize, SearchConditionsDto searchConditionsDto) {
        checkNotNull(teacherDto, "教师不能为空");
        checkGreaterThanOrIsZero(start, "分页查询的开始数必须大于零");
        checkGreaterThanZero(pageSize, "分页查询的单页总数必须大于零");

        Map<String, Object> map = new HashMap<>();
        Page page = PageHelper.startPage(start, pageSize);
        if (!StringUtils.isEmpty(searchConditionsDto.getUsername())) {
            map.put("username", searchConditionsDto.getUsername());
        }
        if (!StringUtils.isEmpty(searchConditionsDto.getName())) {
            map.put("name", searchConditionsDto.getName());
        }
        List<Teacher> teachersList;
        try {
            teachersList = teacherDao.queryTeachersByPage(map);
        } catch (Exception e) {
            logger.info("queryTeachersByPage fail, map:{}, e:{}", map, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据条件分页查询教师记录失败");
        }
        if (teachersList != null) {
            List<TeacherDto> teacherDtosList = new Page<>(start, pageSize, true);
            Page teacherDtosListPage = (Page) teacherDtosList;
            teacherDtosListPage.setTotal(page.getTotal());
            teacherDtosList = queryTeacherDtoList(teacherDtosList, teachersList);
            return new PageInfo<>(teacherDtosList);
        }

        return null;
    }

    private void validateIsSameUsernameTeacher(String username) {
        Teacher teacher;
        try {
            teacher = teacherDao.selectByUsername(username);
        } catch (Exception e) {
            logger.info("selectByUsername fail, username:{}, e:{}", username, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据用户名查询教师记录失败");
        }
        if (teacher != null) {
            throw new ServiceException(ErrorStateEnum.DB_EXIST_SAME_RECORD.getState(), "添加的用户名为 " + username + " 的教师记录已存在");
        }
    }

    private void updateTeacher(Teacher updateTeacher, TeacherDto teacherDto) {
        if (!StringUtils.isEmpty(teacherDto.getName())) {
            updateTeacher.setName(teacherDto.getName());
        }
        if (!StringUtils.isEmpty(teacherDto.getUsername())) {
            updateTeacher.setUsername(teacherDto.getUsername());
        }
        if (!StringUtils.isEmpty(teacherDto.getPassword())) {
            updateTeacher.setPassword(encryptionPassword(teacherDto.getPassword()));
        }
        if (teacherDto.getRoleId() > 0) {
            updateTeacher.setRoleId(teacherDto.getRoleId());
        }
        if (teacherDto.getCollegeId() > 0) {
            updateTeacher.setCollegeId(teacherDto.getCollegeId());
        }
        updateTeacher.setModifier(teacherDto.getModifier());
        updateTeacher.setGmtModify(new Date());
    }

    private String getRoleName(Long id, List<Role> roleList) {
        String name = "";
        for (Role role : roleList) {
            if (role.getId() == id) {
                name = role.getName();
                break;
            }
        }

        return name;
    }

    private String getCollegeName(Long id, List<College> collegeList) {
        String name = "";
        for (College college : collegeList) {
            if (college.getId() == id) {
                name = college.getName();
                break;
            }
        }

        return name;
    }

    private TeacherDto setTeacherDtoNameInfo(Teacher teacher) {
        TeacherDto queryTeacherDto = teacherDto.convertFor(teacher);
        Role role = roleDao.selectByPrimaryKey(teacher.getRoleId());
        queryTeacherDto.setRoleName(role.getName());
        College college = collegeDao.selectByPrimaryKey(teacher.getCollegeId());
        queryTeacherDto.setCollegeName(college.getName());
        return queryTeacherDto;
    }

    private TeacherDto setTeacherDtoGmtCreateAndGmtModify(TeacherDto newTeacherDto, Teacher teacher) {
        newTeacherDto.setGmtCreate(DateUtil.formatDate(teacher.getGmtCreate()));
        newTeacherDto.setGmtModify(DateUtil.formatDate(teacher.getGmtModify()));
        return newTeacherDto;
    }

    private List<TeacherDto> queryTeacherDtoList(List<TeacherDto> teacherDtosList, List<Teacher> teachersList) {
        List<Long> roleIdList = new ArrayList<>();
        List<Long> collegeIdList = new ArrayList<>();
        for (Teacher teacher : teachersList) {
            TeacherDto newTeacherDto = teacherDto.convertFor(teacher);
            roleIdList.add(teacher.getRoleId());
            collegeIdList.add(teacher.getCollegeId());
            newTeacherDto = setTeacherDtoGmtCreateAndGmtModify(newTeacherDto, teacher);
            teacherDtosList.add(newTeacherDto);
        }
        Map<String, Object> connectedQueryMap = new HashMap<>();
        connectedQueryMap.put("roleIdList", roleIdList);
        connectedQueryMap.put("collegeIdList", collegeIdList);
        List<Role> roleList = roleDao.queryRolesByIdList(connectedQueryMap);
        List<College> collegeList = collegeDao.queryCollegesByIdList(connectedQueryMap);
        for (TeacherDto curTeacherDto : teacherDtosList) {
            curTeacherDto.setRoleName(getRoleName(curTeacherDto.getRoleId(), roleList));
            curTeacherDto.setCollegeName(getCollegeName(curTeacherDto.getCollegeId(), collegeList));
        }

        return teacherDtosList;
    }

    private String encryptionPassword(String password) {
        return MD5Util.MD5Encode(password + CommonConst.SERVER_SALT, "");
    }
}

