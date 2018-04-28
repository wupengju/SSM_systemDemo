package com.menglin.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.menglin.dao.TaskDao;
import com.menglin.entity.Task;
import com.menglin.redis.RedisKeys;
import com.menglin.redis.RedisUtil;
import com.menglin.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.menglin.common.AssertArguments.*;

@Service("taskService")
public class TaskServiceImpl implements TaskService {
    private Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);

    @Resource
    private TaskDao taskDao;
    @Resource
    private RedisUtil redisUtil;

    @Override
    public PageInfo<Task> getTasksByPage(int start, int pageSize, Task task) {
        checkGreaterThanZero(start, "分页查询作业的开始数必须大于零");
        checkGreaterThanZero(pageSize, "分页查询作业的单页总数必须大于零");
        Map<String, Object> map = new HashMap<String, Object>();
        PageHelper.startPage(start, pageSize);
        if (task != null && !"".equals(task.getName())) {
            map.put("name", task.getName());
        }
        List<Task> taskList = taskDao.queryTasksByPage(map);
        return new PageInfo<>(taskList);
    }

    @Override
    public Long getTotalTask() {
        return taskDao.queryTotalTask();
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public int addTask(Task task) {
        checkNotNull(task, "作业不能为空");
        checkNotEmpty(task.getName(), "作业名不能为空");
        checkNotEmpty(task.getAuthor(), "作者不能为空");

        int insertId = taskDao.insert(task);
        if (insertId > 0) {
            logger.info("insert task success, save task to redis, task:{}", task);
            redisUtil.put(RedisKeys.Task_CACHE_KEY + insertId, task);
            return 1;
        }

        return 0;
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public int updateTask(Task task) {
        checkNotNull(task, "作业不能为空");
        checkNotEmpty(task.getName(), "作业名不能为空");
        checkNotEmpty(task.getAuthor(), "作者不能为空");

        int updateId = taskDao.updateByPrimaryKey(task);
        if (updateId > 0) {
            logger.info("update task success, delete task in redis and save again, task:{}", task);
            redisUtil.del(RedisKeys.Task_CACHE_KEY + updateId);
            redisUtil.put(RedisKeys.Task_CACHE_KEY + updateId, task);
            return 1;
        }

        return 0;
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public int deleteTaskById(Long id) {
        checkGreaterThanZero(id, "作业 id 不能小于或等于零");

        redisUtil.del(RedisKeys.Task_CACHE_KEY + id);

        return taskDao.deleteByPrimaryKey(id);
    }

    @Override
    public Task getById(Long id) {
        checkGreaterThanZero(id, "作业 id 不能小于或等于零");

        logger.info("get task by id:{}", id);
        Task task = (Task) redisUtil.get(RedisKeys.Task_CACHE_KEY + id, Task.class);
        if (task != null) {
            logger.info("task in redis, task:{}", task);
            return task;
        }

        Task taskFromMysql = taskDao.selectByPrimaryKey(id);
        if (taskFromMysql != null) {
            logger.info("get task from mysql and save task to redis, task:{}", task);
            redisUtil.put(RedisKeys.Task_CACHE_KEY + taskFromMysql.getId(), taskFromMysql);
            return taskFromMysql;
        }

        return null;
    }
}
