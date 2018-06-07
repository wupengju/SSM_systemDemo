package com.menglin.dto;

import com.google.common.base.Converter;
import com.menglin.entity.Teacher;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class TeacherDto extends UserDto {
    private String name;
    private Long collegeId;
    private String collegeName;
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

    public Teacher convertToTeacher() {
        TeacherDto.TeacherDtoToTeacherConverter teacherDtoToTeacherConverter = new TeacherDto.TeacherDtoToTeacherConverter();
        return teacherDtoToTeacherConverter.convert(this);
    }

    public TeacherDto convertFor(Teacher teacher) {
        TeacherDto.TeacherDtoToTeacherConverter teacherDtoToTeacherConverter = new TeacherDto.TeacherDtoToTeacherConverter();
        return teacherDtoToTeacherConverter.reverse().convert(teacher);
    }

    private static class TeacherDtoToTeacherConverter extends Converter<TeacherDto, Teacher> {

        @Override
        protected Teacher doForward(TeacherDto teacherDto) {
            Teacher teacher = new Teacher();
            BeanUtils.copyProperties(teacherDto, teacher);
            return teacher;
        }

        @Override
        protected TeacherDto doBackward(Teacher teacher) {
            TeacherDto teacherDto = new TeacherDto();
            BeanUtils.copyProperties(teacher, teacherDto);
            return teacherDto;
        }
    }
}
