package com.menglin.dto;

import com.google.common.base.Converter;
import com.menglin.entity.Task;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class TaskDto extends BaseTableDto {
    private String description;
    private String url;
    private String status;
    private Long courseId;
    private String courseName;
    private Long teacherId;
    private String teacherName;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public Task convertToTask() {
        TaskDto.TaskDtoToTaskConverter taskDtoToTaskConverter = new TaskDto.TaskDtoToTaskConverter();
        return taskDtoToTaskConverter.convert(this);
    }

    public TaskDto convertFor(Task task) {
        TaskDto.TaskDtoToTaskConverter taskDtoToTaskConverter = new TaskDto.TaskDtoToTaskConverter();
        return taskDtoToTaskConverter.reverse().convert(task);
    }

    private static class TaskDtoToTaskConverter extends Converter<TaskDto, Task> {

        @Override
        protected Task doForward(TaskDto taskDto) {
            Task task = new Task();
            BeanUtils.copyProperties(taskDto, task);
            return task;
        }

        @Override
        protected TaskDto doBackward(Task task) {
            TaskDto taskDto = new TaskDto();
            BeanUtils.copyProperties(task, taskDto);
            return taskDto;
        }
    }
}
