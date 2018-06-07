package com.menglin.dto;

import com.google.common.base.Converter;
import com.menglin.entity.StudentTask;
import com.menglin.entity.StudentTaskAllInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class StudentTaskDto extends BaseTableDto {
    private Long studentId;
    private String studentName;
    private String studentUsername;
    private Long taskId;
    private String taskName;
    private String taskDescription;
    private String taskUrl;
    private String classTeamName;
    private String courseName;
    private String teacherName;
    private String teacherUsername;
    private String content;
    private String answerUrl;
    private String score;
    private String comments;

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentUsername() {
        return studentUsername;
    }

    public void setStudentUsername(String studentUsername) {
        this.studentUsername = studentUsername;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public String getTaskUrl() {
        return taskUrl;
    }

    public void setTaskUrl(String taskUrl) {
        this.taskUrl = taskUrl;
    }

    public String getClassTeamName() {
        return classTeamName;
    }

    public void setClassTeamName(String classTeamName) {
        this.classTeamName = classTeamName;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getTeacherUsername() {
        return teacherUsername;
    }

    public void setTeacherUsername(String teacherUsername) {
        this.teacherUsername = teacherUsername;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAnswerUrl() {
        return answerUrl;
    }

    public void setAnswerUrl(String answerUrl) {
        this.answerUrl = answerUrl;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public StudentTask convertToStudentTask() {
        StudentTaskDto.StudentTaskDtoToStudentTaskConverter studentTaskDtoToStudentTaskConverter = new StudentTaskDto.StudentTaskDtoToStudentTaskConverter();
        return studentTaskDtoToStudentTaskConverter.convert(this);
    }

    public StudentTaskDto convertFor(StudentTask studentTask) {
        StudentTaskDto.StudentTaskDtoToStudentTaskConverter studentTaskDtoToStudentTaskConverter = new StudentTaskDto.StudentTaskDtoToStudentTaskConverter();
        return studentTaskDtoToStudentTaskConverter.reverse().convert(studentTask);
    }

    private static class StudentTaskDtoToStudentTaskConverter extends Converter<StudentTaskDto, StudentTask> {

        @Override
        protected StudentTask doForward(StudentTaskDto studentTaskDto) {
            StudentTask studentTask = new StudentTask();
            BeanUtils.copyProperties(studentTaskDto, studentTask);
            return studentTask;
        }

        @Override
        protected StudentTaskDto doBackward(StudentTask studentTask) {
            StudentTaskDto studentTaskDto = new StudentTaskDto();
            BeanUtils.copyProperties(studentTask, studentTaskDto);
            return studentTaskDto;
        }
    }

    public StudentTaskAllInfo convertToStudentTaskAllInfo() {
        StudentTaskDto.StudentTaskAllInfoDtoToStudentTaskAllInfoConverter studentTaskAllInfoDtoToStudentTaskAllInfoConverter = new StudentTaskDto.StudentTaskAllInfoDtoToStudentTaskAllInfoConverter();
        return studentTaskAllInfoDtoToStudentTaskAllInfoConverter.convert(this);
    }

    public StudentTaskDto convertFor(StudentTaskAllInfo studentTaskAllInfo) {
        StudentTaskDto.StudentTaskAllInfoDtoToStudentTaskAllInfoConverter studentTaskAllInfoDtoToStudentTaskAllInfoConverter = new StudentTaskDto.StudentTaskAllInfoDtoToStudentTaskAllInfoConverter();
        return studentTaskAllInfoDtoToStudentTaskAllInfoConverter.reverse().convert(studentTaskAllInfo);
    }

    private static class StudentTaskAllInfoDtoToStudentTaskAllInfoConverter extends Converter<StudentTaskDto, StudentTaskAllInfo> {

        @Override
        protected StudentTaskAllInfo doForward(StudentTaskDto studentTaskAllInfoDto) {
            StudentTaskAllInfo studentTaskAllInfo = new StudentTaskAllInfo();
            BeanUtils.copyProperties(studentTaskAllInfoDto, studentTaskAllInfo);
            return studentTaskAllInfo;
        }

        @Override
        protected StudentTaskDto doBackward(StudentTaskAllInfo studentTaskAllInfo) {
            StudentTaskDto studentTaskDto = new StudentTaskDto();
            BeanUtils.copyProperties(studentTaskAllInfo, studentTaskDto);
            return studentTaskDto;
        }
    }
}