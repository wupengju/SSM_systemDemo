package com.menglin.service;

import com.github.pagehelper.PageInfo;
import com.menglin.dto.SearchConditionsDto;
import com.menglin.dto.StudentTaskDto;

public interface StudentTaskService {
    int addStudentTask(StudentTaskDto studentTaskDto);

    int updateStudentTask(StudentTaskDto studentTaskDto, String identity);

    int updateStudentTaskUrlAndWriteTaskAttachment(Long studentTaskId, String taskAnswerAttachmentUrl, String modifier, Boolean isDeleteAnswerAttachment);

    void deleteStudentTaskById(Long id);

    StudentTaskDto getStudentTaskById(Long id);

    StudentTaskDto getStudentTaskByStudentIdAndTaskId(Long studentId, Long taskId);

    PageInfo<StudentTaskDto> getStudentTasksByPage(int start, int pageSize, SearchConditionsDto searchConditionsDto);

    PageInfo<StudentTaskDto> getPendingStudentTasksByPage(int start, int pageSize, Long studentId, SearchConditionsDto searchConditionsDto);

    PageInfo<StudentTaskDto> getCompletedStudentTasksByPage(int start, int pageSize, Long studentId, SearchConditionsDto searchConditionsDto);

    PageInfo<StudentTaskDto> getHistoricalStudentTasksByPage(int start, int pageSize, Long studentId, SearchConditionsDto searchConditionsDto);

    PageInfo<StudentTaskDto> getCorrectingStudentTasksByPage(int start, int pageSize, Long teacherId, SearchConditionsDto searchConditionsDto);
}
