package com.menglin.service;

import com.github.pagehelper.PageInfo;
import com.menglin.dto.IdAndNameDto;
import com.menglin.dto.RoleDto;
import com.menglin.dto.SearchConditionsDto;

import java.util.List;

public interface RoleService {
    int addRole(RoleDto roleDto);

    int updateRole(RoleDto roleDto);

    void deleteRoleById(Long id);

    void batchDeleteRolesByIds(String ids);

    RoleDto getRoleById(Long id);

    RoleDto getRoleByName(String name);

    List<IdAndNameDto> getRoleIdAndNameList();

    PageInfo<RoleDto> getRolesByPage(int start, int pageSize, SearchConditionsDto searchConditionsDto);
}
