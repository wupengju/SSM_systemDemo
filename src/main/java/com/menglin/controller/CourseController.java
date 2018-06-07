package com.menglin.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.menglin.common.ActionResult;
import com.menglin.common.CommonConst;
import com.menglin.dto.CourseDto;
import com.menglin.dto.JWTPayloadDto;
import com.menglin.dto.SearchConditionsDto;
import com.menglin.service.CourseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/course")
@ResponseBody
public class CourseController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(CourseController.class);

    @Resource
    private CourseService courseService;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public ActionResult<?> list(
            HttpServletRequest request,
            @RequestParam(value = "page") int page,
            @RequestParam(value = "pageSize") int pageSize,
            SearchConditionsDto searchConditionsDto) {
        validateCurrentUserIdentity(request, CommonConst.ADMIN_IDENTITY);

        PageInfo<CourseDto> pageInfo = courseService.getCoursesByPage(page, pageSize, searchConditionsDto);

        return getQueryByPageResult(pageInfo, "分页查询的课程不存在");
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public ActionResult<?> add(HttpServletRequest request, @RequestBody CourseDto courseDto) {
        JWTPayloadDto JWTPayloadDto = validateCurrentUserIdentityAndGetPayloadInfoFromRequest(request, CommonConst.ADMIN_IDENTITY);

        courseDto.setCreator(JWTPayloadDto.getUsername());
        int insertId = courseService.addCourse(courseDto);
        logger.info("request: course/add , course:{}, insertId:{}", JSONObject.toJSONString(courseDto.convertToCourse()), insertId);

        return insertId > 0 ? createSuccessActionResult("添加成功") : createFailActionResult("添加失败");
    }

    @RequestMapping(value = "", method = RequestMethod.PUT)
    public ActionResult<?> update(HttpServletRequest request, @RequestBody CourseDto courseDto) {
        JWTPayloadDto JWTPayloadDto = validateCurrentUserIdentityAndGetPayloadInfoFromRequest(request, CommonConst.ADMIN_IDENTITY);

        courseDto.setModifier(JWTPayloadDto.getUsername());
        int updateId = courseService.updateCourse(courseDto);
        logger.info("request: course/update , course:{}, updateId:{}", JSONObject.toJSONString(courseDto.convertToCourse()), updateId);

        return updateId > 0 ? createSuccessActionResult("修改成功") : createFailActionResult("修改失败");
    }

    @RequestMapping(value = "/{ids}", method = RequestMethod.DELETE)
    public ActionResult<?> delete(HttpServletRequest request, @PathVariable String ids) {
        validateCurrentUserIdentity(request, CommonConst.ADMIN_IDENTITY);

        courseService.batchDeleteCoursesByIds(ids);
        logger.info("request: course/delete , ids:{}", ids);

        return createSuccessActionResult("删除成功");
    }

    @RequestMapping(value = "/id/{id}", method = RequestMethod.GET)
    public ActionResult<?> queryById(HttpServletRequest request, @PathVariable Long id) {
        validateCurrentUserIdentity(request, CommonConst.ADMIN_IDENTITY);

        CourseDto courseDto = courseService.getCourseById(id);

        return courseDto != null ? createSuccessActionResult(courseDto) : createFailActionResult("查询的课程不存在");
    }
}