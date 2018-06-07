package com.menglin.service;

import com.menglin.entity.StudentCourse;

public interface StudentCourseService {
    int addStudentCourse(StudentCourse major);

    int updateStudentCourse(StudentCourse major);

    void deleteStudentCourseById(Long id);

    StudentCourse getStudentCourseById(Long id);
}
