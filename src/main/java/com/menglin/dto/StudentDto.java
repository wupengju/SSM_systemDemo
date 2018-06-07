package com.menglin.dto;

import com.google.common.base.Converter;
import com.menglin.entity.Student;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class StudentDto extends UserDto {
    private String name;
    private Long collegeId;
    private String collegeName;
    private Long majorId;
    private String majorName;
    private Long classTeamId;
    private String classTeamName;
    private String grade;
    private String creator;
    private String modifier;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCollegeId() {
        return collegeId;
    }

    public void setCollegeId(Long collegeId) {
        this.collegeId = collegeId;
    }

    public String getCollegeName() {
        return collegeName;
    }

    public void setCollegeName(String collegeName) {
        this.collegeName = collegeName;
    }

    public Long getMajorId() {
        return majorId;
    }

    public void setMajorId(Long majorId) {
        this.majorId = majorId;
    }

    public String getMajorName() {
        return majorName;
    }

    public void setMajorName(String majorName) {
        this.majorName = majorName;
    }

    public Long getClassTeamId() {
        return classTeamId;
    }

    public void setClassTeamId(Long classTeamId) {
        this.classTeamId = classTeamId;
    }

    public String getClassTeamName() {
        return classTeamName;
    }

    public void setClassTeamName(String classTeamName) {
        this.classTeamName = classTeamName;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    public Student convertToStudent() {
        StudentDtoToStudentConverter studentDtoToStudentConverter = new StudentDtoToStudentConverter();
        return studentDtoToStudentConverter.convert(this);
    }

    public StudentDto convertFor(Student student) {
        StudentDtoToStudentConverter studentDtoToStudentConverter = new StudentDtoToStudentConverter();
        return studentDtoToStudentConverter.reverse().convert(student);
    }

    private static class StudentDtoToStudentConverter extends Converter<StudentDto, Student> {

        @Override
        protected Student doForward(StudentDto studentDto) {
            Student student = new Student();
            BeanUtils.copyProperties(studentDto, student);
            return student;
        }

        @Override
        protected StudentDto doBackward(Student student) {
            StudentDto studentDto = new StudentDto();
            BeanUtils.copyProperties(student, studentDto);
            return studentDto;
        }
    }
}
