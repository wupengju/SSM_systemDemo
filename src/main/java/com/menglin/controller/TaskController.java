package com.menglin.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.menglin.common.ActionResult;
import com.menglin.common.CommonConst;
import com.menglin.dto.JWTPayloadDto;
import com.menglin.dto.SearchConditionsDto;
import com.menglin.dto.TaskDto;
import com.menglin.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/task")
@ResponseBody
public class TaskController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(TaskController.class);

    @Resource
    private TaskService taskService;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public ActionResult<?> list(
            HttpServletRequest request,
            @RequestParam(value = "page") int page,
            @RequestParam(value = "pageSize") int pageSize,
            SearchConditionsDto searchConditionsDto) {
        validateCurrentUserIdentity(request, CommonConst.ADMIN_IDENTITY);

        PageInfo<TaskDto> pageInfo = taskService.getTasksByPage(page, pageSize, searchConditionsDto);

        return getQueryByPageResult(pageInfo, "分页查询的作业不存在");
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public ActionResult<?> add(HttpServletRequest request, @RequestBody TaskDto taskDto) {
        JWTPayloadDto JWTPayloadDto = validateCurrentUserIdentityAndGetPayloadInfoFromRequest(request, CommonConst.ADMIN_IDENTITY);

        taskDto.setCreator(JWTPayloadDto.getUsername());
        int insertId = taskService.addTask(taskDto);
        logger.info("request: task/add , task:{}, insertId:{}", JSONObject.toJSONString(taskDto.convertToTask()), insertId);

        return insertId > 0 ? createSuccessActionResult("添加成功") : createFailActionResult("添加失败");
    }

    @RequestMapping(value = "", method = RequestMethod.PUT)
    public ActionResult<?> update(HttpServletRequest request, @RequestBody TaskDto taskDto) {
        JWTPayloadDto JWTPayloadDto = validateCurrentUserIdentityAndGetPayloadInfoFromRequest(request, CommonConst.ADMIN_IDENTITY);

        taskDto.setModifier(JWTPayloadDto.getUsername());
        int updateId = taskService.updateTask(taskDto);
        logger.info("request: task/update , task:{}, updateId:{}", JSONObject.toJSONString(taskDto.convertToTask()), updateId);

        return updateId > 0 ? createSuccessActionResult("修改成功") : createFailActionResult("修改失败");
    }

    @RequestMapping(value = "/{ids}", method = RequestMethod.DELETE)
    public ActionResult<?> delete(HttpServletRequest request, @PathVariable String ids) {
        validateCurrentUserIdentity(request, CommonConst.ADMIN_IDENTITY);

        taskService.batchDeleteTasksByIds(ids);
        logger.info("request: task/delete , ids:{}", ids);

        return createSuccessActionResult("删除成功");
    }

    @RequestMapping(value = "/id/{id}", method = RequestMethod.GET)
    public ActionResult<?> queryById(HttpServletRequest request, @PathVariable Long id) {
        validateCurrentUserIdentity(request, CommonConst.ADMIN_IDENTITY);

        TaskDto taskDto = taskService.getTaskById(id);

        return taskDto != null ? createSuccessActionResult(taskDto) : createFailActionResult("查询的作业不存在");
    }
}
