package com.menglin.service;

import com.github.pagehelper.PageInfo;
import com.menglin.dto.SearchConditionsDto;
import com.menglin.dto.TaskDto;

public interface TaskService {
    int addTask(TaskDto taskDto);

    int updateTask(TaskDto taskDto);

    int updateTaskUrlAndWriteTaskAttachment(Long taskId, String taskAttachmentUrl, String modifier, Boolean isDeleteAttachment);

    void deleteTaskById(Long id);

    void batchDeleteTasksByIds(String ids);

    TaskDto getTaskById(Long id);

    PageInfo<TaskDto> getTasksByPageAndTeacherId(int start, int pageSize, Long teacherId, SearchConditionsDto searchConditionsDto);

    PageInfo<TaskDto> getTasksByPage(int start, int pageSize, SearchConditionsDto searchConditionsDto);
}
