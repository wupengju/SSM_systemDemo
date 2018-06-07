package com.menglin.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.menglin.dao.PermissionDao;
import com.menglin.dto.IdAndNameDto;
import com.menglin.dto.PermissionDto;
import com.menglin.dto.SearchConditionsDto;
import com.menglin.entity.Permission;
import com.menglin.enums.ErrorStateEnum;
import com.menglin.exception.ServiceException;
import com.menglin.redis.RedisKeys;
import com.menglin.redis.RedisUtil;
import com.menglin.service.PermissionService;
import com.menglin.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;

import static com.menglin.common.AssertArguments.*;

@Service("permissionService")
public class PermissionServiceImpl implements PermissionService {
    private Logger logger = LoggerFactory.getLogger(PermissionServiceImpl.class);

    @Resource
    private RedisUtil redisUtil;
    @Resource
    private PermissionDao permissionDao;
    @Resource
    private PermissionDto permissionDto;
    @Resource
    private IdAndNameDto idAndNameDto;

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public int addPermission(PermissionDto permissionDto) {
        checkNotNull(permissionDto, "权限不能为空");
        checkNotEmpty(permissionDto.getName(), "权限名字不能为空");
        checkNotEmpty(permissionDto.getDescription(), "权限描述不能为空");
        checkNotEmpty(permissionDto.getCreator(), "权限创建者不能为空");

        validateIsSameNamePermission(permissionDto.getName());
        Permission newPermission = permissionDto.convertToPermission();
        newPermission.setGmtCreate(new Date());
        int insertId;
        try {
            insertId = permissionDao.insertSelective(newPermission);
        } catch (Exception e) {
            logger.info("insert permission fail, permission:{}, e:{}", JSONObject.toJSONString(permissionDto.convertToPermission()), e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "插入权限记录失败");
        }
        if (insertId > 0) {
            logger.info("insert permission success, save permission to redis, permission:{}", JSONObject.toJSONString(newPermission));
            redisUtil.put(RedisKeys.MPERMISSION_CACHE_KEY + newPermission.getId(), newPermission);
            return insertId;
        }

        return 0;
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public int updatePermission(PermissionDto permissionDto) {
        checkNotNull(permissionDto, "权限不能为空");
        checkNotEmpty(permissionDto.getModifier(), "权限修改者不能为空");
        checkGreaterThanZero(permissionDto.getId(), "权限 id 不能小于或等于零");

        Permission updatePermission;
        int updateId;
        try {
            updatePermission = permissionDao.selectByPrimaryKey(permissionDto.getId());
            updatePermission(updatePermission, permissionDto);
            updateId = permissionDao.updateByPrimaryKey(updatePermission);
        } catch (Exception e) {
            logger.info("update permission fail, permission:{}, e:{}", JSONObject.toJSONString(permissionDto.convertToPermission()), e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "修改权限记录失败");
        }
        if (updateId > 0) {
            logger.info("update permission success, save permission to redis, permission:{}", JSONObject.toJSONString(updatePermission));
            redisUtil.del(RedisKeys.MPERMISSION_CACHE_KEY + updatePermission.getId());
            redisUtil.put(RedisKeys.MPERMISSION_CACHE_KEY + updatePermission.getId(), updatePermission);
            return updateId;
        }

        return 0;
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void deletePermissionById(Long id) {
        checkGreaterThanZero(id, "权限 id 不能小于或等于零");

        int deleteId;
        try {
            deleteId = permissionDao.deleteByPrimaryKey(id);
        } catch (Exception e) {
            logger.info("delete permission fail, id:{}, e:{}", id, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "删除权限记录失败");
        }
        if (deleteId > 0) {
            logger.info("delete permission success, id:{}", deleteId);
            redisUtil.del(RedisKeys.MPERMISSION_CACHE_KEY + deleteId);
        } else {
            logger.info("delete permission fail, id:{}", id);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "删除权限记录不存在");
        }
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void batchDeletePermissionsByIds(String ids) {
        checkNotEmpty(ids, "ids 不能为空");

        String[] idsStr = ids.split(",");
        for (String anIdsStr : idsStr) {
            deletePermissionById(Long.parseLong(anIdsStr));
        }
    }

    @Override
    public PermissionDto getPermissionById(Long id) {
        checkGreaterThanZero(id, "权限 id 不能小于或等于零");

        logger.info("get permission by id:{}", id);
        Permission permission = (Permission) redisUtil.get(RedisKeys.MPERMISSION_CACHE_KEY + id, Permission.class);
        if (permission != null) {
            logger.info("permission in redis, permission:{}", JSONObject.toJSONString(permission));
            return setPermissionDtoGmtCreateAndGmtModify(permission);
        }
        Permission permissionFromMysql;
        try {
            permissionFromMysql = permissionDao.selectByPrimaryKey(id);
        } catch (Exception e) {
            logger.info("get permission by primary key fail, id:{}, e:{}", id, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据 id 查询权限记录失败");
        }
        if (permissionFromMysql != null) {
            logger.info("get permission from mysql and save permission to redis, permission:{}", JSONObject.toJSONString(permissionFromMysql));
            redisUtil.put(RedisKeys.MPERMISSION_CACHE_KEY + permissionFromMysql.getId(), permissionFromMysql);
            return setPermissionDtoGmtCreateAndGmtModify(permissionFromMysql);
        }

        return null;
    }

    @Override
    public PermissionDto getPermissionByName(String name) {
        checkNotEmpty(name, "权限名不能为空");

        logger.info("get permission by name:{}", name);
        Permission permission = (Permission) redisUtil.get(RedisKeys.MPERMISSION_CACHE_KEY + name, Permission.class);
        if (permission != null) {
            logger.info("permission in redis, permission:{}", permission);
            return setPermissionDtoGmtCreateAndGmtModify(permission);
        }
        Permission permissionFromMysql;
        try {
            permissionFromMysql = permissionDao.selectByName(name);
        } catch (Exception e) {
            logger.info("get permission by primary key fail, name:{}, e:{}", name, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据权限名查询权限记录失败");
        }
        if (permissionFromMysql != null) {
            logger.info("get permission from mysql and save permission to redis, permission:{}", permissionFromMysql);
            redisUtil.put(RedisKeys.MPERMISSION_CACHE_KEY + permissionFromMysql.getName(), permissionFromMysql);
            return setPermissionDtoGmtCreateAndGmtModify(permissionFromMysql);
        }

        return null;
    }

    @Override
    public List<IdAndNameDto> getPermissionIdAndNameList() {
        List<Permission> permissionsList = permissionDao.getPermissionsList();
        List<IdAndNameDto> idAndNameDtoList = new ArrayList<>();
        for (Permission permission : permissionsList) {
            idAndNameDtoList.add(idAndNameDto.createNewIdAndNameDto(permission.getId(), permission.getName()));
        }
        return idAndNameDtoList;
    }

    @Override
    public PageInfo<PermissionDto> getPermissionsByPage(int start, int pageSize, SearchConditionsDto searchConditionsDto) {
        checkNotNull(searchConditionsDto, "分页查询的条件对象不能为空");
        checkGreaterThanOrIsZero(start, "分页查询的开始数必须大于零");
        checkGreaterThanZero(pageSize, "分页查询的单页总数必须大于零");

        Map<String, Object> map = new HashMap<>();
        Page page = PageHelper.startPage(start, pageSize);
        if (!StringUtils.isEmpty(searchConditionsDto.getName())) {
            map.put("name", searchConditionsDto.getName());
        }
        List<Permission> permissionsList;
        try {
            permissionsList = permissionDao.queryPermissionsByPage(map);
        } catch (Exception e) {
            logger.info("queryPermissionsByPage fail, map:{}, e:{}", map, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据条件分页查询权限记录失败");
        }
        if (permissionsList != null) {
            List<PermissionDto> permissionDtosList = new Page<>(start, pageSize, true);
            Page permissionDtosListPage = (Page) permissionDtosList;
            permissionDtosListPage.setTotal(page.getTotal());
            permissionDtosList = queryPermissionDtoList(permissionDtosList, permissionsList);
            return new PageInfo<>(permissionDtosList);
        }

        return null;
    }

    private void validateIsSameNamePermission(String name) {
        Permission permission;
        try {
            permission = permissionDao.selectByName(name);
        } catch (Exception e) {
            logger.info("selectByUsername fail, name:{}, e:{}", name, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据权限名查询权限记录失败");
        }
        if (permission != null) {
            throw new ServiceException(ErrorStateEnum.DB_EXIST_SAME_RECORD.getState(), "添加的权限名为 " + name + " 的权限记录已存在");
        }
    }

    private void updatePermission(Permission updatePermission, PermissionDto permissionDto) {
        if (!StringUtils.isEmpty(permissionDto.getName())) {
            updatePermission.setName(permissionDto.getName());
        }
        if (!StringUtils.isEmpty(permissionDto.getDescription())) {
            updatePermission.setDescription(permissionDto.getDescription());
        }
        updatePermission.setModifier(permissionDto.getModifier());
        updatePermission.setGmtModify(new Date());
    }

    private PermissionDto setPermissionDtoGmtCreateAndGmtModify(Permission permission) {
        PermissionDto newPermissionDto = permissionDto.convertFor(permission);
        newPermissionDto.setGmtCreate(DateUtil.formatDate(permission.getGmtCreate()));
        newPermissionDto.setGmtModify(DateUtil.formatDate(permission.getGmtModify()));
        return newPermissionDto;
    }

    private List<PermissionDto> queryPermissionDtoList(List<PermissionDto> permissionDtosList, List<Permission> permissionsList) {
        for (Permission permission : permissionsList) {
            permissionDtosList.add(setPermissionDtoGmtCreateAndGmtModify(permission));
        }
        return permissionDtosList;
    }
}
