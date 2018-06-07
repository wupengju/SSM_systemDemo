package com.menglin.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.menglin.common.ActionResult;
import com.menglin.common.CommonConst;
import com.menglin.dto.*;
import com.menglin.service.NoticeService;
import com.menglin.service.StudentService;
import com.menglin.service.StudentTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/student")
@ResponseBody
public class StudentController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(StudentController.class);

    @Resource
    private StudentService studentService;
    @Resource
    private NoticeService noticeService;
    @Resource
    private StudentTaskService studentTaskService;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public ActionResult<?> list(
            HttpServletRequest request,
            @RequestParam(value = "page") int page,
            @RequestParam(value = "pageSize") int pageSize,
            SearchConditionsDto searchConditionsDto) {
        validateCurrentUserIdentity(request, CommonConst.ADMIN_IDENTITY);

        PageInfo<StudentDto> pageInfo = studentService.getStudentsByPage(page, pageSize, searchConditionsDto);

        return getQueryByPageResult(pageInfo, "分页查询的学生不存在");
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public ActionResult<?> add(HttpServletRequest request, @RequestBody StudentDto studentDto) {
        JWTPayloadDto JWTPayloadDto = validateCurrentUserIdentityAndGetPayloadInfoFromRequest(request, CommonConst.ADMIN_IDENTITY);

        studentDto.setCreator(JWTPayloadDto.getUsername());
        int insertId = studentService.addStudent(studentDto);
        logger.info("request: student/add , student:{}, insertId:{}", JSONObject.toJSONString(studentDto.convertToStudent()), insertId);

        return insertId > 0 ? createSuccessActionResult("添加成功") : createFailActionResult("添加失败");
    }

    @RequestMapping(value = "", method = RequestMethod.PUT)
    public ActionResult<?> update(HttpServletRequest request, @RequestBody StudentDto studentDto) {
        JWTPayloadDto JWTPayloadDto = validateCurrentUserIdentityAndGetPayloadInfoFromRequest(request, CommonConst.ADMIN_IDENTITY);

        studentDto.setModifier(JWTPayloadDto.getUsername());
        int updateId = studentService.updateStudent(studentDto);
        logger.info("request: student/update , student:{}, updateId:{}", JSONObject.toJSONString(studentDto.convertToStudent()), updateId);

        return updateId > 0 ? createSuccessActionResult("修改成功") : createFailActionResult("修改失败");
    }

    @RequestMapping(value = "/{ids}", method = RequestMethod.DELETE)
    public ActionResult<?> delete(HttpServletRequest request, @PathVariable String ids) {
        validateCurrentUserIdentity(request, CommonConst.ADMIN_IDENTITY);

        studentService.batchDeleteStudentsByIds(ids);
        logger.info("request: student/delete , ids:{}", ids);

        return createSuccessActionResult("删除成功");
    }

    @RequestMapping(value = "/id/{id}", method = RequestMethod.GET)
    public ActionResult<?> queryById(HttpServletRequest request, @PathVariable Long id) {
        validateCurrentUserIdentity(request, CommonConst.ADMIN_IDENTITY);

        StudentDto studentDto = studentService.getStudentById(id);

        return studentDto != null ? createSuccessActionResult(studentDto) : createFailActionResult("查询的学生不存在");
    }


    /**
     * 系统公告
     */
    @RequestMapping(value = "/notice", method = RequestMethod.GET)
    public ActionResult<?> noticeList(HttpServletRequest request,
                                      @RequestParam(value = "page") int page,
                                      @RequestParam(value = "pageSize") int pageSize,
                                      SearchConditionsDto searchConditionsDto) {
        validateCurrentUserIdentity(request, CommonConst.STUDENT_IDENTITY);

        PageInfo<NoticeDto> pageInfo = noticeService.getNoticesByPage(page, pageSize, searchConditionsDto);

        return getQueryByPageResult(pageInfo, "分页查询的公告不存在");
    }


    /**
     * 待完成作业
     */
    @RequestMapping(value = "/pendingStudentTask", method = RequestMethod.GET)
    public ActionResult<?> pendingStudentTask(HttpServletRequest request,
                                              @RequestParam(value = "page") int page,
                                              @RequestParam(value = "pageSize") int pageSize,
                                              SearchConditionsDto searchConditionsDto) {
        JWTPayloadDto JWTPayloadDto = validateCurrentUserIdentityAndGetPayloadInfoFromRequest(request, CommonConst.STUDENT_IDENTITY);

        PageInfo<StudentTaskDto> pageInfo = studentTaskService.getPendingStudentTasksByPage(page, pageSize, JWTPayloadDto.getUserId(), searchConditionsDto);

        return getQueryByPageResult(pageInfo, "分页查询的待完成作业不存在");
    }


    /**
     * 已完成作业
     */
    @RequestMapping(value = "/completedStudentTask", method = RequestMethod.GET)
    public ActionResult<?> completedStudentTask(HttpServletRequest request,
                                                @RequestParam(value = "page") int page,
                                                @RequestParam(value = "pageSize") int pageSize,
                                                SearchConditionsDto searchConditionsDto) {
        JWTPayloadDto JWTPayloadDto = validateCurrentUserIdentityAndGetPayloadInfoFromRequest(request, CommonConst.STUDENT_IDENTITY);

        PageInfo<StudentTaskDto> pageInfo = studentTaskService.getCompletedStudentTasksByPage(page, pageSize, JWTPayloadDto.getUserId(), searchConditionsDto);

        return getQueryByPageResult(pageInfo, "分页查询的已完成作业不存在");
    }


    /**
     * 历史作业
     */
    @RequestMapping(value = "/historicalStudentTask", method = RequestMethod.GET)
    public ActionResult<?> historicalStudentTask(HttpServletRequest request,
                                                 @RequestParam(value = "page") int page,
                                                 @RequestParam(value = "pageSize") int pageSize,
                                                 SearchConditionsDto searchConditionsDto) {
        JWTPayloadDto JWTPayloadDto = validateCurrentUserIdentityAndGetPayloadInfoFromRequest(request, CommonConst.STUDENT_IDENTITY);

        PageInfo<StudentTaskDto> pageInfo = studentTaskService.getHistoricalStudentTasksByPage(page, pageSize, JWTPayloadDto.getUserId(), searchConditionsDto);

        return getQueryByPageResult(pageInfo, "分页查询的历史作业不存在");
    }


    /**
     * 个人信息
     */
    @RequestMapping(value = "/personalInfo", method = RequestMethod.GET)
    public ActionResult<?> personalInfo(HttpServletRequest request) {
        JWTPayloadDto JWTPayloadDto = validateCurrentUserIdentityAndGetPayloadInfoFromRequest(request, CommonConst.STUDENT_IDENTITY);

        StudentDto studentDto = studentService.getStudentById(JWTPayloadDto.getUserId());

        return studentDto != null ? createSuccessActionResult(studentDto) : createFailActionResult("查询的学生不存在");
    }


    /**
     * 修改密码
     */
    @RequestMapping(value = "/password", method = RequestMethod.PUT)
    public ActionResult<?> modifyPassword(HttpServletRequest request, @RequestBody ModifyPasswordInfoDto modifyPasswordInfoDto) {
        JWTPayloadDto JWTPayloadDto = validateCurrentUserIdentityAndGetPayloadInfoFromRequest(request, CommonConst.STUDENT_IDENTITY);

        modifyPasswordInfoDto.setId(JWTPayloadDto.getUserId());
        modifyPasswordInfoDto.setModifier(JWTPayloadDto.getUsername());
        int updateId = studentService.modifyPassword(modifyPasswordInfoDto);
        logger.info("request: student/update , modifyPasswordInfoDto:{}, updateId:{}", JSONObject.toJSONString(modifyPasswordInfoDto), updateId);

        return updateId > 0 ? createSuccessActionResult("修改成功") : createFailActionResult("修改失败");
    }
}
