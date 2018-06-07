package com.menglin.dto;

import com.google.common.base.Converter;
import com.menglin.entity.Major;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class MajorDto extends BaseTableDto {
    private String category;
    private Long collegeId;
    private String collegeName;
    private String code;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Major convertToMajor() {
        MajorDto.MajorDtoToMajorConverter majorDtoToMajorConverter = new MajorDto.MajorDtoToMajorConverter();
        return majorDtoToMajorConverter.convert(this);
    }

    public MajorDto convertFor(Major major) {
        MajorDto.MajorDtoToMajorConverter majorDtoToMajorConverter = new MajorDto.MajorDtoToMajorConverter();
        return majorDtoToMajorConverter.reverse().convert(major);
    }

    private static class MajorDtoToMajorConverter extends Converter<MajorDto, Major> {

        @Override
        protected Major doForward(MajorDto majorDto) {
            Major major = new Major();
            BeanUtils.copyProperties(majorDto, major);
            return major;
        }

        @Override
        protected MajorDto doBackward(Major major) {
            MajorDto majorDto = new MajorDto();
            BeanUtils.copyProperties(major, majorDto);
            return majorDto;
        }
    }
}
