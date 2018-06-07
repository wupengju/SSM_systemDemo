package com.menglin.service;

import com.github.pagehelper.PageInfo;
import com.menglin.dto.IdAndNameDto;
import com.menglin.dto.MajorDto;
import com.menglin.dto.SearchConditionsDto;

import java.util.List;

public interface MajorService {
    int addMajor(MajorDto majorDto);

    int updateMajor(MajorDto majorDto);

    void deleteMajorById(Long id);

    void batchDeleteMajorsByIds(String ids);

    MajorDto getMajorById(Long id);

    MajorDto getMajorByName(String name);

    List<IdAndNameDto> getMajorIdAndNameList(Long collegeId);

    PageInfo<MajorDto> getMajorsByPage(int start, int pageSize, SearchConditionsDto searchConditionsDto);
}