package com.menglin.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.menglin.dao.RoleDao;
import com.menglin.dto.IdAndNameDto;
import com.menglin.dto.RoleDto;
import com.menglin.dto.SearchConditionsDto;
import com.menglin.entity.Role;
import com.menglin.enums.ErrorStateEnum;
import com.menglin.exception.ServiceException;
import com.menglin.redis.RedisKeys;
import com.menglin.redis.RedisUtil;
import com.menglin.service.RoleService;
import com.menglin.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;

import static com.menglin.common.AssertArguments.*;

@Service("roleService")
public class RoleServiceImpl implements RoleService {
    private Logger logger = LoggerFactory.getLogger(RoleServiceImpl.class);

    @Resource
    private RedisUtil redisUtil;
    @Resource
    private RoleDao roleDao;
    @Resource
    private RoleDto roleDto;
    @Resource
    private IdAndNameDto idAndNameDto;

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public int addRole(RoleDto roleDto) {
        checkNotNull(roleDto, "角色不能为空");
        checkNotEmpty(roleDto.getName(), "角色名字不能为空");
        checkNotEmpty(roleDto.getDescription(), "角色描述不能为空");
        checkNotEmpty(roleDto.getCreator(), "角色创建者不能为空");

        validateIsSameNameRole(roleDto.getName());
        Role newRole = roleDto.convertToRole();
        newRole.setGmtCreate(new Date());
        int insertId;
        try {
            insertId = roleDao.insertSelective(newRole);
        } catch (Exception e) {
            logger.info("insert role fail, role:{}, e:{}", JSONObject.toJSONString(newRole), e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "插入角色记录失败");
        }
        if (insertId > 0) {
            logger.info("insert role success, save role to redis, role:{}", JSONObject.toJSONString(newRole));
            redisUtil.put(RedisKeys.ROLE_CACHE_KEY + newRole.getId(), newRole);
            return insertId;
        }

        return 0;
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public int updateRole(RoleDto roleDto) {
        checkNotNull(roleDto, "角色不能为空");
        checkNotEmpty(roleDto.getModifier(), "角色修改者不能为空");
        checkGreaterThanZero(roleDto.getId(), "角色 id 不能小于或等于零");

        Role updateRole;
        int updateId;
        try {
            updateRole = roleDao.selectByPrimaryKey(roleDto.getId());
            updateRole(updateRole, roleDto);
            updateId = roleDao.updateByPrimaryKey(updateRole);
        } catch (Exception e) {
            logger.info("update role fail, role:{}, e:{}", JSONObject.toJSONString(roleDto.convertToRole()), e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "修改角色记录失败");
        }
        if (updateId > 0) {
            logger.info("update role success, save role to redis, role:{}", JSONObject.toJSONString(updateRole));
            redisUtil.del(RedisKeys.ROLE_CACHE_KEY + updateRole.getId());
            redisUtil.put(RedisKeys.ROLE_CACHE_KEY + updateRole.getId(), updateRole);
            return updateId;
        }

        return 0;
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void deleteRoleById(Long id) {
        checkGreaterThanZero(id, "角色 id 不能小于或等于零");

        int deleteId;
        try {
            deleteId = roleDao.deleteByPrimaryKey(id);
        } catch (Exception e) {
            logger.info("delete role fail, id:{}, e:{}", id, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "删除角色记录失败");
        }
        if (deleteId > 0) {
            logger.info("delete role success, id:{}", deleteId);
            redisUtil.del(RedisKeys.ROLE_CACHE_KEY + deleteId);
        } else {
            logger.info("delete role fail, id:{}", id);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "删除角色记录不存在");
        }
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void batchDeleteRolesByIds(String ids) {
        checkNotEmpty(ids, "ids 不能为空");

        String[] idsStr = ids.split(",");
        for (String anIdsStr : idsStr) {
            deleteRoleById(Long.parseLong(anIdsStr));
        }
    }

    @Override
    public RoleDto getRoleById(Long id) {
        checkGreaterThanZero(id, "角色 id 不能小于或等于零");

        logger.info("get role by id:{}", id);
        Role role = (Role) redisUtil.get(RedisKeys.ROLE_CACHE_KEY + id, Role.class);
        if (role != null) {
            logger.info("role in redis, role:{}", JSONObject.toJSONString(role));
            return setRoleDtoGmtCreateAndGmtModify(role);
        }
        Role roleFromMysql;
        try {
            roleFromMysql = roleDao.selectByPrimaryKey(id);
        } catch (Exception e) {
            logger.info("get role by primary key fail, id:{}, e:{}", id, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据 id 查询角色记录失败");
        }
        if (roleFromMysql != null) {
            logger.info("get role from mysql and save role to redis, role:{}", JSONObject.toJSONString(roleFromMysql));
            redisUtil.put(RedisKeys.ROLE_CACHE_KEY + roleFromMysql.getId(), roleFromMysql);
            return setRoleDtoGmtCreateAndGmtModify(roleFromMysql);
        }

        return null;
    }

    @Override
    public RoleDto getRoleByName(String name) {
        checkNotEmpty(name, "角色名不能为空");

        logger.info("get role by name:{}", name);
        Role role = (Role) redisUtil.get(RedisKeys.ROLE_CACHE_KEY + name, Role.class);
        if (role != null) {
            logger.info("role in redis, role:{}", role);
            return setRoleDtoGmtCreateAndGmtModify(role);
        }
        Role roleFromMysql;
        try {
            roleFromMysql = roleDao.selectByName(name);
        } catch (Exception e) {
            logger.info("get role by primary key fail, name:{}, e:{}", name, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据角色名查询角色记录失败");
        }
        if (roleFromMysql != null) {
            logger.info("get role from mysql and save role to redis, role:{}", roleFromMysql);
            redisUtil.put(RedisKeys.ROLE_CACHE_KEY + roleFromMysql.getName(), roleFromMysql);
            return setRoleDtoGmtCreateAndGmtModify(roleFromMysql);
        }

        return null;
    }

    @Override
    public List<IdAndNameDto> getRoleIdAndNameList() {
        List<Role> roleList = roleDao.getRolesList();
        List<IdAndNameDto> idAndNameDtoList = new ArrayList<>();
        for (Role role : roleList) {
            idAndNameDtoList.add(idAndNameDto.createNewIdAndNameDto(role.getId(), role.getName()));
        }
        return idAndNameDtoList;
    }

    @Override
    public PageInfo<RoleDto> getRolesByPage(int start, int pageSize, SearchConditionsDto searchConditionsDto) {
        checkNotNull(searchConditionsDto, "分页查询的条件对象不能为空");
        checkGreaterThanOrIsZero(start, "分页查询的开始数必须大于零");
        checkGreaterThanZero(pageSize, "分页查询的单页总数必须大于零");

        Map<String, Object> map = new HashMap<>();
        Page page = PageHelper.startPage(start, pageSize);
        if (!StringUtils.isEmpty(searchConditionsDto.getName())) {
            map.put("name", searchConditionsDto.getName());
        }
        List<Role> rolesList;
        try {
            rolesList = roleDao.queryRolesByPage(map);
        } catch (Exception e) {
            logger.info("queryRolesByPage fail, map:{}, e:{}", map, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据条件分页查询角色记录失败");
        }
        if (rolesList != null) {
            List<RoleDto> roleDtosList = new Page<>(start, pageSize, true);
            Page roleDtosListPage = (Page) roleDtosList;
            roleDtosListPage.setTotal(page.getTotal());
            roleDtosList = queryRoleDtoList(roleDtosList, rolesList);
            return new PageInfo<>(roleDtosList);
        }

        return null;
    }

    private void validateIsSameNameRole(String name) {
        Role role;
        try {
            role = roleDao.selectByName(name);
        } catch (Exception e) {
            logger.info("selectByUsername fail, name:{}, e:{}", name, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据角色名查询角色记录失败");
        }
        if (role != null) {
            throw new ServiceException(ErrorStateEnum.DB_EXIST_SAME_RECORD.getState(), "添加的角色名为 " + name + " 的角色记录已存在");
        }
    }

    private void updateRole(Role updateRole, RoleDto roleDto) {
        if (!StringUtils.isEmpty(roleDto.getName())) {
            updateRole.setName(roleDto.getName());
        }
        if (!StringUtils.isEmpty(roleDto.getDescription())) {
            updateRole.setDescription(roleDto.getDescription());
        }
        updateRole.setModifier(roleDto.getModifier());
        updateRole.setGmtModify(new Date());
    }

    private RoleDto setRoleDtoGmtCreateAndGmtModify(Role role) {
        RoleDto newRoleDto = roleDto.convertFor(role);
        newRoleDto.setGmtCreate(DateUtil.formatDate(role.getGmtCreate()));
        newRoleDto.setGmtModify(DateUtil.formatDate(role.getGmtModify()));
        return newRoleDto;
    }

    private List<RoleDto> queryRoleDtoList(List<RoleDto> roleDtosList, List<Role> rolesList) {
        for (Role role : rolesList) {
            roleDtosList.add(setRoleDtoGmtCreateAndGmtModify(role));
        }
        return roleDtosList;
    }
}
