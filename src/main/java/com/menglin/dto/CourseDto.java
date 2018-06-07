package com.menglin.dto;

import com.google.common.base.Converter;
import com.menglin.entity.Course;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class CourseDto extends BaseTableDto {
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Course convertToCourse() {
        CourseDto.CourseDtoToCourseConverter courseDtoToCourseConverter = new CourseDto.CourseDtoToCourseConverter();
        return courseDtoToCourseConverter.convert(this);
    }

    public CourseDto convertFor(Course course) {
        CourseDto.CourseDtoToCourseConverter courseDtoToCourseConverter = new CourseDto.CourseDtoToCourseConverter();
        return courseDtoToCourseConverter.reverse().convert(course);
    }

    private static class CourseDtoToCourseConverter extends Converter<CourseDto, Course> {

        @Override
        protected Course doForward(CourseDto courseDto) {
            Course course = new Course();
            BeanUtils.copyProperties(courseDto, course);
            return course;
        }

        @Override
        protected CourseDto doBackward(Course course) {
            CourseDto courseDto = new CourseDto();
            BeanUtils.copyProperties(course, courseDto);
            return courseDto;
        }
    }
}

