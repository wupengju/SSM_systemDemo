package com.menglin.controller;

import com.alibaba.fastjson.JSONObject;
import com.menglin.common.ActionResult;
import com.menglin.dto.*;
import com.menglin.service.StudentService;
import com.menglin.service.TeacherService;
import com.menglin.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Controller
@ResponseBody
public class UserController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(UserController.class);

    @Resource
    private UserService userService;
    @Resource
    private StudentService studentService;
    @Resource
    private TeacherService teacherService;

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ActionResult<?> login(HttpServletRequest request, @RequestBody LoginUserDto loginUserDto) {
        JWTPayloadDto JWTPayloadDto = getPayloadInfoFromRequest(request);
        if (JWTPayloadDto != null) {
            return createSuccessActionResult("已经登录用户: " + JWTPayloadDto.getUsername() + ", 身份: " + JWTPayloadDto.getIdentity());
        }
        LoginResponseDto loginResponseDto;
        try {
            loginResponseDto = userService.login(loginUserDto.getUsername(), loginUserDto.getSignature());
        } catch (Exception e) {
            logger.info("/login，login fail, LoginUserDto:{}", JSONObject.toJSONString(loginUserDto));
            return createFailActionResult("登录失败，请确认登录信息无误后再重试");
        }
        logger.info("/login， LoginUserDto:", JSONObject.toJSONString(loginUserDto));

        return createSuccessActionResult(loginResponseDto);
    }

    @RequestMapping(value = "/register/student", method = RequestMethod.POST)
    public ActionResult<?> registerStudent(@RequestBody StudentDto studentDto) {
        studentDto = (StudentDto) userService.registerNewAccount(studentDto);
        studentDto.setCreator(studentDto.getUsername());
        int insertId = studentService.addStudent(studentDto);
        logger.info("/register/student， student:{}", JSONObject.toJSONString(studentDto.convertToStudent()));

        return insertId > 0 ? createSuccessActionResult("注册成功") : createSuccessActionResult("注册失败");
    }

    @RequestMapping(value = "/register/teacher", method = RequestMethod.POST)
    public ActionResult<?> registerTeacher(@RequestBody TeacherDto teacherDto) {
        teacherDto = (TeacherDto) userService.registerNewAccount(teacherDto);
        teacherDto.setCreator(teacherDto.getUsername());
        int insertId = teacherService.addTeacher(teacherDto);
        logger.info("/register/teacher， teacher:{}", JSONObject.toJSONString(teacherDto.convertToTeacher()));

        return insertId > 0 ? createSuccessActionResult("注册成功") : createSuccessActionResult("注册失败");
    }

    @RequestMapping(value = "/quit", method = RequestMethod.POST)
    public ActionResult<?> quit(HttpServletRequest request) {
        JWTPayloadDto JWTPayloadDto = getPayloadInfoFromRequest(request);
        if (JWTPayloadDto != null) {
            String currentUserPreviewFileRealPath = "/pdfs/" + JWTPayloadDto.getUsername() + "/";
            String realPath = request.getServletContext().getRealPath(currentUserPreviewFileRealPath);
            logger.info("/quit，currentName:{}", JWTPayloadDto.getUsername());
            LoginResponseDto loginResponseDto;
            try {
                loginResponseDto = userService.quit(realPath, JWTPayloadDto);
            } catch (Exception e) {
                logger.info("/quit，quit fail, JWTPayloadDto:{}", JSONObject.toJSONString(JWTPayloadDto));
                return createFailActionResult("退出失败, " + e.getMessage());
            }
            return createSuccessActionResult(loginResponseDto);
        } else {
            return createFailActionResult("请先登录用户");
        }
    }
}