package com.menglin.service;

import com.github.pagehelper.PageInfo;
import com.menglin.dto.AdminDto;
import com.menglin.dto.SearchConditionsDto;

public interface AdminService {
    int addAdmin(AdminDto adminDto);

    int updateAdmin(AdminDto adminDto);

    void deleteAdminById(Long id);

    void batchDeleteAdminsByIds(String ids);

    AdminDto getAdminById(Long id);

    AdminDto getAdminByUsername(String username);

    PageInfo<AdminDto> getAdminsByPage(int start, int pageSize, SearchConditionsDto searchConditionsDto);
}
