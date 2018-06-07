package com.menglin.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.menglin.common.CommonConst;
import com.menglin.dao.*;
import com.menglin.dto.SearchConditionsDto;
import com.menglin.dto.StudentTaskDto;
import com.menglin.dto.TaskDto;
import com.menglin.entity.*;
import com.menglin.enums.ErrorStateEnum;
import com.menglin.exception.ServiceException;
import com.menglin.service.StudentTaskService;
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

@Service("studentTaskService")
public class StudentTaskServiceImpl implements StudentTaskService {
    private Logger logger = LoggerFactory.getLogger(StudentTaskServiceImpl.class);

    @Resource
    private TaskService taskService;
    @Resource
    private StudentTaskDao studentTaskDao;
    @Resource
    private StudentDao studentDao;
    @Resource
    private TaskDao taskDao;
    @Resource
    private CourseDao courseDao;
    @Resource
    private TeacherDao teacherDao;
    @Resource
    private StudentTaskDto studentTaskDto;
    @Resource
    private TaskDto taskDto;

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public int addStudentTask(StudentTaskDto studentTaskDto) {
        checkNotNull(studentTaskDto, "学生作业不能为空");
        checkNotEmpty(studentTaskDto.getCreator(), "学生作业创建者不能为空");
        checkGreaterThanZero(studentTaskDto.getStudentId(), "学生 id 不能小于或等于零");
        checkGreaterThanZero(studentTaskDto.getTaskId(), "作业 id 不能小于或等于零");

        validateIsSameNameStudentTask(studentTaskDto.getStudentId(), studentTaskDto.getTaskId());
        StudentTask newStudentTask = studentTaskDto.convertToStudentTask();
        newStudentTask.setGmtCreate(new Date());
        int insertId;
        try {
            insertId = studentTaskDao.insert(newStudentTask);
        } catch (Exception e) {
            logger.info("insert studentTask fail, studentTask:{}, e:{}", JSONObject.toJSONString(newStudentTask), e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "插入学生作业记录失败");
        }
        if (insertId > 0) {
            logger.info("insert studentTask success, save studentTask to redis, studentTask:{}", JSONObject.toJSONString(newStudentTask));
            return insertId;
        }

        return 0;
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void deleteStudentTaskById(Long id) {
        checkGreaterThanZero(id, "学生作业 id 不能小于或等于零");

        int deleteId;
        try {
            deleteId = studentTaskDao.deleteByPrimaryKey(id);
        } catch (Exception e) {
            logger.info("delete studentTask fail, id:{}, e:{}", id, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "删除学生作业记录失败");
        }
        if (deleteId > 0) {
            logger.info("delete studentTask success, id:{}", deleteId);
        } else {
            logger.info("delete studentTask fail, id:{}", id);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "删除学生作业记录不存在");
        }
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public int updateStudentTask(StudentTaskDto studentTaskDto, String identity) {
        checkNotNull(studentTaskDto, "学生作业不能为空");
        checkNotEmpty(studentTaskDto.getModifier(), "学生作业修改者不能为空");
        checkGreaterThanZero(studentTaskDto.getId(), "学生作业 id 不能小于或等于零");

        if (CommonConst.STUDENT_IDENTITY.equals(identity)) {
            if (StringUtils.isEmpty(studentTaskDto.getContent()) && StringUtils.isEmpty(studentTaskDto.getAnswerUrl())) {
                throw new ServiceException("学生作业答案内容不能为空");
            }
        }

        if (CommonConst.TEACHER_IDENTITY.equals(identity)) {
            checkNotEmpty(studentTaskDto.getScore(), "学生作业分数不能为空");
            checkNotEmpty(studentTaskDto.getComments(), "学生作业教师评语不能为空");
        }

        StudentTask updateStudentTask;
        int updateId;
        try {
            updateStudentTask = studentTaskDao.selectByPrimaryKey(studentTaskDto.getId());
            updateStudentTask(updateStudentTask, studentTaskDto, identity);
            updateId = studentTaskDao.updateByPrimaryKey(updateStudentTask);
        } catch (ServiceException e) {
            logger.info("update studentTask fail, studentTask:{}, e:{}", JSONObject.toJSONString(studentTaskDto.convertToStudentTask()), e);
            throw new ServiceException(ErrorStateEnum.BUSINESS_ERROR.getState(), e.getMessage());
        } catch (Exception e) {
            logger.info("update studentTask fail, studentTask:{}, e:{}", JSONObject.toJSONString(studentTaskDto.convertToStudentTask()), e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "修改学生作业记录失败");
        }
        if (updateId > 0) {
            logger.info("updateStudentTaskForTeacher success, studentTask:{}", JSONObject.toJSONString(updateStudentTask));
            return updateId;
        }

        return 0;
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public int updateStudentTaskUrlAndWriteTaskAttachment(Long studentTaskId, String taskAnswerAttachmentUrl, String modifier, Boolean isDeleteAnswerAttachment) {
        checkNotNull(isDeleteAnswerAttachment, "是否删除作业答案附件不能为空");
        checkNotEmpty(taskAnswerAttachmentUrl, "学生作业答案附件地址不能为空");
        checkNotEmpty(modifier, "学生作业修改者不能为空");
        checkGreaterThanZero(studentTaskId, "学生作业 id 不能小于或等于零");

        String studentTaskUrl = isDeleteAnswerAttachment ? "" : taskAnswerAttachmentUrl;
        StudentTask updateStudentTask;
        int updateId;
        try {
            updateStudentTask = studentTaskDao.selectByPrimaryKey(studentTaskId);
            updateStudentTask.setUrl(studentTaskUrl);
            updateStudentTask.setModifier(modifier);
            updateStudentTask.setGmtModify(new Date());
            updateId = studentTaskDao.updateByPrimaryKey(updateStudentTask);
        } catch (Exception e) {
            logger.info("update studentTask fail, studentTaskId:{}, e:{}", studentTaskId, e);
            if (!isDeleteAnswerAttachment) {
                FileUtil.deleteFile(taskAnswerAttachmentUrl);
            }
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "修改学生作业记录失败");
        }
        if (updateId > 0) {
            logger.info("updateStudentTaskUrl success, studentTask:{}", JSONObject.toJSONString(updateStudentTask));
            if (isDeleteAnswerAttachment) {
                FileUtil.deleteFile(taskAnswerAttachmentUrl);
            }
            return updateId;
        } else {
            if (!isDeleteAnswerAttachment) {
                FileUtil.deleteFile(taskAnswerAttachmentUrl);
            }
        }

        return 0;
    }

    @Override
    public StudentTaskDto getStudentTaskById(Long id) {
        checkGreaterThanZero(id, "学生作业 id 不能小于或等于零");

        logger.info("get studentTask by id:{}", id);
        StudentTask studentTaskFromMysql;
        try {
            studentTaskFromMysql = studentTaskDao.selectByPrimaryKey(id);
        } catch (Exception e) {
            logger.info("get studentTask by primary key fail, id:{}, e:{}", id, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据 id 查询学生作业记录失败");
        }
        if (studentTaskFromMysql != null) {
            logger.info("get studentTask from mysql, studentTask:{}", JSONObject.toJSONString(studentTaskFromMysql));
            return setStudentTaskDtoGmtCreateAndGmtModify(setStudentTaskDtoNameInfo(studentTaskFromMysql), studentTaskFromMysql);
        }

        return null;
    }

    @Override
    public StudentTaskDto getStudentTaskByStudentIdAndTaskId(Long studentId, Long taskId) {
        checkGreaterThanZero(studentId, "学生 id 不能小于或等于零");
        checkGreaterThanZero(taskId, "作业 id 不能小于或等于零");

        logger.info("get studentTask by studentId and taskId, studentId:{}, taskId:{}", studentId, taskId);
        StudentTask studentTaskFromMysql;
        try {
            studentTaskFromMysql = studentTaskDao.selectByStudentIdAndTaskId(studentId, taskId);
        } catch (Exception e) {
            logger.info("get studentTask by studentId and taskId fail, studentId:{}, taskId:{}, e:{}", studentId, taskId, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据学生ID和作业ID查询学生作业记录失败");
        }
        if (studentTaskFromMysql != null) {
            logger.info("get studentTask from mysql, studentTask:{}", JSONObject.toJSONString(studentTaskFromMysql));
            return setStudentTaskDtoGmtCreateAndGmtModify(setStudentTaskDtoNameInfo(studentTaskFromMysql), studentTaskFromMysql);
        }

        return null;
    }

    @Override
    public PageInfo<StudentTaskDto> getStudentTasksByPage(int start, int pageSize, SearchConditionsDto searchConditionsDto) {
        checkNotNull(searchConditionsDto, "分页查询的条件对象不能为空");
        checkGreaterThanOrIsZero(start, "分页查询的开始数必须大于零");
        checkGreaterThanZero(pageSize, "分页查询的单页总数必须大于零");

        Map<String, Object> map = new HashMap<>();
        addSearchStudentTaskInfo(map, searchConditionsDto);
        Page page = PageHelper.startPage(start, pageSize);
        List<StudentTaskAllInfo> studentTaskAllInfoList;
        try {
            studentTaskAllInfoList = studentTaskDao.queryStudentTasksByPage(map);
        } catch (Exception e) {
            logger.info("getStudentTasksByPage fail, map:{}, e:{}", map, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据条件分页查询学生作业记录失败");
        }
        if (studentTaskAllInfoList != null) {
            return new PageInfo<>(queryStudentTaskAllInfoList(page, studentTaskAllInfoList));
        }

        return null;
    }

    @Override
    public PageInfo<StudentTaskDto> getPendingStudentTasksByPage(int start, int pageSize, Long studentId, SearchConditionsDto searchConditionsDto) {
        checkGreaterThanOrIsZero(start, "分页查询的开始数必须大于零");
        checkGreaterThanZero(pageSize, "分页查询的单页总数必须大于零");
        checkGreaterThanZero(studentId, "学生 id 不能小于或等于零");

        Map<String, Object> map = new HashMap<>();
        map.put("studentId", studentId);
        addSearchStudentTaskInfo(map, searchConditionsDto);
        Page page = PageHelper.startPage(start, pageSize);
        List<StudentTaskAllInfo> studentTaskAllInfoList;
        try {
            studentTaskAllInfoList = studentTaskDao.queryPendingStudentTasksByPage(map);
        } catch (Exception e) {
            logger.info("getPendingStudentTasksByPage fail, map:{}, e:{}", map, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据条件分页查询学生待完成作业记录失败");
        }
        if (studentTaskAllInfoList != null) {
            return new PageInfo<>(queryStudentTaskAllInfoList(page, studentTaskAllInfoList));
        }

        return null;
    }

    @Override
    public PageInfo<StudentTaskDto> getCompletedStudentTasksByPage(int start, int pageSize, Long studentId, SearchConditionsDto searchConditionsDto) {
        checkGreaterThanOrIsZero(start, "分页查询的开始数必须大于零");
        checkGreaterThanZero(pageSize, "分页查询的单页总数必须大于零");
        checkGreaterThanZero(studentId, "学生 id 不能小于或等于零");

        Map<String, Object> map = new HashMap<>();
        map.put("studentId", studentId);
        addSearchStudentTaskInfo(map, searchConditionsDto);
        Page page = PageHelper.startPage(start, pageSize);
        List<StudentTaskAllInfo> studentTaskAllInfoList;
        try {
            studentTaskAllInfoList = studentTaskDao.queryCompletedStudentTasksByPage(map);
        } catch (Exception e) {
            logger.info("getCompletedStudentTasksByPage fail, map:{}, e:{}", map, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据条件分页查询学生已完成作业记录失败");
        }
        if (studentTaskAllInfoList != null) {
            return new PageInfo<>(queryStudentTaskAllInfoList(page, studentTaskAllInfoList));
        }

        return null;
    }

    @Override
    public PageInfo<StudentTaskDto> getHistoricalStudentTasksByPage(int start, int pageSize, Long studentId, SearchConditionsDto searchConditionsDto) {
        checkGreaterThanOrIsZero(start, "分页查询的开始数必须大于零");
        checkGreaterThanZero(pageSize, "分页查询的单页总数必须大于零");
        checkGreaterThanZero(studentId, "学生 id 不能小于或等于零");

        Map<String, Object> map = new HashMap<>();
        map.put("studentId", studentId);
        addSearchStudentTaskInfo(map, searchConditionsDto);
        Page page = PageHelper.startPage(start, pageSize);
        List<StudentTaskAllInfo> studentTaskAllInfoList;
        try {
            studentTaskAllInfoList = studentTaskDao.queryHistoricalStudentTasksByPage(map);
        } catch (Exception e) {
            logger.info("getHistoricalStudentTasksByPage fail, map:{}, e:{}", map, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据条件分页查询学生历史作业记录失败");
        }
        if (studentTaskAllInfoList != null) {
            return new PageInfo<>(queryStudentTaskAllInfoList(page, studentTaskAllInfoList));
        }

        return null;
    }

    @Override
    public PageInfo<StudentTaskDto> getCorrectingStudentTasksByPage(int start, int pageSize, Long teacherId, SearchConditionsDto searchConditionsDto) {
        checkGreaterThanOrIsZero(start, "分页查询的开始数必须大于零");
        checkGreaterThanZero(pageSize, "分页查询的单页总数必须大于零");
        checkGreaterThanZero(teacherId, "教师 id 不能小于或等于零");

        Map<String, Object> map = new HashMap<>();
        map.put("teacherId", teacherId);
        addSearchStudentTaskInfo(map, searchConditionsDto);
        Page page = PageHelper.startPage(start, pageSize);
        List<StudentTaskAllInfo> studentTaskAllInfoList;
        try {
            studentTaskAllInfoList = studentTaskDao.queryCorrectingStudentTasksByPage(map);
        } catch (Exception e) {
            logger.info("queryCorrectingStudentTasksByPage fail, map:{}, e:{}", map, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据条件分页查询待批改作业记录失败");
        }
        if (studentTaskAllInfoList != null) {
            return new PageInfo<>(queryStudentTaskAllInfoList(page, studentTaskAllInfoList));
        }

        return null;
    }

    private void validateIsSameNameStudentTask(Long studentId, Long taskId) {
        StudentTask studentTask;
        try {
            studentTask = studentTaskDao.selectByStudentIdAndTaskId(studentId, taskId);
        } catch (Exception e) {
            logger.info("selectByNameAndTaskId fail, studentId:{}, taskId:{}, e:{}", studentId, taskId, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据学生作业名查询学生作业记录失败");
        }
        if (studentTask != null) {
            throw new ServiceException(ErrorStateEnum.DB_EXIST_SAME_RECORD.getState(), "添加的学生作业记录已存在");
        }
    }

    private void updateStudentTask(StudentTask updateStudentTask, StudentTaskDto studentTaskDto, String identity) {
        if (CommonConst.STUDENT_IDENTITY.equals(identity)) {
            if (!StringUtils.isEmpty(studentTaskDto.getContent())) {
                updateStudentTask.setContent(studentTaskDto.getContent());
            }
            if (!StringUtils.isEmpty(studentTaskDto.getAnswerUrl())) {
                updateStudentTask.setUrl(studentTaskDto.getAnswerUrl());
            }
        } else if (CommonConst.TEACHER_IDENTITY.equals(identity)) {
            if (StringUtils.isEmpty(updateStudentTask.getContent()) && StringUtils.isEmpty(updateStudentTask.getUrl())) {
                throw new ServiceException("批改的作业的答案不能为空");
            }
            updateStudentTask.setScore(studentTaskDto.getScore());
            updateStudentTask.setComments(studentTaskDto.getComments());
        }
        updateStudentTask.setModifier(studentTaskDto.getModifier());
        updateStudentTask.setGmtModify(new Date());
    }

    private void addSearchStudentTaskInfo(Map<String, Object> map, SearchConditionsDto searchConditionsDto) {
        if (!StringUtils.isEmpty(searchConditionsDto.getStudentName())) {
            map.put("studentName", searchConditionsDto.getStudentName());
        }
        if (!StringUtils.isEmpty(searchConditionsDto.getStudentUsername())) {
            map.put("studentUsername", searchConditionsDto.getStudentUsername());
        }
        if (!StringUtils.isEmpty(searchConditionsDto.getClassTeamName())) {
            map.put("classTeamName", searchConditionsDto.getClassTeamName());
        }
        if (!StringUtils.isEmpty(searchConditionsDto.getTaskName())) {
            map.put("taskName", searchConditionsDto.getTaskName());
        }
        if (!StringUtils.isEmpty(searchConditionsDto.getCourseName())) {
            map.put("courseName", searchConditionsDto.getCourseName());
        }
        if (!StringUtils.isEmpty(searchConditionsDto.getTeacherName())) {
            map.put("teacherName", searchConditionsDto.getTeacherName());
        }
    }

    private StudentTaskDto setStudentTaskDtoNameInfo(StudentTask studentTask) {
        StudentTaskDto queryStudentTaskDto = studentTaskDto.convertFor(studentTask);
        Student student = studentDao.selectByPrimaryKey(studentTask.getStudentId());
        queryStudentTaskDto.setStudentUsername(student.getUsername());
        queryStudentTaskDto.setStudentName(student.getName());
        TaskDto taskDto = taskService.getTaskById(studentTask.getTaskId());
        queryStudentTaskDto.setTaskUrl(taskDto.getUrl());
        queryStudentTaskDto.setTaskName(taskDto.getName());
        queryStudentTaskDto.setTaskDescription(taskDto.getDescription());
        queryStudentTaskDto.setCourseName(taskDto.getCourseName());
        queryStudentTaskDto.setTeacherName(taskDto.getTeacherName());
        return queryStudentTaskDto;
    }

    private StudentTaskDto setStudentTaskDtoGmtCreateAndGmtModify(StudentTaskDto newStudentTaskDto, StudentTask studentTask) {
        newStudentTaskDto.setGmtCreate(DateUtil.formatDate(studentTask.getGmtCreate()));
        newStudentTaskDto.setGmtModify(DateUtil.formatDate(studentTask.getGmtModify()));
        return newStudentTaskDto;
    }

    private StudentTaskDto setStudentTaskDtoGmtCreateAndGmtModify(StudentTaskDto newStudentTaskDto, StudentTaskAllInfo studentTaskAllInfo) {
        newStudentTaskDto.setGmtCreate(DateUtil.formatDate(studentTaskAllInfo.getGmtCreate()));
        newStudentTaskDto.setGmtModify(DateUtil.formatDate(studentTaskAllInfo.getGmtModify()));
        return newStudentTaskDto;
    }

    private List<StudentTaskDto> queryStudentTaskAllInfoList(Page page, List<StudentTaskAllInfo> studentTaskAllInfoList) {
        List<StudentTaskDto> studentTaskDtosList = new Page<>(page.getPageNum(), page.getPageSize(), true);
        Page studentTaskDtosListPage = (Page) studentTaskDtosList;
        studentTaskDtosListPage.setTotal(page.getTotal());
        for (StudentTaskAllInfo studentTaskAllInfo : studentTaskAllInfoList) {
            studentTaskDtosList.add(setStudentTaskDtoGmtCreateAndGmtModify(studentTaskDto.convertFor(studentTaskAllInfo), studentTaskAllInfo));
        }
        return studentTaskDtosList;
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

    private Student getStudent(Long id, List<Student> studentList) {
        Student student = null;
        for (Student curStudent : studentList) {
            if (curStudent.getId() == id) {
                student = curStudent;
            }
        }

        return student;
    }

    private TaskDto getTaskDto(Long id, List<TaskDto> taskDtoList) {
        TaskDto taskDto = null;
        for (TaskDto curTaskDto : taskDtoList) {
            if (curTaskDto.getId() == id) {
                taskDto = curTaskDto;
            }
        }

        return taskDto;
    }

    private List<StudentTaskDto> queryStudentTaskDtoList(Page page, List<StudentTask> StudentTasksList, SearchConditionsDto searchConditionsDto) {
        List<StudentTaskDto> studentTaskDtosList = new Page<>(page.getPageNum(), page.getPageSize(), true);
        Page studentTaskDtosListPage = (Page) studentTaskDtosList;
        studentTaskDtosListPage.setTotal(page.getTotal());
        if (page.getTotal() == 0) {
            return studentTaskDtosList;
        }
        List<Long> studentIdList = new ArrayList<>();
        List<Long> taskIdList = new ArrayList<>();
        for (StudentTask studentTask : StudentTasksList) {
            studentIdList.add(studentTask.getStudentId());
            taskIdList.add(studentTask.getTaskId());
            StudentTaskDto newStudentTaskDto = studentTaskDto.convertFor(studentTask);
            newStudentTaskDto.setAnswerUrl(studentTask.getUrl());
            studentTaskDtosList.add(setStudentTaskDtoGmtCreateAndGmtModify(newStudentTaskDto, studentTask));
        }
        Map<String, Object> connectedQueryMap = new HashMap<>();
        connectedQueryMap.put("studentIdList", studentIdList);
        connectedQueryMap.put("taskIdList", taskIdList);
        if (!StringUtils.isEmpty(searchConditionsDto.getTaskName())) {
            connectedQueryMap.put("taskName", searchConditionsDto.getTaskName());
        }
        List<Student> studentList = studentDao.queryStudentsByIdList(connectedQueryMap);
        List<Task> taskList = taskDao.queryTasksByIdList(connectedQueryMap);
        List<TaskDto> taskDtoList = taskListToTaskDtoListConverter(taskList, searchConditionsDto);
        for (StudentTaskDto curStudentTaskDto : studentTaskDtosList) {
            Student student = getStudent(curStudentTaskDto.getStudentId(), studentList);
            curStudentTaskDto.setStudentUsername(student.getUsername());
            curStudentTaskDto.setStudentName(student.getName());
            TaskDto taskDto = getTaskDto(curStudentTaskDto.getTaskId(), taskDtoList);
            curStudentTaskDto.setTaskUrl(taskDto.getUrl());
            curStudentTaskDto.setTaskName(taskDto.getName());
            curStudentTaskDto.setTaskDescription(taskDto.getDescription());
            curStudentTaskDto.setCourseName(taskDto.getCourseName());
            curStudentTaskDto.setTeacherName(taskDto.getTeacherName());
        }
        return studentTaskDtosList;
    }

    private List<TaskDto> taskListToTaskDtoListConverter(List<Task> tasksList, SearchConditionsDto searchConditionsDto) {
        List<TaskDto> taskDtosList = new ArrayList<>();
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
        if (!StringUtils.isEmpty(searchConditionsDto.getCourseName())) {
            connectedQueryMap.put("courseName", searchConditionsDto.getCourseName());
        }
        if (!StringUtils.isEmpty(searchConditionsDto.getName())) {
            connectedQueryMap.put("teacherName", searchConditionsDto.getName());
        }
        List<Course> courseList = courseDao.queryCoursesByIdList(connectedQueryMap);
        List<Teacher> teacherList = teacherDao.queryTeachersByIdList(connectedQueryMap);
        for (TaskDto curTaskDto : taskDtosList) {
            curTaskDto.setCourseName(getCourseName(curTaskDto.getCourseId(), courseList));
            curTaskDto.setTeacherName(getTeacherName(curTaskDto.getTeacherId(), teacherList));
        }
        return taskDtosList;
    }
}
