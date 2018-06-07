package com.menglin.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.menglin.dao.AdminDao;
import com.menglin.dao.NoticeDao;
import com.menglin.dto.NoticeDto;
import com.menglin.dto.SearchConditionsDto;
import com.menglin.entity.Admin;
import com.menglin.entity.Notice;
import com.menglin.enums.ErrorStateEnum;
import com.menglin.exception.ServiceException;
import com.menglin.redis.RedisKeys;
import com.menglin.redis.RedisUtil;
import com.menglin.service.NoticeService;
import com.menglin.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;

import static com.menglin.common.AssertArguments.*;

@Service("noticeService")
public class NoticeServiceImpl implements NoticeService {
    private Logger logger = LoggerFactory.getLogger(NoticeServiceImpl.class);

    @Resource
    private RedisUtil redisUtil;
    @Resource
    private NoticeDao noticeDao;
    @Resource
    private AdminDao adminDao;
    @Resource
    private NoticeDto noticeDto;

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public int addNotice(NoticeDto noticeDto) {
        checkNotNull(noticeDto, "公告不能为空");
        checkNotEmpty(noticeDto.getName(), "公告名字不能为空");
        checkNotEmpty(noticeDto.getCreator(), "公告创建者不能为空");
        checkNotEmpty(noticeDto.getContent(), "公告内容不能为空");
        checkGreaterThanZero(noticeDto.getAdminId(), "管理员 id 不能小于或等于零");

        validateIsSameNameNotice(noticeDto.getName());
        Notice newNotice = noticeDto.convertToNotice();
        newNotice.setGmtCreate(new Date());
        int insertId;
        try {
            insertId = noticeDao.insertSelective(newNotice);
        } catch (Exception e) {
            logger.info("insert notice fail, notice:{}, e:{}", JSONObject.toJSONString(noticeDto.convertToNotice()), e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "插入公告记录失败");
        }
        if (insertId > 0) {
            logger.info("insert notice success, save notice to redis, notice:{}", JSONObject.toJSONString(newNotice));
            redisUtil.put(RedisKeys.NOTICE_CACHE_KEY + newNotice.getId(), newNotice);
            return insertId;
        }

        return 0;
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public int updateNotice(NoticeDto noticeDto) {
        checkNotNull(noticeDto, "公告不能为空");
        checkNotEmpty(noticeDto.getModifier(), "公告修改者不能为空");
        checkGreaterThanZero(noticeDto.getId(), "公告 id 不能小于或等于零");

        Notice updateNotice;
        int updateId;
        try {
            updateNotice = noticeDao.selectByPrimaryKey(noticeDto.getId());
            updateNotice(updateNotice, noticeDto);
            updateId = noticeDao.updateByPrimaryKey(updateNotice);
        } catch (Exception e) {
            logger.info("update notice fail, notice:{}, e:{}", JSONObject.toJSONString(noticeDto.convertToNotice()), e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "修改公告记录失败");
        }
        if (updateId > 0) {
            logger.info("update notice success, save notice to redis, notice:{}", JSONObject.toJSONString(updateNotice));
            redisUtil.del(RedisKeys.NOTICE_CACHE_KEY + updateNotice.getId());
            redisUtil.put(RedisKeys.NOTICE_CACHE_KEY + updateNotice.getId(), updateNotice);
            return updateId;
        }

        return 0;
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void deleteNoticeById(Long id) {
        checkGreaterThanZero(id, "公告 id 不能小于或等于零");

        int deleteId;
        try {
            deleteId = noticeDao.deleteByPrimaryKey(id);
        } catch (Exception e) {
            logger.info("delete notice fail, id:{}, e:{}", id, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "删除公告记录失败");
        }
        if (deleteId > 0) {
            logger.info("delete notice success, id:{}", deleteId);
            redisUtil.del(RedisKeys.NOTICE_CACHE_KEY + deleteId);
        } else {
            logger.info("delete notice fail, id:{}", id);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "删除公告记录不存在");
        }
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void batchDeleteNoticesByIds(String ids) {
        checkNotEmpty(ids, "ids 不能为空");

        String[] idsStr = ids.split(",");
        for (String anIdsStr : idsStr) {
            deleteNoticeById(Long.parseLong(anIdsStr));
        }
    }

    @Override
    public NoticeDto getNoticeById(Long id) {
        checkGreaterThanZero(id, "公告 id 不能小于或等于零");

        logger.info("get notice by id:{}", id);
        Notice notice = (Notice) redisUtil.get(RedisKeys.NOTICE_CACHE_KEY + id, Notice.class);
        if (notice != null) {
            logger.info("notice in redis, notice:{}", JSONObject.toJSONString(notice));
            return setNoticeDtoGmtCreateAndGmtModify(setNoticeDtoNameInfo(notice), notice);
        }
        Notice noticeFromMysql;
        try {
            noticeFromMysql = noticeDao.selectByPrimaryKey(id);
        } catch (Exception e) {
            logger.info("get notice by primary key fail, id:{}, e:{}", id, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据 id 查询公告记录失败");
        }
        if (noticeFromMysql != null) {
            logger.info("get notice from mysql and save notice to redis, notice:{}", JSONObject.toJSONString(noticeFromMysql));
            redisUtil.put(RedisKeys.NOTICE_CACHE_KEY + noticeFromMysql.getId(), noticeFromMysql);
            return setNoticeDtoGmtCreateAndGmtModify(setNoticeDtoNameInfo(noticeFromMysql), noticeFromMysql);
        }

        return null;
    }

    @Override
    public NoticeDto getNoticeByName(String name) {
        checkNotEmpty(name, "公告名不能为空");

        logger.info("get notice by name:{}", name);
        Notice notice = (Notice) redisUtil.get(RedisKeys.NOTICE_CACHE_KEY + name, Notice.class);
        if (notice != null) {
            logger.info("notice in redis, notice:{}", notice);
            return setNoticeDtoGmtCreateAndGmtModify(setNoticeDtoNameInfo(notice), notice);
        }
        Notice noticeFromMysql;
        try {
            noticeFromMysql = noticeDao.selectByName(name);
        } catch (Exception e) {
            logger.info("get notice by primary key fail, name:{}, e:{}", name, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据公告名查询公告记录失败");
        }
        if (noticeFromMysql != null) {
            logger.info("get notice from mysql and save notice to redis, notice:{}", noticeFromMysql);
            redisUtil.put(RedisKeys.NOTICE_CACHE_KEY + noticeFromMysql.getName(), noticeFromMysql);
            return setNoticeDtoGmtCreateAndGmtModify(setNoticeDtoNameInfo(noticeFromMysql), noticeFromMysql);
        }

        return null;
    }

    @Override
    public PageInfo<NoticeDto> getNoticesByPage(int start, int pageSize, SearchConditionsDto searchConditionsDto) {
        checkNotNull(searchConditionsDto, "分页查询的条件对象不能为空");
        checkGreaterThanOrIsZero(start, "分页查询的开始数必须大于零");
        checkGreaterThanZero(pageSize, "分页查询的单页总数必须大于零");

        Map<String, Object> map = new HashMap<>();
        Page page = PageHelper.startPage(start, pageSize);
        if (!StringUtils.isEmpty(searchConditionsDto.getName())) {
            map.put("name", searchConditionsDto.getName());
        }
        List<Notice> noticesList;
        try {
            noticesList = noticeDao.queryNoticesByPage(map);
        } catch (Exception e) {
            logger.info("queryNoticesByPage fail, map:{}, e:{}", map, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据条件分页查询公告记录失败");
        }
        if (noticesList != null) {
            List<NoticeDto> noticeDtosList = new Page<>(start, pageSize, true);
            Page noticeDtosListPage = (Page) noticeDtosList;
            noticeDtosListPage.setTotal(page.getTotal());
            noticeDtosList = queryNoticeDtoList(noticeDtosList, noticesList);
            return new PageInfo<>(noticeDtosList);
        }

        return null;
    }

    private void validateIsSameNameNotice(String name) {
        Notice notice;
        try {
            notice = noticeDao.selectByName(name);
        } catch (Exception e) {
            logger.info("selectByUsername fail, name:{}, e:{}", name, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据公告名查询公告记录失败");
        }
        if (notice != null) {
            throw new ServiceException(ErrorStateEnum.DB_EXIST_SAME_RECORD.getState(), "添加的公告名为 " + name + " 的公告记录已存在");
        }
    }

    private void updateNotice(Notice updateNotice, NoticeDto noticeDto) {
        if (!StringUtils.isEmpty(noticeDto.getName())) {
            updateNotice.setName(noticeDto.getName());
        }
        if (!StringUtils.isEmpty(noticeDto.getContent())) {
            updateNotice.setContent(noticeDto.getContent());
        }
        if (!StringUtils.isEmpty(noticeDto.getUrl())) {
            updateNotice.setUrl(noticeDto.getUrl());
        }
        if (noticeDto.getAdminId() > 0) {
            updateNotice.setAdminId(noticeDto.getAdminId());
        }
        updateNotice.setModifier(noticeDto.getModifier());
        updateNotice.setGmtModify(new Date());
    }

    private NoticeDto setNoticeDtoNameInfo(Notice notice) {
        NoticeDto queryNoticeDto = noticeDto.convertFor(notice);
        Admin admin = adminDao.selectByPrimaryKey(notice.getAdminId());
        queryNoticeDto.setAdminUserName(admin.getUsername());
        return queryNoticeDto;
    }

    private NoticeDto setNoticeDtoGmtCreateAndGmtModify(NoticeDto newNoticeDto, Notice notice) {
        newNoticeDto.setGmtCreate(DateUtil.formatDate(notice.getGmtCreate()));
        newNoticeDto.setGmtModify(DateUtil.formatDate(notice.getGmtModify()));
        return newNoticeDto;
    }

    private String getAdminUsername(Long id, List<Admin> adminList) {
        String username = "";
        for (Admin admin : adminList) {
            if (admin.getId() == id) {
                username = admin.getUsername();
                break;
            }
        }

        return username;
    }

    private List<NoticeDto> queryNoticeDtoList(List<NoticeDto> noticeDtosList, List<Notice> noticesList) {
        List<Long> adminIdList = new ArrayList<>();
        for (Notice notice : noticesList) {
            adminIdList.add(notice.getAdminId());
            noticeDtosList.add(setNoticeDtoGmtCreateAndGmtModify(noticeDto.convertFor(notice), notice));
        }
        Map<String, Object> connectedQueryMap = new HashMap<>();
        connectedQueryMap.put("adminIdList", adminIdList);
        List<Admin> adminList = adminDao.queryAdminsByIdList(connectedQueryMap);
        for (NoticeDto curNoticeDto : noticeDtosList) {
            curNoticeDto.setAdminUserName(getAdminUsername(curNoticeDto.getAdminId(), adminList));
        }
        return noticeDtosList;
    }
}