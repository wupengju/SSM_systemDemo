package com.menglin.service;

import com.github.pagehelper.PageInfo;
import com.menglin.dto.IdAndNameDto;
import com.menglin.dto.PermissionDto;
import com.menglin.dto.SearchConditionsDto;

import java.util.List;

public interface PermissionService {
    int addPermission(PermissionDto permissionDto);

    int updatePermission(PermissionDto permissionDto);

    void deletePermissionById(Long id);

    void batchDeletePermissionsByIds(String ids);

    PermissionDto getPermissionById(Long id);

    PermissionDto getPermissionByName(String name);

    List<IdAndNameDto> getPermissionIdAndNameList();

    PageInfo<PermissionDto> getPermissionsByPage(int start, int pageSize, SearchConditionsDto searchConditionsDto);
}
