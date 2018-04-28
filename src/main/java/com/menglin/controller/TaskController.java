package com.menglin.controller;

import com.github.pagehelper.PageInfo;
import com.menglin.common.ActionResult;
import com.menglin.entity.Task;
import com.menglin.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

import static com.menglin.common.AssertArguments.checkNotNull;

@Controller
@RequestMapping("/tasks")
@ResponseBody
public class TaskController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(TaskController.class);

    @Resource
    private TaskService taskService;


    /*
     * 分页查询
     * */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ActionResult<?> list(
            @RequestParam(value = "page", required = false) String page,
            @RequestParam(value = "pageSize", required = false) String pageSize,
            Task task) {
        PageInfo<Task> pageInfo = taskService.getTasksByPage(Integer.parseInt(page), Integer.parseInt(pageSize), task);

        Map<String, Object> data = new HashMap<>();
        data.put("rows", pageInfo.getList());
        data.put("totalNum", pageInfo.getTotal());
        data.put("totalPages", pageInfo.getPages());

        return createSuccessActionResult(data);
    }


    /*
     * 添加
     * */
    @RequestMapping(value = "", method = RequestMethod.POST)
    public ActionResult<?> add(@RequestBody Task task) {

        int resultTotal = taskService.addTask(task);

        logger.info("request: tasks/add , task:{}", task.toString());
        return resultTotal > 0 ? createSuccessActionResult("添加成功") : createFailActionResult("添加失败");
    }


    /*
     * 修改
     * */
    @RequestMapping(value = "", method = RequestMethod.PUT)
    public ActionResult<?> update(@RequestBody Task task) {
        int resultTotal = taskService.updateTask(task);

        logger.info("request: tasks/update , task:{}", task.toString());
        return resultTotal > 0 ? createSuccessActionResult("修改成功") : createFailActionResult("修改失败");
    }


    /*
     * 删除
     * */
    @RequestMapping(value = "/{ids}", method = RequestMethod.DELETE)
    public ActionResult<?> delete(@PathVariable String ids) {
        checkNotNull(ids, "ids 不能为空");

        String[] idsStr = ids.split(",");
        for (int i = 0; i < idsStr.length; i++) {
            taskService.deleteTaskById(Long.parseLong(idsStr[i]));
        }

        logger.info("request: tasks/delete , ids:{}", ids);
        return createSuccessActionResult(null);
    }


    /*
     * 根据 ID 查询
     * */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public ActionResult<?> queryById(@PathVariable String id) {
        checkNotNull(id, "id 不能为空");

        Task task = taskService.getById(Long.parseLong(id));

        return createSuccessActionResult(task);
    }

}
