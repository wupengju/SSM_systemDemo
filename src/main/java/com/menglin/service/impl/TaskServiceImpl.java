package com.menglin.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.menglin.common.CommonConst;
import com.menglin.dao.CourseDao;
import com.menglin.dao.TaskDao;
import com.menglin.dao.TeacherDao;
import com.menglin.dto.SearchConditionsDto;
import com.menglin.dto.TaskDto;
import com.menglin.entity.Course;
import com.menglin.entity.Task;
import com.menglin.entity.Teacher;
import com.menglin.enums.ErrorStateEnum;
import com.menglin.exception.ServiceException;
import com.menglin.redis.RedisKeys;
import com.menglin.redis.RedisUtil;
import com.menglin.service.TaskService;
import com.menglin.util.DateUtil;
import com.menglin.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;

import static com.menglin.common.AssertArguments.*;

@Service("taskService")
public class TaskServiceImpl implements TaskService {
    private Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);

    @Resource
    private RedisUtil redisUtil;
    @Resource
    private TaskDao taskDao;
    @Resource
    private CourseDao courseDao;
    @Resource
    private TeacherDao teacherDao;
    @Resource
    private TaskDto taskDto;

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public int addTask(TaskDto taskDto) {
        checkNotNull(taskDto, "作业不能为空");
        checkNotEmpty(taskDto.getName(), "作业名不能为空");
        checkNotEmpty(taskDto.getDescription(), "作业描述不能为空");
        checkNotEmpty(taskDto.getCreator(), "作业创建者不能为空");
        checkGreaterThanZero(taskDto.getCourseId(), "课程 id 不能小于或等于零");
        checkGreaterThanZero(taskDto.getTeacherId(), "教师 id 不能小于或等于零");

        validateIsSameNameTask(taskDto.getName(), taskDto.getTeacherId());
        Task newTask = taskDto.convertToTask();
        newTask.setGmtCreate(new Date());
        int insertId;
        try {
            insertId = taskDao.insertSelective(newTask);
        } catch (Exception e) {
            logger.info("insert task fail, task:{}, e:{}", JSONObject.toJSONString(newTask), e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "插入作业记录失败");
        }
        if (insertId > 0) {
            logger.info("insert task success, save task to redis, task:{}", JSONObject.toJSONString(newTask));
            redisUtil.put(RedisKeys.TASK_CACHE_KEY + newTask.getId(), newTask);
            return insertId;
        }

        return 0;
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void deleteTaskById(Long id) {
        checkGreaterThanZero(id, "作业 id 不能小于或等于零");

        int deleteId;
        try {
            deleteId = taskDao.deleteByPrimaryKey(id);
        } catch (Exception e) {
            logger.info("delete task fail, id:{}, e:{}", id, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "删除作业记录失败");
        }
        if (deleteId > 0) {
            logger.info("delete task success, id:{}", deleteId);
            redisUtil.del(RedisKeys.TASK_CACHE_KEY + deleteId);
        } else {
            logger.info("delete task fail, id:{}", id);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "删除作业记录不存在");
        }
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void batchDeleteTasksByIds(String ids) {
        checkNotEmpty(ids, "ids 不能为空");

        String[] idsStr = ids.split(",");
        for (String anIdsStr : idsStr) {
            deleteTaskById(Long.parseLong(anIdsStr));
        }
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public int updateTask(TaskDto taskDto) {
        checkNotNull(taskDto, "作业不能为空");
        checkNotEmpty(taskDto.getModifier(), "作业修改者不能为空");
        checkGreaterThanZero(taskDto.getId(), "作业 id 不能小于或等于零");

        Task updateTask;
        int updateId;
        try {
            updateTask = taskDao.selectByPrimaryKey(taskDto.getId());
            updateTask(updateTask, taskDto);
            updateId = taskDao.updateByPrimaryKey(updateTask);
        } catch (Exception e) {
            logger.info("update task fail, task:{}, e:{}", JSONObject.toJSONString(taskDto.convertToTask()), e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "修改作业记录失败");
        }
        if (updateId > 0) {
            logger.info("updateTaskForTeacher success, delete task in redis and save new task, task:{}", JSONObject.toJSONString(updateTask));
            redisUtil.del(RedisKeys.TASK_CACHE_KEY + updateTask.getId());
            redisUtil.put(RedisKeys.TASK_CACHE_KEY + updateTask.getId(), updateTask);
            return updateId;
        }

        return 0;
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public int updateTaskUrlAndWriteTaskAttachment(Long taskId, String taskAttachmentUrl, String modifier, Boolean isDeleteAttachment) {
        checkNotNull(isDeleteAttachment, "是否删除作业附件不能为空");
        checkNotEmpty(taskAttachmentUrl, "作业附件地址不能为空");
        checkNotEmpty(modifier, "作业修改者不能为空");
        checkGreaterThanZero(taskId, "作业 id 不能小于或等于零");

        String taskUrl = isDeleteAttachment ? "" : taskAttachmentUrl;
        Task updateTask;
        int updateId;
        try {
            updateTask = taskDao.selectByPrimaryKey(taskId);
            if (CommonConst.TASK_PUBLISHED_STATUS.equals(updateTask.getStatus())) {
                throw new ServiceException(ErrorStateEnum.BUSINESS_ERROR.getState(), "已发布的作业不允许删除附件");
            }
            updateTask.setModifier(modifier);
            updateTask.setUrl(taskUrl);
            updateTask.setGmtModify(new Date());
            updateId = taskDao.updateByPrimaryKey(updateTask);
        } catch (ServiceException e) {
            logger.info("delete task attachment fail, taskId:{}, e:{}", taskId, e);
            throw new ServiceException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            logger.info("update taskUrl fail, taskId:{}, e:{}", taskId, e);
            if (!isDeleteAttachment) {
                FileUtil.deleteFile(taskAttachmentUrl);
            }
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "修改作业记录失败");
        }
        if (updateId > 0) {
            logger.info("updateTaskUrl success, delete task in redis and save new task, task:{}", JSONObject.toJSONString(updateTask));
            if (isDeleteAttachment) {
                FileUtil.deleteFile(taskAttachmentUrl);
            }
            redisUtil.del(RedisKeys.TASK_CACHE_KEY + updateTask.getId());
            redisUtil.put(RedisKeys.TASK_CACHE_KEY + updateTask.getId(), updateTask);
            return updateId;
        } else {
            if (!isDeleteAttachment) {
                FileUtil.deleteFile(taskAttachmentUrl);
            }
        }

        return 0;
    }

    @Override
    public TaskDto getTaskById(Long id) {
        checkGreaterThanZero(id, "作业 id 不能小于或等于零");

        logger.info("get task by id:{}", id);
        Task task = (Task) redisUtil.get(RedisKeys.TASK_CACHE_KEY + id, Task.class);
        if (task != null) {
            logger.info("task in redis, task:{}", task);
            return setTaskDtoGmtCreateAndGmtModify(setTaskDtoNameInfo(task), task);
        }
        Task taskFromMysql;
        try {
            taskFromMysql = taskDao.selectByPrimaryKey(id);
        } catch (Exception e) {
            logger.info("get task by primary key fail, id:{}, e:{}", id, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据 id 查询作业记录失败");
        }
        if (taskFromMysql != null) {
            logger.info("get task from mysql and save task to redis, task:{}", JSONObject.toJSONString(taskFromMysql));
            redisUtil.put(RedisKeys.TASK_CACHE_KEY + taskFromMysql.getId(), taskFromMysql);
            return setTaskDtoGmtCreateAndGmtModify(setTaskDtoNameInfo(taskFromMysql), taskFromMysql);
        }

        return null;
    }

    @Override
    public PageInfo<TaskDto> getTasksByPageAndTeacherId(int start, int pageSize, Long teacherId, SearchConditionsDto searchConditionsDto) {
        checkNotNull(searchConditionsDto, "分页查询的条件对象不能为空");
        checkGreaterThanOrIsZero(start, "分页查询的开始数必须大于零");
        checkGreaterThanZero(pageSize, "分页查询的单页总数必须大于零");

        Map<String, Object> map = new HashMap<>();
        Page page = PageHelper.startPage(start, pageSize);
        if (teacherId > 0) {
            map.put("teacherId", teacherId);
        }
        if (!StringUtils.isEmpty(searchConditionsDto.getTaskName())) {
            map.put("taskName", searchConditionsDto.getTaskName());
        }
        if (!StringUtils.isEmpty(searchConditionsDto.getCourseName())) {
            map.put("courseName", searchConditionsDto.getCourseName());
        }
        List<Task> tasksList;
        try {
            tasksList = taskDao.queryTasksByPage(map);
        } catch (Exception e) {
            logger.info("queryTasksByPage fail, map:{}, e:{}", map, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据条件分页查询作业记录失败");
        }
        if (tasksList != null) {
            List<TaskDto> taskDtosList = new Page<>(start, pageSize, true);
            Page taskDtosListPage = (Page) taskDtosList;
            taskDtosListPage.setTotal(page.getTotal());
            taskDtosList = queryTaskDtoList(taskDtosList, tasksList);
            return new PageInfo<>(taskDtosList);
        }

        return null;
    }

    @Override
    public PageInfo<TaskDto> getTasksByPage(int start, int pageSize, SearchConditionsDto searchConditionsDto) {
        return this.getTasksByPageAndTeacherId(start, pageSize, 0L, searchConditionsDto);
    }

    private void validateIsSameNameTask(String name, Long teacherId) {
        List<Task> taskList;
        try {
            taskList = taskDao.selectByNameAndTeacherId(name, teacherId);
        } catch (Exception e) {
            logger.info("selectByNameAndTeacherId fail, name:{}, teacherId:{}, e:{}", name, teacherId, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据作业名查询作业记录失败");
        }
        if (!taskList.isEmpty()) {
            throw new ServiceException(ErrorStateEnum.DB_EXIST_SAME_RECORD.getState(), "添加的作业名为 " + name + " 的作业记录已存在");
        }
    }

    private void updateTask(Task updateTask, TaskDto taskDto) {
        if (!StringUtils.isEmpty(taskDto.getUrl())) {
            updateTask.setUrl(taskDto.getUrl());
        }
        if (!StringUtils.isEmpty(taskDto.getName())) {
            updateTask.setName(taskDto.getName());
        }
        if (!StringUtils.isEmpty(taskDto.getDescription())) {
            updateTask.setDescription(taskDto.getDescription());
        }
        if (taskDto.getCourseId() > 0) {
            updateTask.setCourseId(taskDto.getCourseId());
        }
        if (taskDto.getTeacherId() > 0) {
            updateTask.setTeacherId(taskDto.getTeacherId());
        }
        updateTask.setModifier(taskDto.getModifier());
        updateTask.setGmtModify(new Date());
    }

    private TaskDto setTaskDtoNameInfo(Task task) {
        TaskDto queryTaskDto = taskDto.convertFor(task);
        Course course = courseDao.selectByPrimaryKey(task.getCourseId());
        queryTaskDto.setCourseName(course.getName());
        Teacher teacher = teacherDao.selectByPrimaryKey(task.getTeacherId());
        queryTaskDto.setTeacherName(teacher.getName());
        return queryTaskDto;
    }

    private TaskDto setTaskDtoGmtCreateAndGmtModify(TaskDto newTaskDto, Task task) {
        newTaskDto.setGmtCreate(DateUtil.formatDate(task.getGmtCreate()));
        newTaskDto.setGmtModify(DateUtil.formatDate(task.getGmtModify()));
        return newTaskDto;
    }

    private String getCourseName(Long id, List<Course> courseList) {
        String name = "";
        for (Course course : courseList) {
            if (course.getId() == id) {
                name = course.getName();
                break;
            }
        }

        return name;
    }

    private String getTeacherName(Long id, List<Teacher> teacherList) {
        String name = "";
        for (Teacher teacher : teacherList) {
            if (teacher.getId() == id) {
                name = teacher.getName();
                break;
            }
        }

        return name;
    }

    private List<TaskDto> queryTaskDtoList(List<TaskDto> taskDtosList, List<Task> tasksList) {
        List<Long> courseIdList = new ArrayList<>();
        List<Long> teacherIdList = new ArrayList<>();
        for (Task task : tasksList) {
            courseIdList.add(task.getCourseId());
            teacherIdList.add(task.getTeacherId());
            taskDtosList.add(setTaskDtoGmtCreateAndGmtModify(taskDto.convertFor(task), task));
        }
        Map<String, Object> connectedQueryMap = new HashMap<>();
        connectedQueryMap.put("courseIdList", courseIdList);
        connectedQueryMap.put("teacherIdList", teacherIdList);
        List<Course> courseList = courseDao.queryCoursesByIdList(connectedQueryMap);
        List<Teacher> teacherList = teacherDao.queryTeachersByIdList(connectedQueryMap);
        for (TaskDto curTaskDto : taskDtosList) {
            curTaskDto.setCourseName(getCourseName(curTaskDto.getCourseId(), courseList));
            curTaskDto.setTeacherName(getTeacherName(curTaskDto.getTeacherId(), teacherList));
        }
        return taskDtosList;
    }
}
