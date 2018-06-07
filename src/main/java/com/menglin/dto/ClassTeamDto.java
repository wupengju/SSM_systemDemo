package com.menglin.dto;

import com.google.common.base.Converter;
import com.menglin.entity.ClassTeam;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class ClassTeamDto extends BaseTableDto {
    private Long collegeId;
    private String collegeName;
    private Long majorId;
    private String majorName;

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

    public ClassTeam convertToClassTeam() {
        ClassTeamDto.ClassTeamDtoToClassTeamConverter classTeamDtoToClassTeamConverter = new ClassTeamDto.ClassTeamDtoToClassTeamConverter();
        return classTeamDtoToClassTeamConverter.convert(this);
    }

    public ClassTeamDto convertFor(ClassTeam classTeam) {
        ClassTeamDto.ClassTeamDtoToClassTeamConverter classTeamDtoToClassTeamConverter = new ClassTeamDto.ClassTeamDtoToClassTeamConverter();
        return classTeamDtoToClassTeamConverter.reverse().convert(classTeam);
    }

    private static class ClassTeamDtoToClassTeamConverter extends Converter<ClassTeamDto, ClassTeam> {

        @Override
        protected ClassTeam doForward(ClassTeamDto classTeamDto) {
            ClassTeam classTeam = new ClassTeam();
            BeanUtils.copyProperties(classTeamDto, classTeam);
            return classTeam;
        }

        @Override
        protected ClassTeamDto doBackward(ClassTeam classTeam) {
            ClassTeamDto classTeamDto = new ClassTeamDto();
            BeanUtils.copyProperties(classTeam, classTeamDto);
            return classTeamDto;
        }
    }
}
