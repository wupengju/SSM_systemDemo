package com.menglin.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.menglin.common.CommonConst;
import com.menglin.dao.AdminDao;
import com.menglin.dao.StudentDao;
import com.menglin.dao.TeacherDao;
import com.menglin.dto.JWTCheckInfoDto;
import com.menglin.dto.JWTPayloadDto;
import com.menglin.dto.LoginResponseDto;
import com.menglin.dto.UserDto;
import com.menglin.entity.Admin;
import com.menglin.entity.Student;
import com.menglin.entity.Teacher;
import com.menglin.enums.ErrorStateEnum;
import com.menglin.exception.ServiceException;
import com.menglin.service.UserService;
import com.menglin.util.FileUtil;
import com.menglin.util.JWTUtil;
import com.menglin.util.MD5Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.UnsupportedEncodingException;

import static com.menglin.common.AssertArguments.checkNotEmpty;
import static com.menglin.common.AssertArguments.checkNotNull;

@Service("userService")
public class UserServiceImpl implements UserService {
    private Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Resource
    private StudentDao studentDao;
    @Resource
    private TeacherDao teacherDao;
    @Resource
    private AdminDao adminDao;

    @Override
    public LoginResponseDto login(String username, String actSignature) {
        checkNotEmpty(username, "用户名不能为空");
        checkNotEmpty(actSignature, "签名不能为空");

        LoginResponseDto loginResponseDto;
        actSignature = MD5Util.MD5Encode(actSignature + CommonConst.SERVER_SALT, "");
        if (username.length() == 10) { // 学生
            Student student;
            try {
                student = studentDao.selectByUsername(username);
                loginResponseDto = validateStudentUser(student, actSignature);
            } catch (UnsupportedEncodingException e) {
                logger.info("student login create Token fail, username:{}, e:{}", username, e);
                throw new ServiceException("创建 Token 失败");
            } catch (ServiceException e) {
                logger.info("student login fail, username:{}, e:{}", username, e);
                throw new ServiceException(e.getCode(), e.getMessage());
            } catch (RuntimeException e) {
                logger.info("student login access sql error, username:{}, e:{}", username, e);
                throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), ErrorStateEnum.DB_EXCEPTION.getStateInfo());
            }
            return loginResponseDto;
        } else if (username.length() == 8) { // 老师
            Teacher teacher;
            try {
                teacher = teacherDao.selectByUsername(username);
                loginResponseDto = validateTeacherUser(teacher, actSignature);
            } catch (UnsupportedEncodingException e) {
                logger.info("teacher login create Token fail, username:{}, e:{}", username, e);
                throw new ServiceException("创建 Token 失败");
            } catch (ServiceException e) {
                logger.info("teacher login fail, username:{}, e:{}", username, e);
                throw new ServiceException(e.getCode(), e.getMessage());
            } catch (RuntimeException e) {
                logger.info("teacher login access sql error, username:{}, e:{}", username, e);
                throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), ErrorStateEnum.DB_EXCEPTION.getStateInfo());
            }
            return loginResponseDto;
        } else if (username.length() <= 7) { // 管理员
            Admin admin;
            try {
                admin = adminDao.selectByUsername(username);
                loginResponseDto = validateAdminUser(admin, actSignature);
            } catch (UnsupportedEncodingException e) {
                logger.info("admin login create Token fail, username:{}, e:{}", username, e);
                throw new ServiceException("创建 Token 失败");
            } catch (ServiceException e) {
                logger.info("admin login fail, username:{}, e:{}", username, e);
                throw new ServiceException(e.getCode(), e.getMessage());
            } catch (RuntimeException e) {
                logger.info("admin login access sql error, username:{}, e:{}", username, e);
                throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), ErrorStateEnum.DB_EXCEPTION.getStateInfo());
            }
            return loginResponseDto;
        } else {
            logger.info("username format is error, username:{}", username);
            throw new ServiceException("用户名格式不正确，请确认后重新登录");
        }
    }

    @Override
    public LoginResponseDto quit(String userPreviewFileFolderPath, JWTPayloadDto JWTPayloadDto) {
        checkNotNull(JWTPayloadDto, "用户信息对象不能为空");
        checkNotEmpty(userPreviewFileFolderPath, "用户名不能为空");

        try {

            // 删除该用户在会话过程中预览生成的所有 PDF 文件（文件夹）
            FileUtil.deleteDir(new File(userPreviewFileFolderPath));
        } catch (Exception e) {
            logger.info("quit fail, delete preview file folder fail, userPreviewFileFolderPath:{}", userPreviewFileFolderPath);
            throw new ServiceException("删除用户预览文件文件夹失败");
        }
        LoginResponseDto loginResponseDto;
        try {
            loginResponseDto = createUserBo(JWTPayloadDto.getUserId(), JWTPayloadDto.getUsername(), JWTPayloadDto.getName(), JWTPayloadDto.getIdentity(), 0L);
        } catch (UnsupportedEncodingException e) {
            logger.info("quit fail, JWTPayloadDto:{}, e:{}", JSONObject.toJSONString(JWTPayloadDto), e);
            throw new ServiceException("创建 Token 失败");
        } catch (ServiceException e) {
            logger.info("quit fail, JWTPayloadDto:{}, e:{}", JSONObject.toJSONString(JWTPayloadDto), e);
            throw new ServiceException(e.getCode(), e.getMessage());
        } catch (RuntimeException e) {
            logger.info("quit access sql error, JWTPayloadDto:{}, e:{}", JSONObject.toJSONString(JWTPayloadDto), e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), ErrorStateEnum.DB_EXCEPTION.getStateInfo());
        }

        return loginResponseDto;
    }

    public UserDto registerNewAccount(UserDto userDto) {
        checkNotNull(userDto, "注册用户不能为空");
        checkNotEmpty(userDto.getPassword(), "密码不能为空");

        // MD5(signature+MD5(server uuid))
        userDto.setPassword(MD5Util.MD5Encode(userDto.getPassword() + CommonConst.SERVER_SALT, ""));

        return userDto;
    }

    @Override
    public JWTPayloadDto getPayloadInfo(String jwt) {
        JWTPayloadDto JWTPayloadDto;
        try {
            JWTPayloadDto = JWTUtil.decrypt(jwt, JWTPayloadDto.class);
        } catch (ServiceException e) {
            logger.info("getPayloadInfo fail, jwt:{}, e:{}", jwt, e);
            throw e;
        }

        return JWTPayloadDto;
    }

    @Override
    public boolean checkJWT(JWTCheckInfoDto jwtCheckInfoDto) {

        // 从Header中取数据
        String jwt = jwtCheckInfoDto.getAuthorization();
        String currentName = jwtCheckInfoDto.getCurrentName();
        logger.info("checkJWT, RequestMapping:{}, jwt:{}, currentName:{}", jwtCheckInfoDto.getRequestUrI(), jwt, currentName);
        JWTPayloadDto JWTPayloadDto;
        try {
            JWTPayloadDto = JWTUtil.decrypt(jwt, JWTPayloadDto.class);
        } catch (ServiceException e) {
            logger.info("checkJWT fail, jwt:{}, currentName:{}, e:{}", jwt, currentName, e);
            throw e;
        }

        // 利用name 去查询密码
        return JWTPayloadDto != null && currentName.equals(JWTPayloadDto.getUsername());
    }

    private LoginResponseDto validateStudentUser(Student student, String actSignature) throws UnsupportedEncodingException {
        String expectedSignature = student.getPassword();
        if (actSignature.equals(expectedSignature)) {
            return createUserBo(student.getId(), student.getUsername(), student.getName(), CommonConst.STUDENT_IDENTITY, CommonConst.JWT_MAXAGE);
        } else {
            logger.info("validate signature fail, actSignature:{}, student:{}", actSignature, JSONObject.toJSONString(student));
            throw new ServiceException(ErrorStateEnum.SIGNATURE_VALIDATE_FAIL.getState(), "用户名或密码错误");
        }
    }

    private LoginResponseDto validateTeacherUser(Teacher teacher, String actSignature) throws UnsupportedEncodingException {
        String expectedSignature = teacher.getPassword();
        if (actSignature.equals(expectedSignature)) {
            return createUserBo(teacher.getId(), teacher.getUsername(), teacher.getName(), CommonConst.TEACHER_IDENTITY, CommonConst.JWT_MAXAGE);
        } else {
            logger.info("validate signature fail, actSignature:{}, teacher:{}", actSignature, JSONObject.toJSONString(teacher));
            throw new ServiceException(ErrorStateEnum.SIGNATURE_VALIDATE_FAIL.getState(), "用户名或密码错误");
        }
    }

    private LoginResponseDto validateAdminUser(Admin admin, String actSignature) throws UnsupportedEncodingException {
        String expectedSignature = admin.getPassword();
        if (actSignature.equals(expectedSignature)) {
            return createUserBo(admin.getId(), admin.getUsername(), admin.getUsername(), CommonConst.ADMIN_IDENTITY, CommonConst.JWT_MAXAGE);
        } else {
            logger.info("validate signature fail, actSignature:{}, admin:{}", actSignature, JSONObject.toJSONString(admin));
            throw new ServiceException(ErrorStateEnum.SIGNATURE_VALIDATE_FAIL.getState(), "用户名或密码错误");
        }
    }

    private LoginResponseDto createUserBo(Long id, String username, String name, String identity, Long jwtMaxAge) throws UnsupportedEncodingException {
        LoginResponseDto loginResponseDto = new LoginResponseDto();
        JWTPayloadDto JWTPayloadDto = new JWTPayloadDto();
        JWTPayloadDto.setUserId(id);
        JWTPayloadDto.setUsername(username);
        JWTPayloadDto.setName(name);
        JWTPayloadDto.setIdentity(identity);
        String jwt = JWTUtil.encrypt(JWTPayloadDto, jwtMaxAge);
        loginResponseDto.setUsername(username);
        loginResponseDto.setName(name);
        loginResponseDto.setIdentity(identity);
        loginResponseDto.setJwt(jwt);
        return loginResponseDto;
    }
}
