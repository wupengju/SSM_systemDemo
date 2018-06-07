package com.menglin.dto;

import com.google.common.base.Converter;
import com.menglin.entity.College;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class CollegeDto extends BaseTableDto {
    private String description;
    private String code;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public College convertToCollege() {
        CollegeDto.CollegeDtoToCollegeConverter collegeDtoToCollegeConverter = new CollegeDto.CollegeDtoToCollegeConverter();
        return collegeDtoToCollegeConverter.convert(this);
    }

    public CollegeDto convertFor(College college) {
        CollegeDto.CollegeDtoToCollegeConverter collegeDtoToCollegeConverter = new CollegeDto.CollegeDtoToCollegeConverter();
        return collegeDtoToCollegeConverter.reverse().convert(college);
    }

    private static class CollegeDtoToCollegeConverter extends Converter<CollegeDto, College> {

        @Override
        protected College doForward(CollegeDto collegeDto) {
            College college = new College();
            BeanUtils.copyProperties(collegeDto, college);
            return college;
        }

        @Override
        protected CollegeDto doBackward(College college) {
            CollegeDto collegeDto = new CollegeDto();
            BeanUtils.copyProperties(college, collegeDto);
            return collegeDto;
        }
    }
}
