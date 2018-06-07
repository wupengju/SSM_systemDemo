package com.menglin.service;

import com.menglin.entity.RolePermission;

public interface RolePermissionService {
    int addRolePermission(RolePermission rolePermission);

    int updateRolePermission(RolePermission rolePermission);

    void deleteRolePermissionById(Long id);

    RolePermission getRolePermissionById(Long id);
}
