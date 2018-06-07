package com.menglin.service;

import com.github.pagehelper.PageInfo;
import com.menglin.dto.ClassTeamDto;
import com.menglin.dto.IdAndNameDto;
import com.menglin.dto.SearchConditionsDto;

import java.util.List;

public interface ClassTeamService {
    int addClassTeam(ClassTeamDto classTeamDto);

    int updateClassTeam(ClassTeamDto classTeamDto);

    void deleteClassTeamById(Long id);

    void batchDeleteClassTeamsByIds(String ids);

    ClassTeamDto getClassTeamById(Long id);

    ClassTeamDto getClassTeamByName(String name);

    List<IdAndNameDto> getClassTeamIdAndNameListByCollegeIdAndMajorId(Long collegeId, Long majorId);

    List<IdAndNameDto> getClassTeamIdAndNameListByTeacherId(Long teacherId);

    PageInfo<ClassTeamDto> getClassTeamsByPage(int start, int pageSize, SearchConditionsDto searchConditionsDto);
}
