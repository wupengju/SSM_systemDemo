package com.menglin.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.menglin.common.CommonConst;
import com.menglin.dao.AdminDao;
import com.menglin.dao.RoleDao;
import com.menglin.dto.AdminDto;
import com.menglin.dto.SearchConditionsDto;
import com.menglin.entity.Admin;
import com.menglin.entity.Role;
import com.menglin.enums.ErrorStateEnum;
import com.menglin.exception.ServiceException;
import com.menglin.service.AdminService;
import com.menglin.util.DateUtil;
import com.menglin.util.MD5Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;

import static com.menglin.common.AssertArguments.*;

@Service("adminService")
public class AdminServiceImpl implements AdminService {
    private Logger logger = LoggerFactory.getLogger(AdminServiceImpl.class);

    @Resource
    private AdminDao adminDao;
    @Resource
    private RoleDao roleDao;
    @Resource
    private AdminDto adminDto;

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public int addAdmin(AdminDto adminDto) {
        checkNotNull(adminDto, "管理员不能为空");
        checkNotEmpty(adminDto.getUsername(), "管理员用户名不能为空");
        checkNotEmpty(adminDto.getPassword(), "管理员密码不能为空");

        validateIsSameUsernameAdmin(adminDto.getUsername());

        Admin newAdmin = adminDto.convertToAdmin();
        newAdmin.setPassword(encryptionPassword(newAdmin.getPassword()));
        // 添加管理员的默认角色
        newAdmin.setRoleId(1L);
        newAdmin.setGmtCreate(new Date());
        int insertId;
        try {
            insertId = adminDao.insert(newAdmin);
        } catch (Exception e) {
            logger.info("insert admin fail, admin:{}, e:{}", JSONObject.toJSONString(newAdmin), e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "插入管理员记录失败");
        }
        if (insertId > 0) {
            logger.info("insert admin success, admin:{}", JSONObject.toJSONString(newAdmin));
            return insertId;
        }

        return 0;
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public int updateAdmin(AdminDto adminDto) {
        checkNotNull(adminDto, "管理员不能为空");
        checkGreaterThanZero(adminDto.getId(), "管理员 id 不能小于或等于零");

        Admin updateAdmin;
        int updateId;
        try {
            updateAdmin = adminDao.selectByPrimaryKey(adminDto.getId());
            updateAdmin(updateAdmin, adminDto);
            updateId = adminDao.updateByPrimaryKey(updateAdmin);
        } catch (Exception e) {
            logger.info("update admin fail, admin:{}, e:{}", JSONObject.toJSONString(adminDto.convertToAdmin()), e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "修改管理员记录失败");
        }
        if (updateId > 0) {
            logger.info("update admin success, admin:{}", JSONObject.toJSONString(updateAdmin));
            return updateId;
        }

        return 0;
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void deleteAdminById(Long id) {
        checkGreaterThanZero(id, "管理员 id 不能小于或等于零");

        int deleteId;
        try {
            deleteId = adminDao.deleteByPrimaryKey(id);
        } catch (Exception e) {
            logger.info("delete admin fail, id:{}, e:{}", id, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "删除管理员记录失败");
        }
        if (deleteId > 0) {
            logger.info("delete admin success, id:{}", deleteId);
        } else {
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "删除管理员记录不存在");
        }
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void batchDeleteAdminsByIds(String ids) {
        checkNotEmpty(ids, "ids 不能为空");

        String[] idsStr = ids.split(",");
        for (String anIdsStr : idsStr) {
            deleteAdminById(Long.parseLong(anIdsStr));
        }
    }

    @Override
    public AdminDto getAdminById(Long id) {
        checkGreaterThanZero(id, "管理员 id 不能小于或等于零");

        logger.info("get admin by id:{}", id);
        Admin adminFromMysql;
        try {
            adminFromMysql = adminDao.selectByPrimaryKey(id);
        } catch (Exception e) {
            logger.info("get admin by primary key fail, id:{}, e:{}", id, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据 id 查询管理员记录失败");
        }
        if (adminFromMysql != null) {
            logger.info("get admin from mysql, admin:{}", JSONObject.toJSONString(adminFromMysql));
            return setAdminDtoGmtCreateAndGmtModify(setAdminDtoNameInfo(adminFromMysql), adminFromMysql);
        }

        return null;
    }

    @Override
    public AdminDto getAdminByUsername(String username) {
        checkNotEmpty(username, "管理员用户名不能为空");

        logger.info("get admin by username:{}", username);
        Admin adminFromMysql;
        try {
            adminFromMysql = adminDao.selectByUsername(username);
        } catch (Exception e) {
            logger.info("get admin by primary key fail, username:{}, e:{}", username, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据 username 查询管理员记录失败");
        }
        if (adminFromMysql != null) {
            logger.info("get admin from mysql, admin:{}", JSONObject.toJSONString(adminFromMysql));
            return setAdminDtoGmtCreateAndGmtModify(setAdminDtoNameInfo(adminFromMysql), adminFromMysql);
        }

        return null;
    }

    @Override
    public PageInfo<AdminDto> getAdminsByPage(int start, int pageSize, SearchConditionsDto searchConditionsDto) {
        checkNotNull(searchConditionsDto, "分页查询的条件对象不能为空");
        checkGreaterThanOrIsZero(start, "分页查询的开始数必须大于零");
        checkGreaterThanZero(pageSize, "分页查询的单页总数必须大于零");

        Map<String, Object> map = new HashMap<>();
        Page page = PageHelper.startPage(start, pageSize);
        if (!StringUtils.isEmpty(searchConditionsDto.getUsername())) {
            map.put("username", searchConditionsDto.getUsername());
        }
        List<Admin> adminsList;
        try {
            adminsList = adminDao.queryAdminsByPage(map);
        } catch (Exception e) {
            logger.info("queryAdminsByPage fail, map:{}, e:{}", map, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据条件分页查询管理员记录失败");
        }
        if (adminsList != null) {
            List<AdminDto> adminDtosList = new Page<>(start, pageSize, true);
            Page adminDtosListPage = (Page) adminDtosList;
            adminDtosListPage.setTotal(page.getTotal());
            List<Long> roleIdList = new ArrayList<>();
            for (Admin admin : adminsList) {
                AdminDto newAdminDto = adminDto.convertFor(admin);
                roleIdList.add(admin.getRoleId());
                newAdminDto = setAdminDtoGmtCreateAndGmtModify(newAdminDto, admin);
                adminDtosList.add(newAdminDto);
            }
            Map<String, Object> connectedQueryMap = new HashMap<>();
            connectedQueryMap.put("roleIdList", roleIdList);
            List<Role> roleList = roleDao.queryRolesByIdList(connectedQueryMap);
            for (AdminDto curAdminDto : adminDtosList) {
                curAdminDto.setRoleName(getRoleName(curAdminDto.getRoleId(), roleList));
            }
            return new PageInfo<>(adminDtosList);
        }

        return null;
    }

    private void validateIsSameUsernameAdmin(String username) {
        Admin admin;
        try {
            admin = adminDao.selectByUsername(username);
        } catch (Exception e) {
            logger.info("selectByUsername fail, username:{}, e:{}", username, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据用户名查询管理员记录失败");
        }
        if (admin != null) {
            throw new ServiceException(ErrorStateEnum.DB_EXIST_SAME_RECORD.getState(), "添加的用户名为 " + username + " 的管理员记录已存在");
        }
    }

    private void updateAdmin(Admin updateAdmin, AdminDto adminDto) {
        if (adminDto.getRoleId() > 0) {
            updateAdmin.setRoleId(adminDto.getRoleId());
        }
        if (!StringUtils.isEmpty(adminDto.getUsername())) {
            updateAdmin.setUsername(adminDto.getUsername());
        }
        if (!StringUtils.isEmpty(adminDto.getPassword())) {
            updateAdmin.setPassword(encryptionPassword(adminDto.getPassword()));
        }
        updateAdmin.setGmtModify(new Date());
    }

    private String getRoleName(Long id, List<Role> roleList) {
        String name = "";
        for (Role role : roleList) {
            if (role.getId() == id) {
                name = role.getName();
                break;
            }
        }

        return name;
    }

    private AdminDto setAdminDtoNameInfo(Admin admin) {
        AdminDto queryAdminDto = adminDto.convertFor(admin);
        Role role = roleDao.selectByPrimaryKey(admin.getRoleId());
        queryAdminDto.setRoleName(role.getName());
        return queryAdminDto;
    }

    private AdminDto setAdminDtoGmtCreateAndGmtModify(AdminDto newAdminDto, Admin admin) {
        newAdminDto.setGmtCreate(DateUtil.formatDate(admin.getGmtCreate()));
        newAdminDto.setGmtModify(DateUtil.formatDate(admin.getGmtModify()));
        return newAdminDto;
    }

    private String encryptionPassword(String password) {
        return MD5Util.MD5Encode(password + CommonConst.SERVER_SALT, "");
    }
}
