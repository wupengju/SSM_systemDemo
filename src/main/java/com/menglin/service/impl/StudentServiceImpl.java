package com.menglin.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.menglin.common.CommonConst;
import com.menglin.dao.*;
import com.menglin.dto.ModifyPasswordInfoDto;
import com.menglin.dto.SearchConditionsDto;
import com.menglin.dto.StudentDto;
import com.menglin.entity.*;
import com.menglin.enums.ErrorStateEnum;
import com.menglin.exception.ServiceException;
import com.menglin.redis.RedisKeys;
import com.menglin.redis.RedisUtil;
import com.menglin.service.StudentService;
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

@Service("studentService")
public class StudentServiceImpl implements StudentService {
    private Logger logger = LoggerFactory.getLogger(StudentServiceImpl.class);

    @Resource
    private RedisUtil redisUtil;
    @Resource
    private StudentDao studentDao;
    @Resource
    private RoleDao roleDao;
    @Resource
    private CollegeDao collegeDao;
    @Resource
    private MajorDao majorDao;
    @Resource
    private ClassTeamDao classTeamDao;
    @Resource
    private StudentDto studentDto;

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public int addStudent(StudentDto studentDto) {
        checkNotNull(studentDto, "学生不能为空");
        checkNotEmpty(studentDto.getUsername(), "学生用户名不能为空");
        checkNotEmpty(studentDto.getPassword(), "学生密码不能为空");
        checkNotEmpty(studentDto.getName(), "学生名字不能为空");
        checkNotEmpty(studentDto.getGrade(), "学生年级不能为空");
        checkNotEmpty(studentDto.getCreator(), "学生创建者不能为空");
        checkGreaterThanZero(studentDto.getCollegeId(), "学院 id 不能小于或等于零");
        checkGreaterThanZero(studentDto.getMajorId(), "专业 id 不能小于或等于零");
        checkGreaterThanZero(studentDto.getClassTeamId(), "班级 id 不能小于或等于零");

        validateIsSameUsernameStudent(studentDto.getUsername());

        Student newStudent = studentDto.convertToStudent();
        newStudent.setPassword(encryptionPassword(newStudent.getPassword()));
        // 添加学生的默认角色
        newStudent.setRoleId(3L);
        newStudent.setGmtCreate(new Date());
        int insertId;
        try {
            insertId = studentDao.insertSelective(newStudent);
        } catch (Exception e) {
            logger.info("insert student fail, student:{}, e:{}", JSONObject.toJSONString(newStudent), e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "插入学生记录失败");
        }
        if (insertId > 0) {
            logger.info("insert student success, save student to redis, student:{}", JSONObject.toJSONString(newStudent));
            redisUtil.put(RedisKeys.STUDENT_CACHE_KEY + newStudent.getId(), newStudent);
            return insertId;
        }

        return 0;
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public int updateStudent(StudentDto studentDto) {
        checkNotNull(studentDto, "学生不能为空");
        checkNotEmpty(studentDto.getModifier(), "学生修改者不能为空");
        checkGreaterThanZero(studentDto.getId(), "学生 id 不能小于或等于零");

        Student updateStudent;
        int updateId;
        try {
            updateStudent = studentDao.selectByPrimaryKey(studentDto.getId());
            updateStudent(updateStudent, studentDto);
            updateId = studentDao.updateByPrimaryKey(updateStudent);
        } catch (Exception e) {
            logger.info("update student fail, student:{}, e:{}", JSONObject.toJSONString(studentDto.convertToStudent()), e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "修改学生记录失败");
        }
        if (updateId > 0) {
            logger.info("update student success, save student to redis, student:{}", JSONObject.toJSONString(updateStudent));
            redisUtil.del(RedisKeys.STUDENT_CACHE_KEY + updateStudent.getId());
            redisUtil.put(RedisKeys.STUDENT_CACHE_KEY + updateStudent.getId(), updateStudent);
            return updateId;
        }

        return 0;
    }

    @Override
    public int modifyPassword(ModifyPasswordInfoDto modifyPasswordInfoDto) {
        checkNotNull(modifyPasswordInfoDto, "密码不能为空");
        checkNotEmpty(modifyPasswordInfoDto.getModifier(), "学生修改者不能为空");
        checkNotEmpty(modifyPasswordInfoDto.getOldPassword(), "原密码不能为空");
        checkNotEmpty(modifyPasswordInfoDto.getNewPassword(), "新密码不能为空");
        checkGreaterThanZero(modifyPasswordInfoDto.getId(), "学生 id 不能小于或等于零");

        Student updateStudent;
        int updateId;
        try {
            updateStudent = studentDao.selectByPrimaryKey(modifyPasswordInfoDto.getId());
            if (!encryptionPassword(modifyPasswordInfoDto.getOldPassword()).equals(updateStudent.getPassword())) {
                throw new ServiceException(ErrorStateEnum.BUSINESS_ERROR.getState(), "原密码有误");
            }
            updateStudent.setPassword(encryptionPassword(modifyPasswordInfoDto.getNewPassword()));
            updateStudent.setModifier(modifyPasswordInfoDto.getModifier());
            updateStudent.setGmtModify(new Date());
            updateId = studentDao.updateByPrimaryKey(updateStudent);
        } catch (ServiceException e) {
            logger.info("update student password fail, modifyPasswordInfoDto:{}, e:{}", JSONObject.toJSONString(modifyPasswordInfoDto), e);
            throw new ServiceException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            logger.info("update student password fail, modifyPasswordInfoDto:{}, e:{}", JSONObject.toJSONString(modifyPasswordInfoDto), e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "修改密码失败");
        }
        if (updateId > 0) {
            logger.info("update student password success, save student to redis, student:{}", JSONObject.toJSONString(updateStudent));
            redisUtil.del(RedisKeys.STUDENT_CACHE_KEY + updateStudent.getId());
            redisUtil.put(RedisKeys.STUDENT_CACHE_KEY + updateStudent.getId(), updateStudent);
            return updateId;
        }

        return 0;
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void deleteStudentById(Long id) {
        checkGreaterThanZero(id, "学生 id 不能小于或等于零");

        int deleteId;
        try {
            deleteId = studentDao.deleteByPrimaryKey(id);
        } catch (Exception e) {
            logger.info("delete student fail, id:{}, e:{}", id, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "删除学生记录失败");
        }
        if (deleteId > 0) {
            logger.info("delete student success, id:{}", deleteId);
            redisUtil.del(RedisKeys.STUDENT_CACHE_KEY + deleteId);
        } else {
            logger.info("delete student fail, id:{}", id);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "删除学生记录不存在");
        }
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void batchDeleteStudentsByIds(String ids) {
        checkNotEmpty(ids, "ids 不能为空");

        String[] idsStr = ids.split(",");
        for (String anIdsStr : idsStr) {
            deleteStudentById(Long.parseLong(anIdsStr));
        }
    }

    @Override
    public StudentDto getStudentById(Long id) {
        checkGreaterThanZero(id, "用户 id 不能小于或等于零");

        logger.info("get student by id:{}", id);
        Student student = (Student) redisUtil.get(RedisKeys.STUDENT_CACHE_KEY + id, Student.class);
        if (student != null) {
            logger.info("student in redis, student:{}", JSONObject.toJSONString(student));
            return setStudentDtoGmtCreateAndGmtModify(setStudentDtoNameInfo(student), student);
        }
        Student studentFromMysql;
        try {
            studentFromMysql = studentDao.selectByPrimaryKey(id);
        } catch (Exception e) {
            logger.info("get student by primary key fail, id:{}, e:{}", id, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据 id 查询学生记录失败");
        }
        if (studentFromMysql != null) {
            logger.info("get student from mysql and save student to redis, student:{}", JSONObject.toJSONString(studentFromMysql));
            redisUtil.put(RedisKeys.STUDENT_CACHE_KEY + studentFromMysql.getId(), studentFromMysql);
            return setStudentDtoGmtCreateAndGmtModify(setStudentDtoNameInfo(studentFromMysql), studentFromMysql);
        }

        return null;
    }

    @Override
    public StudentDto getStudentByUsername(String username) {
        checkNotEmpty(username, "用户名不能为空");

        logger.info("get student by username:{}", username);
        Student student = (Student) redisUtil.get(RedisKeys.STUDENT_CACHE_KEY + username, Student.class);
        if (student != null) {
            logger.info("student in redis, student:{}", student);
            return setStudentDtoGmtCreateAndGmtModify(setStudentDtoNameInfo(student), student);
        }
        Student studentFromMysql;
        try {
            studentFromMysql = studentDao.selectByUsername(username);
        } catch (Exception e) {
            logger.info("get student by primary key fail, username:{}, e:{}", username, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据用户名查询学生记录失败");
        }
        if (studentFromMysql != null) {
            logger.info("get student from mysql and save student to redis, student:{}", studentFromMysql);
            redisUtil.put(RedisKeys.STUDENT_CACHE_KEY + studentFromMysql.getUsername(), studentFromMysql);
            return setStudentDtoGmtCreateAndGmtModify(setStudentDtoNameInfo(studentFromMysql), studentFromMysql);
        }

        return null;
    }

    @Override
    public List<StudentDto> getStudentsByClassTeamId(Long classTeamId) {
        checkGreaterThanZero(classTeamId, "班级 id 不能小于或等于零");

        logger.info("get students by classTeamId:{}", classTeamId);
        List<Student> studentsList;
        List<StudentDto> studentDtosList = new ArrayList<>();
        try {
            studentsList = studentDao.selectStudentsByClassTeamId(classTeamId);
        } catch (Exception e) {
            logger.info("get student by classTeamId fail, classTeamId:{}, e:{}", classTeamId, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据班级 id 查询学生记录失败");
        }
        if (!studentsList.isEmpty()) {
            studentDtosList = queryStudentDtoList(studentDtosList, studentsList);
            logger.info("get students from mysql, studentList:{}", studentsList.toString());

            return studentDtosList;
        }

        return studentDtosList;
    }

    @Override
    public PageInfo<StudentDto> getStudentsByPage(int start, int pageSize, SearchConditionsDto searchConditionsDto) {
        checkNotNull(searchConditionsDto, "查询的条件对象不能为空");
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
        List<Student> studentsList;
        try {
            studentsList = studentDao.queryStudentsByPage(map);
        } catch (Exception e) {
            logger.info("queryStudentsByPage fail, map:{}, e:{}", map, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据条件分页查询学生记录失败");
        }
        if (studentsList != null) {
            List<StudentDto> studentDtosList = new Page<>(start, pageSize, true);
            Page studentDtosListPage = (Page) studentDtosList;
            studentDtosListPage.setTotal(page.getTotal());
            studentDtosList = queryStudentDtoList(studentDtosList, studentsList);
            return new PageInfo<>(studentDtosList);
        }

        return null;
    }

    private void validateIsSameUsernameStudent(String username) {
        Student student;
        try {
            student = studentDao.selectByUsername(username);
        } catch (Exception e) {
            logger.info("selectByUsername fail, username:{}, e:{}", username, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据用户名查询学生记录失败");
        }
        if (student != null) {
            throw new ServiceException(ErrorStateEnum.DB_EXIST_SAME_RECORD.getState(), "添加的用户名为 " + username + " 的学生记录已存在");
        }
    }

    private void updateStudent(Student updateStudent, StudentDto studentDto) {
        if (!StringUtils.isEmpty(studentDto.getName())) {
            updateStudent.setName(studentDto.getName());
        }
        if (!StringUtils.isEmpty(studentDto.getUsername())) {
            updateStudent.setUsername(studentDto.getUsername());
        }
        if (!StringUtils.isEmpty(studentDto.getPassword())) {
            updateStudent.setPassword(encryptionPassword(studentDto.getPassword()));
        }
        if (!StringUtils.isEmpty(studentDto.getGrade())) {
            updateStudent.setGrade(studentDto.getGrade());
        }
        if (studentDto.getRoleId() > 0) {
            updateStudent.setRoleId(studentDto.getRoleId());
        }
        if (studentDto.getClassTeamId() > 0) {
            updateStudent.setClassTeamId(studentDto.getClassTeamId());
        }
        if (studentDto.getMajorId() > 0) {
            updateStudent.setMajorId(studentDto.getMajorId());
        }
        if (studentDto.getCollegeId() > 0) {
            updateStudent.setCollegeId(studentDto.getCollegeId());
        }
        updateStudent.setModifier(studentDto.getModifier());
        updateStudent.setGmtModify(new Date());
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

    private String getMajorName(Long id, List<Major> majorList) {
        String name = "";
        for (Major major : majorList) {
            if (major.getId() == id) {
                name = major.getName();
                break;
            }
        }

        return name;
    }

    private String getClassTeamName(Long id, List<ClassTeam> classTeamList) {
        String name = "";
        for (ClassTeam classTeam : classTeamList) {
            if (classTeam.getId() == id) {
                name = classTeam.getName();
                break;
            }
        }

        return name;
    }

    private StudentDto setStudentDtoNameInfo(Student student) {
        StudentDto queryStudentDto = studentDto.convertFor(student);
        Role role = roleDao.selectByPrimaryKey(student.getRoleId());
        queryStudentDto.setRoleName(role.getName());
        College college = collegeDao.selectByPrimaryKey(student.getCollegeId());
        queryStudentDto.setCollegeName(college.getName());
        Major major = majorDao.selectByPrimaryKey(student.getMajorId());
        queryStudentDto.setMajorName(major.getName());
        ClassTeam classTeam = classTeamDao.selectByPrimaryKey(student.getClassTeamId());
        queryStudentDto.setClassTeamName(classTeam.getName());
        return queryStudentDto;
    }

    private StudentDto setStudentDtoGmtCreateAndGmtModify(StudentDto newStudentDto, Student student) {
        newStudentDto.setGmtCreate(DateUtil.formatDate(student.getGmtCreate()));
        newStudentDto.setGmtModify(DateUtil.formatDate(student.getGmtModify()));
        return newStudentDto;
    }

    private List<StudentDto> queryStudentDtoList(List<StudentDto> studentDtosList, List<Student> studentsList) {
        List<Long> roleIdList = new ArrayList<>();
        List<Long> collegeIdList = new ArrayList<>();
        List<Long> majorIdList = new ArrayList<>();
        List<Long> classTeamIdList = new ArrayList<>();
        for (Student student : studentsList) {
            StudentDto newStudentDto = studentDto.convertFor(student);
            roleIdList.add(student.getRoleId());
            collegeIdList.add(student.getCollegeId());
            majorIdList.add(student.getMajorId());
            classTeamIdList.add(student.getClassTeamId());
            newStudentDto = setStudentDtoGmtCreateAndGmtModify(newStudentDto, student);
            studentDtosList.add(newStudentDto);
        }
        Map<String, Object> connectedQueryMap = new HashMap<>();
        connectedQueryMap.put("roleIdList", roleIdList);
        connectedQueryMap.put("collegeIdList", collegeIdList);
        connectedQueryMap.put("majorIdList", majorIdList);
        connectedQueryMap.put("classTeamIdList", classTeamIdList);
        List<Role> roleList = roleDao.queryRolesByIdList(connectedQueryMap);
        List<College> collegeList = collegeDao.queryCollegesByIdList(connectedQueryMap);
        List<Major> majorList = majorDao.queryMajorsByIdList(connectedQueryMap);
        List<ClassTeam> classTeamList = classTeamDao.queryClassTeamsByIdList(connectedQueryMap);
        for (StudentDto curStudentDto : studentDtosList) {
            curStudentDto.setRoleName(getRoleName(curStudentDto.getRoleId(), roleList));
            curStudentDto.setCollegeName(getCollegeName(curStudentDto.getCollegeId(), collegeList));
            curStudentDto.setMajorName(getMajorName(curStudentDto.getMajorId(), majorList));
            curStudentDto.setClassTeamName(getClassTeamName(curStudentDto.getClassTeamId(), classTeamList));
        }
        return studentDtosList;
    }

    private String encryptionPassword(String password) {
        return MD5Util.MD5Encode(password + CommonConst.SERVER_SALT, "");
    }
}
