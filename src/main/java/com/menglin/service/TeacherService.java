package com.menglin.service;

import com.github.pagehelper.PageInfo;
import com.menglin.dto.ModifyPasswordInfoDto;
import com.menglin.dto.PublishTaskDto;
import com.menglin.dto.SearchConditionsDto;
import com.menglin.dto.TeacherDto;

public interface TeacherService {
    int addTeacher(TeacherDto teacherDto);

    int updateTeacher(TeacherDto teacherDto);

    int modifyPassword(ModifyPasswordInfoDto modifyPasswordInfoDto);

    void deleteTeacherById(Long id);

    void batchDeleteTeachersByIds(String ids);

    void publishTask(PublishTaskDto publishTaskDto, Long teacherId, String username);

    TeacherDto getTeacherById(Long id);

    TeacherDto getTeacherByUsername(String username);

    PageInfo<TeacherDto> getTeachersByPage(int start, int pageSize, SearchConditionsDto searchConditionsDto);
}
