package com.menglin.service;

import com.github.pagehelper.PageInfo;
import com.menglin.dto.CourseDto;
import com.menglin.dto.IdAndNameDto;
import com.menglin.dto.SearchConditionsDto;

import java.util.List;

public interface CourseService {
    int addCourse(CourseDto courseDto);

    int updateCourse(CourseDto courseDto);

    void deleteCourseById(Long id);

    void batchDeleteCoursesByIds(String ids);

    CourseDto getCourseById(Long id);

    CourseDto getCourseByName(String name);

    List<IdAndNameDto> getCourseIdAndNameListByTeacherId(Long teacherId);

    PageInfo<CourseDto> getCoursesByPage(int start, int pageSize, SearchConditionsDto searchConditionsDto);
}