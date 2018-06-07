package com.menglin.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.menglin.dao.RolePermissionDao;
import com.menglin.entity.RolePermission;
import com.menglin.enums.ErrorStateEnum;
import com.menglin.exception.ServiceException;
import com.menglin.service.RolePermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

import static com.menglin.common.AssertArguments.*;

@Service("rolePermissionService")
public class RolePermissionServiceImpl implements RolePermissionService {
    private Logger logger = LoggerFactory.getLogger(RolePermissionServiceImpl.class);

    @Resource
    private RolePermissionDao rolePermissionDao;

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public int addRolePermission(RolePermission rolePermission) {
        checkNotNull(rolePermission, "角色权限不能为空");
        checkNotEmpty(rolePermission.getCreator(), "角色权限创建者不能为空");
        checkGreaterThanZero(rolePermission.getRoleId(), "角色 id 不能小于或等于零");
        checkGreaterThanZero(rolePermission.getPermissionId(), "权限 id 不能小于或等于零");

        validateIsSameNameRolePermission(rolePermission.getRoleId(), rolePermission.getPermissionId());
        rolePermission.setGmtCreate(new Date());
        int insertId;
        try {
            insertId = rolePermissionDao.insertSelective(rolePermission);
        } catch (Exception e) {
            logger.info("insert rolePermission fail, rolePermission:{}, e:{}", JSONObject.toJSONString(rolePermission), e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "插入角色权限记录失败");
        }
        if (insertId > 0) {
            logger.info("insert rolePermission success, save rolePermission to redis, rolePermission:{}", JSONObject.toJSONString(rolePermission));
            return insertId;
        }

        return 0;
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public int updateRolePermission(RolePermission rolePermission) {
        checkNotNull(rolePermission, "角色权限不能为空");
        checkNotEmpty(rolePermission.getModifier(), "角色权限修改者不能为空");
        checkGreaterThanZero(rolePermission.getId(), "角色权限 id 不能小于或等于零");

        RolePermission updateRolePermission;
        int updateId;
        try {
            updateRolePermission = rolePermissionDao.selectByPrimaryKey(rolePermission.getId());
            updateRolePermission(updateRolePermission, rolePermission);
            updateId = rolePermissionDao.updateByPrimaryKey(updateRolePermission);
        } catch (Exception e) {
            logger.info("update rolePermission fail, rolePermission:{}, e:{}", JSONObject.toJSONString(rolePermission), e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "修改角色权限记录失败");
        }
        if (updateId > 0) {
            logger.info("update rolePermission success, rolePermission:{}", JSONObject.toJSONString(updateRolePermission));
            return updateId;
        }

        return 0;
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void deleteRolePermissionById(Long id) {
        checkGreaterThanZero(id, "角色权限 id 不能小于或等于零");

        int deleteId;
        try {
            deleteId = rolePermissionDao.deleteByPrimaryKey(id);
        } catch (Exception e) {
            logger.info("delete rolePermission fail, id:{}, e:{}", id, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "删除角色权限记录失败");
        }
        if (deleteId > 0) {
            logger.info("delete rolePermission success, id:{}", deleteId);
        } else {
            logger.info("delete rolePermission fail, id:{}", id);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "删除角色权限记录不存在");
        }
    }

    @Override
    public RolePermission getRolePermissionById(Long id) {
        checkGreaterThanZero(id, "角色权限 id 不能小于或等于零");

        logger.info("get rolePermission by id:{}", id);
        RolePermission rolePermissionFromMysql;
        try {
            rolePermissionFromMysql = rolePermissionDao.selectByPrimaryKey(id);
        } catch (Exception e) {
            logger.info("get rolePermission by primary key fail, id:{}, e:{}", id, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据 id 查询角色权限记录失败");
        }
        if (rolePermissionFromMysql != null) {
            logger.info("get rolePermission from mysql and save rolePermission to redis, rolePermission:{}", JSONObject.toJSONString(rolePermissionFromMysql));
            return rolePermissionFromMysql;
        }

        return null;
    }

    private void validateIsSameNameRolePermission(Long roleId, Long permissionId) {
        RolePermission rolePermission;
        try {
            rolePermission = rolePermissionDao.selectByRoleIdAndPermissionId(roleId, permissionId);
        } catch (Exception e) {
            logger.info("selectByMajorIdAndCourseId fail, roleId:{}, permissionId:{}, e:{}", roleId, permissionId, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据角色ID和权限ID查询角色权限记录失败");
        }
        if (rolePermission != null) {
            throw new ServiceException(ErrorStateEnum.DB_EXIST_SAME_RECORD.getState(), "添加的角色权限记录已存在");
        }
    }

    private void updateRolePermission(RolePermission updateRolePermission, RolePermission rolePermission) {
        if (rolePermission.getRoleId() > 0) {
            updateRolePermission.setRoleId(rolePermission.getRoleId());
        }
        if (rolePermission.getPermissionId() > 0) {
            updateRolePermission.setPermissionId(rolePermission.getPermissionId());
        }
        updateRolePermission.setModifier(rolePermission.getModifier());
        updateRolePermission.setGmtModify(new Date());
    }
}
