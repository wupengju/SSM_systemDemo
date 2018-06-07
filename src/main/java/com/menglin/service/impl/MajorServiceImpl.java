package com.menglin.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.menglin.dao.CollegeDao;
import com.menglin.dao.MajorDao;
import com.menglin.dto.IdAndNameDto;
import com.menglin.dto.MajorDto;
import com.menglin.dto.SearchConditionsDto;
import com.menglin.entity.College;
import com.menglin.entity.Major;
import com.menglin.enums.ErrorStateEnum;
import com.menglin.exception.ServiceException;
import com.menglin.redis.RedisKeys;
import com.menglin.redis.RedisUtil;
import com.menglin.service.MajorService;
import com.menglin.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;

import static com.menglin.common.AssertArguments.*;

@Service("majorService")
public class MajorServiceImpl implements MajorService {
    private Logger logger = LoggerFactory.getLogger(MajorServiceImpl.class);

    @Resource
    private RedisUtil redisUtil;
    @Resource
    private MajorDao majorDao;
    @Resource
    private CollegeDao collegeDao;
    @Resource
    private MajorDto majorDto;
    @Resource
    private IdAndNameDto idAndNameDto;

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public int addMajor(MajorDto majorDto) {
        checkNotNull(majorDto, "专业不能为空");
        checkNotEmpty(majorDto.getName(), "专业名字不能为空");
        checkNotEmpty(majorDto.getCreator(), "专业创建者不能为空");
        checkNotEmpty(majorDto.getCode(), "专业代码不能为空");
        checkNotEmpty(majorDto.getCategory(), "专业分类不能为空");
        checkGreaterThanZero(majorDto.getCollegeId(), "学院 id 不能小于或等于零");

        validateIsSameNameMajor(majorDto.getName());
        Major newMajor = majorDto.convertToMajor();
        newMajor.setGmtCreate(new Date());
        int insertId;
        try {
            insertId = majorDao.insertSelective(newMajor);
        } catch (Exception e) {
            logger.info("insert major fail, major:{}, e:{}", JSONObject.toJSONString(newMajor), e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "插入专业记录失败");
        }
        if (insertId > 0) {
            logger.info("insert major success, save major to redis, major:{}", JSONObject.toJSONString(newMajor));
            redisUtil.put(RedisKeys.MAJOR_CACHE_KEY + newMajor.getId(), newMajor);
            return insertId;
        }

        return 0;
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public int updateMajor(MajorDto majorDto) {
        checkNotNull(majorDto, "专业不能为空");
        checkNotEmpty(majorDto.getModifier(), "专业修改者不能为空");
        checkGreaterThanZero(majorDto.getId(), "专业 id 不能小于或等于零");

        Major updateMajor;
        int updateId;
        try {
            updateMajor = majorDao.selectByPrimaryKey(majorDto.getId());
            updateMajor(updateMajor, majorDto);
            updateId = majorDao.updateByPrimaryKey(updateMajor);
        } catch (Exception e) {
            logger.info("update major fail, major:{}, e:{}", JSONObject.toJSONString(majorDto.convertToMajor()), e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "修改专业记录失败");
        }
        if (updateId > 0) {
            logger.info("update major success, save major to redis, major:{}", JSONObject.toJSONString(updateMajor));
            redisUtil.del(RedisKeys.MAJOR_CACHE_KEY + updateMajor.getId());
            redisUtil.put(RedisKeys.MAJOR_CACHE_KEY + updateMajor.getId(), updateMajor);
            return updateId;
        }

        return 0;
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void deleteMajorById(Long id) {
        checkGreaterThanZero(id, "专业 id 不能小于或等于零");

        int deleteId;
        try {
            deleteId = majorDao.deleteByPrimaryKey(id);
        } catch (Exception e) {
            logger.info("delete major fail, id:{}, e:{}", id, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "删除专业记录失败");
        }
        if (deleteId > 0) {
            logger.info("delete major success, id:{}", deleteId);
            redisUtil.del(RedisKeys.MAJOR_CACHE_KEY + deleteId);
        } else {
            logger.info("delete major fail, id:{}", id);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "删除专业记录不存在");
        }
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void batchDeleteMajorsByIds(String ids) {
        checkNotEmpty(ids, "ids 不能为空");

        String[] idsStr = ids.split(",");
        for (String anIdsStr : idsStr) {
            deleteMajorById(Long.parseLong(anIdsStr));
        }
    }

    @Override
    public MajorDto getMajorById(Long id) {
        checkGreaterThanZero(id, "专业 id 不能小于或等于零");

        logger.info("get major by id:{}", id);
        Major major = (Major) redisUtil.get(RedisKeys.MAJOR_CACHE_KEY + id, Major.class);
        if (major != null) {
            logger.info("major in redis, major:{}", JSONObject.toJSONString(major));
            return setMajorDtoGmtCreateAndGmtModify(setMajorDtoNameInfo(major), major);
        }
        Major majorFromMysql;
        try {
            majorFromMysql = majorDao.selectByPrimaryKey(id);
        } catch (Exception e) {
            logger.info("get major by primary key fail, id:{}, e:{}", id, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据 id 查询专业记录失败");
        }
        if (majorFromMysql != null) {
            logger.info("get major from mysql and save major to redis, major:{}", JSONObject.toJSONString(majorFromMysql));
            redisUtil.put(RedisKeys.MAJOR_CACHE_KEY + majorFromMysql.getId(), majorFromMysql);
            return setMajorDtoGmtCreateAndGmtModify(setMajorDtoNameInfo(majorFromMysql), majorFromMysql);
        }

        return null;
    }

    @Override
    public MajorDto getMajorByName(String name) {
        checkNotEmpty(name, "专业名不能为空");

        logger.info("get major by name:{}", name);
        Major major = (Major) redisUtil.get(RedisKeys.MAJOR_CACHE_KEY + name, Major.class);
        if (major != null) {
            logger.info("major in redis, major:{}", major);
            return setMajorDtoGmtCreateAndGmtModify(setMajorDtoNameInfo(major), major);
        }
        Major majorFromMysql;
        try {
            majorFromMysql = majorDao.selectByName(name);
        } catch (Exception e) {
            logger.info("get major by primary key fail, name:{}, e:{}", name, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据专业名查询专业记录失败");
        }
        if (majorFromMysql != null) {
            logger.info("get major from mysql and save major to redis, major:{}", majorFromMysql);
            redisUtil.put(RedisKeys.MAJOR_CACHE_KEY + majorFromMysql.getName(), majorFromMysql);
            return setMajorDtoGmtCreateAndGmtModify(setMajorDtoNameInfo(majorFromMysql), majorFromMysql);
        }

        return null;
    }

    @Override
    public List<IdAndNameDto> getMajorIdAndNameList(Long collegeId) {
        checkGreaterThanZero(collegeId, "学院 id 不能小于或等于零");

        List<Major> majorsList = majorDao.getMajorsList(collegeId);
        List<IdAndNameDto> idAndNameDtoList = new ArrayList<>();
        for (Major major : majorsList) {
            idAndNameDtoList.add(idAndNameDto.createNewIdAndNameDto(major.getId(), major.getName()));
        }
        return idAndNameDtoList;
    }

    @Override
    public PageInfo<MajorDto> getMajorsByPage(int start, int pageSize, SearchConditionsDto searchConditionsDto) {
        checkNotNull(searchConditionsDto, "分页查询的条件对象不能为空");
        checkGreaterThanOrIsZero(start, "分页查询的开始数必须大于零");
        checkGreaterThanZero(pageSize, "分页查询的单页总数必须大于零");

        Map<String, Object> map = new HashMap<>();
        Page page = PageHelper.startPage(start, pageSize);
        if (!StringUtils.isEmpty(searchConditionsDto.getName())) {
            map.put("name", searchConditionsDto.getName());
        }
        List<Major> majorsList;
        try {
            majorsList = majorDao.queryMajorsByPage(map);
        } catch (Exception e) {
            logger.info("queryMajorsByPage fail, map:{}, e:{}", map, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据条件分页查询专业记录失败");
        }
        if (majorsList != null) {
            List<MajorDto> majorDtosList = new Page<>(start, pageSize, true);
            Page majorDtosListPage = (Page) majorDtosList;
            majorDtosListPage.setTotal(page.getTotal());
            majorDtosList = queryMajorDtoList(majorDtosList, majorsList);
            return new PageInfo<>(majorDtosList);
        }

        return null;
    }

    private void validateIsSameNameMajor(String name) {
        Major major;
        try {
            major = majorDao.selectByName(name);
        } catch (Exception e) {
            logger.info("selectByUsername fail, name:{}, e:{}", name, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据专业名查询专业记录失败");
        }
        if (major != null) {
            throw new ServiceException(ErrorStateEnum.DB_EXIST_SAME_RECORD.getState(), "添加的专业名为 " + name + " 的专业记录已存在");
        }
    }

    private void updateMajor(Major updateMajor, MajorDto majorDto) {
        if (!StringUtils.isEmpty(majorDto.getName())) {
            updateMajor.setName(majorDto.getName());
        }
        if (!StringUtils.isEmpty(majorDto.getCode())) {
            updateMajor.setCode(majorDto.getCode());
        }
        if (!StringUtils.isEmpty(majorDto.getCategory())) {
            updateMajor.setCategory(majorDto.getCategory());
        }
        if (majorDto.getCollegeId() > 0) {
            updateMajor.setCollegeId(majorDto.getCollegeId());
        }
        updateMajor.setModifier(majorDto.getModifier());
        updateMajor.setGmtModify(new Date());
    }

    private String getCollegeName(Long id, List<College> collegeList) {
        String name = "";
        for (College college : collegeList) {
            if (college.getId() == id) {
                name = college.getName();
                break;
            }
        }

        return name;
    }

    private MajorDto setMajorDtoNameInfo(Major major) {
        MajorDto queryMajorDto = majorDto.convertFor(major);
        College college = collegeDao.selectByPrimaryKey(major.getCollegeId());
        queryMajorDto.setCollegeName(college.getName());
        return queryMajorDto;
    }

    private MajorDto setMajorDtoGmtCreateAndGmtModify(MajorDto newMajorDto, Major major) {
        newMajorDto.setGmtCreate(DateUtil.formatDate(major.getGmtCreate()));
        newMajorDto.setGmtModify(DateUtil.formatDate(major.getGmtModify()));
        return newMajorDto;
    }

    private List<MajorDto> queryMajorDtoList(List<MajorDto> majorDtosList, List<Major> majorsList) {
        List<Long> collegeIdList = new ArrayList<>();
        for (Major major : majorsList) {
            MajorDto newMajorDto = majorDto.convertFor(major);
            collegeIdList.add(major.getCollegeId());
            newMajorDto = setMajorDtoGmtCreateAndGmtModify(newMajorDto, major);
            majorDtosList.add(newMajorDto);
        }
        Map<String, Object> connectedQueryMap = new HashMap<>();
        connectedQueryMap.put("collegeIdList", collegeIdList);
        List<College> collegeList = collegeDao.queryCollegesByIdList(connectedQueryMap);
        for (MajorDto curMajorDto : majorDtosList) {
            curMajorDto.setCollegeName(getCollegeName(curMajorDto.getCollegeId(), collegeList));
        }
        return majorDtosList;
    }
}