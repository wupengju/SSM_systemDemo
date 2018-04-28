package com.menglin.service;

import com.menglin.entity.Student;

public interface StudentService {
    Student getStudentById(Long id);

    Student getStudentByUsername(String username);

    Student getStudentByUsernameAndPassword(String username, String password);
}
