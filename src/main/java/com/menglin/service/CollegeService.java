package com.menglin.service;

import com.github.pagehelper.PageInfo;
import com.menglin.dto.CollegeDto;
import com.menglin.dto.IdAndNameDto;
import com.menglin.dto.SearchConditionsDto;

import java.util.List;

public interface CollegeService {
    int addCollege(CollegeDto collegeDto);

    int updateCollege(CollegeDto collegeDto);

    void deleteCollegeById(Long id);

    void batchDeleteCollegesByIds(String ids);

    CollegeDto getCollegeById(Long id);

    CollegeDto getCollegeByName(String name);

    List<IdAndNameDto> getCollegeIdAndNameList();

    PageInfo<CollegeDto> getCollegesByPage(int start, int pageSize, SearchConditionsDto searchConditionsDto);
}