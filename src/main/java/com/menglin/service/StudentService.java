package com.menglin.service;

import com.github.pagehelper.PageInfo;
import com.menglin.dto.ModifyPasswordInfoDto;
import com.menglin.dto.SearchConditionsDto;
import com.menglin.dto.StudentDto;

import java.util.List;

public interface StudentService {

    int addStudent(StudentDto studentDto);

    int updateStudent(StudentDto studentDto);

    int modifyPassword(ModifyPasswordInfoDto modifyPasswordInfoDto);

    void deleteStudentById(Long id);

    void batchDeleteStudentsByIds(String ids);

    StudentDto getStudentById(Long id);

    StudentDto getStudentByUsername(String username);

    List<StudentDto> getStudentsByClassTeamId(Long classTeamId);

    PageInfo<StudentDto> getStudentsByPage(int start, int pageSize, SearchConditionsDto searchConditionsDto);
}
