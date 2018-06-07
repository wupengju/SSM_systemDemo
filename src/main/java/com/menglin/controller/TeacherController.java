package com.menglin.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.menglin.common.ActionResult;
import com.menglin.common.CommonConst;
import com.menglin.dto.*;
import com.menglin.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping("/teacher")
@ResponseBody
public class TeacherController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(TeacherController.class);

    @Resource
    private TeacherService teacherService;
    @Resource
    private NoticeService noticeService;
    @Resource
    private TaskService taskService;
    @Resource
    private ClassTeamService classTeamService;
    @Resource
    private CourseService courseService;
    @Resource
    private StudentTaskService studentTaskService;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public ActionResult<?> list(
            HttpServletRequest request,
            @RequestParam(value = "page") int page,
            @RequestParam(value = "pageSize") int pageSize,
            SearchConditionsDto searchConditionsDto) {
        validateCurrentUserIdentity(request, CommonConst.ADMIN_IDENTITY);

        PageInfo<TeacherDto> pageInfo = teacherService.getTeachersByPage(page, pageSize, searchConditionsDto);

        return getQueryByPageResult(pageInfo, "分页查询的教师不存在");
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public ActionResult<?> add(HttpServletRequest request, @RequestBody TeacherDto teacherDto) {
        JWTPayloadDto JWTPayloadDto = validateCurrentUserIdentityAndGetPayloadInfoFromRequest(request, CommonConst.ADMIN_IDENTITY);

        teacherDto.setCreator(JWTPayloadDto.getUsername());
        int insertId = teacherService.addTeacher(teacherDto);
        logger.info("request: teacher/add , teacher:{}, insertId:{}", JSONObject.toJSONString(teacherDto.convertToTeacher()), insertId);

        return insertId > 0 ? createSuccessActionResult("添加成功") : createFailActionResult("添加失败");
    }

    @RequestMapping(value = "", method = RequestMethod.PUT)
    public ActionResult<?> update(HttpServletRequest request, @RequestBody TeacherDto teacherDto) {
        JWTPayloadDto JWTPayloadDto = validateCurrentUserIdentityAndGetPayloadInfoFromRequest(request, CommonConst.ADMIN_IDENTITY);

        teacherDto.setModifier(JWTPayloadDto.getUsername());
        int updateId = teacherService.updateTeacher(teacherDto);
        logger.info("request: teacher/update , teacher:{}, updateId:{}", JSONObject.toJSONString(teacherDto.convertToTeacher()), updateId);

        return updateId > 0 ? createSuccessActionResult("修改成功") : createFailActionResult("修改失败");
    }

    @RequestMapping(value = "/{ids}", method = RequestMethod.DELETE)
    public ActionResult<?> delete(HttpServletRequest request, @PathVariable String ids) {
        validateCurrentUserIdentity(request, CommonConst.ADMIN_IDENTITY);

        teacherService.batchDeleteTeachersByIds(ids);
        logger.info("request: teacher/delete , ids:{}", ids);

        return createSuccessActionResult("删除成功");
    }

    @RequestMapping(value = "/id/{id}", method = RequestMethod.GET)
    public ActionResult<?> queryById(HttpServletRequest request, @PathVariable Long id) {
        validateCurrentUserIdentity(request, CommonConst.ADMIN_IDENTITY);

        TeacherDto teacherDto = teacherService.getTeacherById(id);

        return teacherDto != null ? createSuccessActionResult(teacherDto) : createFailActionResult("查询的教师不存在");
    }

    @RequestMapping(value = "/username/{username}", method = RequestMethod.GET)
    public ActionResult<?> queryByUsername(HttpServletRequest request, @PathVariable String username) {
        validateCurrentUserIdentity(request, CommonConst.ADMIN_IDENTITY);

        TeacherDto teacherDto = teacherService.getTeacherByUsername(username);

        return teacherDto != null ? createSuccessActionResult(teacherDto) : createFailActionResult("查询的教师不存在");
    }


    /**
     * 系统公告
     */
    @RequestMapping(value = "/notice", method = RequestMethod.GET)
    public ActionResult<?> noticeList(HttpServletRequest request,
                                      @RequestParam(value = "page") int page,
                                      @RequestParam(value = "pageSize") int pageSize,
                                      SearchConditionsDto searchConditionsDto) {
        validateCurrentUserIdentity(request, CommonConst.TEACHER_IDENTITY);

        PageInfo<NoticeDto> pageInfo = noticeService.getNoticesByPage(page, pageSize, searchConditionsDto);

        return getQueryByPageResult(pageInfo, "分页查询的公告不存在");
    }


    /**
     * 创建作业
     */
    @RequestMapping(value = "/task", method = RequestMethod.POST)
    public ActionResult<?> createTask(HttpServletRequest request, @RequestBody TaskDto taskDto) {
        JWTPayloadDto JWTPayloadDto = validateCurrentUserIdentityAndGetPayloadInfoFromRequest(request, CommonConst.TEACHER_IDENTITY);

        taskDto.setCreator(JWTPayloadDto.getUsername());
        taskDto.setTeacherId(JWTPayloadDto.getUserId());
        int insertId = taskService.addTask(taskDto);
        logger.info("request: teacher/task/add , task:{}, insertId:{}", JSONObject.toJSONString(taskDto.convertToTask()), insertId);

        return insertId > 0 ? createSuccessActionResult("添加成功") : createFailActionResult("添加失败");
    }


    /**
     * 修改作业
     */
    @RequestMapping(value = "/task", method = RequestMethod.PUT)
    public ActionResult<?> updateTask(HttpServletRequest request, @RequestBody TaskDto taskDto) {
        JWTPayloadDto JWTPayloadDto = validateCurrentUserIdentityAndGetPayloadInfoFromRequest(request, CommonConst.TEACHER_IDENTITY);

        taskDto.setCreator(JWTPayloadDto.getUsername());
        int updateId = taskService.updateTask(taskDto);
        logger.info("request: teacher/task/update , task:{}, updateId:{}", JSONObject.toJSONString(taskDto.convertToTask()), updateId);

        return updateId > 0 ? createSuccessActionResult("修改成功") : createFailActionResult("修改失败");
    }


    /**
     * 删除作业
     */
    @RequestMapping(value = "/task/{ids}", method = RequestMethod.DELETE)
    public ActionResult<?> deleteTask(HttpServletRequest request, @PathVariable String ids) {
        validateCurrentUserIdentity(request, CommonConst.TEACHER_IDENTITY);

        taskService.batchDeleteTasksByIds(ids);
        logger.info("request: teacher/task/delete , ids:{}", ids);

        return createSuccessActionResult("删除成功");
    }


    /**
     * 待批改作业列表
     */
    @RequestMapping(value = "/studentTask", method = RequestMethod.GET)
    public ActionResult<?> correctingStudentTask(HttpServletRequest request,
                                                 @RequestParam(value = "page") int page,
                                                 @RequestParam(value = "pageSize") int pageSize,
                                                 SearchConditionsDto searchConditionsDto) {
        JWTPayloadDto JWTPayloadDto = validateCurrentUserIdentityAndGetPayloadInfoFromRequest(request, CommonConst.TEACHER_IDENTITY);

        PageInfo<StudentTaskDto> pageInfo = studentTaskService.getCorrectingStudentTasksByPage(page, pageSize, JWTPayloadDto.getUserId(), searchConditionsDto);

        return getQueryByPageResult(pageInfo, "分页查询的待批改作业不存在");
    }


    /**
     * 发布作业
     */
    @RequestMapping(value = "/publishTask", method = RequestMethod.POST)
    public ActionResult<?> publishTask(HttpServletRequest request, @RequestBody PublishTaskDto publishTaskDto) {
        JWTPayloadDto JWTPayloadDto = validateCurrentUserIdentityAndGetPayloadInfoFromRequest(request, CommonConst.TEACHER_IDENTITY);

        teacherService.publishTask(publishTaskDto, JWTPayloadDto.getUserId(), JWTPayloadDto.getUsername());
        logger.info("request: teacher/publishTask/add , publishTaskDto:{}", JSONObject.toJSONString(publishTaskDto));

        return createSuccessActionResult("发布成功");
    }


    /**
     * 批改作业
     */
    @RequestMapping(value = "/studentTask", method = RequestMethod.PUT)
    public ActionResult<?> pendingCorrectedTask(HttpServletRequest request, @RequestBody StudentTaskDto studentTaskDto) {
        JWTPayloadDto JWTPayloadDto = validateCurrentUserIdentityAndGetPayloadInfoFromRequest(request, CommonConst.TEACHER_IDENTITY);

        studentTaskDto.setModifier(JWTPayloadDto.getUsername());
        int updateId = studentTaskService.updateStudentTask(studentTaskDto, JWTPayloadDto.getIdentity());
        logger.info("request: teacher/studentTask/update , studentTask:{}, updateId:{}", JSONObject.toJSONString(studentTaskDto.convertToStudentTask()), updateId);

        return updateId > 0 ? createSuccessActionResult("修改成功") : createFailActionResult("修改失败");
    }


    /**
     * 作业列表
     */
    @RequestMapping(value = "/task", method = RequestMethod.GET)
    public ActionResult<?> taskList(HttpServletRequest request,
                                    @RequestParam(value = "page") int page,
                                    @RequestParam(value = "pageSize") int pageSize,
                                    SearchConditionsDto searchConditionsDto) {
        JWTPayloadDto JWTPayloadDto = validateCurrentUserIdentityAndGetPayloadInfoFromRequest(request, CommonConst.TEACHER_IDENTITY);

        PageInfo<TaskDto> pageInfo = taskService.getTasksByPageAndTeacherId(page, pageSize, JWTPayloadDto.getUserId(), searchConditionsDto);

        return getQueryByPageResult(pageInfo, "分页查询的作业不存在");
    }


    /**
     * 班级列表信息
     */
    @RequestMapping(value = "/classTeam", method = RequestMethod.GET)
    public ActionResult<?> getClassTeamIdAndNameDto(HttpServletRequest request) {
        JWTPayloadDto JWTPayloadDto = validateCurrentUserIdentityAndGetPayloadInfoFromRequest(request, CommonConst.TEACHER_IDENTITY);

        List<IdAndNameDto> idAndNameDtoList = classTeamService.getClassTeamIdAndNameListByTeacherId(JWTPayloadDto.getUserId());

        return idAndNameDtoList != null ? createSuccessActionResult(idAndNameDtoList) : createFailActionResult("班级列表不存在");
    }


    /**
     * 课程列表信息
     */
    @RequestMapping(value = "/course", method = RequestMethod.GET)
    public ActionResult<?> getCourseIdAndNameDto(HttpServletRequest request) {
        JWTPayloadDto JWTPayloadDto = validateCurrentUserIdentityAndGetPayloadInfoFromRequest(request, CommonConst.TEACHER_IDENTITY);

        List<IdAndNameDto> idAndNameDtoList = courseService.getCourseIdAndNameListByTeacherId(JWTPayloadDto.getUserId());

        return idAndNameDtoList != null ? createSuccessActionResult(idAndNameDtoList) : createFailActionResult("班级列表不存在");
    }

    /**
     * 个人信息
     */
    @RequestMapping(value = "/personalInfo", method = RequestMethod.GET)
    public ActionResult<?> personalInfo(HttpServletRequest request) {
        JWTPayloadDto JWTPayloadDto = validateCurrentUserIdentityAndGetPayloadInfoFromRequest(request, CommonConst.TEACHER_IDENTITY);

        TeacherDto teacherDto = teacherService.getTeacherById(JWTPayloadDto.getUserId());

        return teacherDto != null ? createSuccessActionResult(teacherDto) : createFailActionResult("查询的教师不存在");
    }


    /**
     * 修改密码
     */
    @RequestMapping(value = "/password", method = RequestMethod.PUT)
    public ActionResult<?> modifyPassword(HttpServletRequest request, @RequestBody ModifyPasswordInfoDto modifyPasswordInfoDto) {
        JWTPayloadDto JWTPayloadDto = validateCurrentUserIdentityAndGetPayloadInfoFromRequest(request, CommonConst.TEACHER_IDENTITY);

        modifyPasswordInfoDto.setId(JWTPayloadDto.getUserId());
        modifyPasswordInfoDto.setModifier(JWTPayloadDto.getUsername());
        int updateId = teacherService.modifyPassword(modifyPasswordInfoDto);
        logger.info("request: student/update , modifyPasswordInfoDto:{}, updateId:{}", JSONObject.toJSONString(modifyPasswordInfoDto), updateId);

        return updateId > 0 ? createSuccessActionResult("修改成功") : createFailActionResult("修改失败");
    }
}