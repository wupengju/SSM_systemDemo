package com.menglin.dto;

import org.springframework.stereotype.Component;

@Component
public class PublishTaskDto {
    private Long taskId;
    private String classTeamIds;

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getClassTeamIds() {
        return classTeamIds;
    }

    public void setClassTeamIds(String classTeamIds) {
        this.classTeamIds = classTeamIds;
    }
}
