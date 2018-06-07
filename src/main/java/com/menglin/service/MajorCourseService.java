package com.menglin.service;

import com.menglin.entity.MajorCourse;

public interface MajorCourseService {
    int addMajorCourse(MajorCourse majorCourse);

    int updateMajorCourse(MajorCourse majorCourse);

    void deleteMajorCourseById(Long id);

    MajorCourse getMajorCourseById(Long id);
}