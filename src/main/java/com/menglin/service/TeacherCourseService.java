package com.menglin.service;

import com.menglin.entity.TeacherCourse;

public interface TeacherCourseService {
    int addTeacherCourse(TeacherCourse major);

    int updateTeacherCourse(TeacherCourse major);

    void deleteTeacherCourseById(Long id);

    TeacherCourse getTeacherCourseById(Long id);
}
