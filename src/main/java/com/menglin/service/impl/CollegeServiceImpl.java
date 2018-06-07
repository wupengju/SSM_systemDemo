package com.menglin.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.menglin.dao.CollegeDao;
import com.menglin.dto.CollegeDto;
import com.menglin.dto.IdAndNameDto;
import com.menglin.dto.SearchConditionsDto;
import com.menglin.entity.College;
import com.menglin.enums.ErrorStateEnum;
import com.menglin.exception.ServiceException;
import com.menglin.redis.RedisKeys;
import com.menglin.redis.RedisUtil;
import com.menglin.service.CollegeService;
import com.menglin.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;

import static com.menglin.common.AssertArguments.*;

@Service("collegeService")
public class CollegeServiceImpl implements CollegeService {
    private Logger logger = LoggerFactory.getLogger(CollegeServiceImpl.class);

    @Resource
    private RedisUtil redisUtil;
    @Resource
    private CollegeDao collegeDao;
    @Resource
    private CollegeDto collegeDto;
    @Resource
    private IdAndNameDto idAndNameDto;

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public int addCollege(CollegeDto collegeDto) {
        checkNotNull(collegeDto, "学院不能为空");
        checkNotEmpty(collegeDto.getName(), "学院名字不能为空");
        checkNotEmpty(collegeDto.getCreator(), "学院创建者不能为空");
        checkNotEmpty(collegeDto.getDescription(), "学院描述不能为空");
        checkNotEmpty(collegeDto.getCode(), "学院代码不能为空");

        validateIsSameNameCollege(collegeDto.getName());
        College newCollege = collegeDto.convertToCollege();
        newCollege.setGmtCreate(new Date());
        int insertId;
        try {
            insertId = collegeDao.insertSelective(newCollege);
        } catch (Exception e) {
            logger.info("insert college fail, college:{}, e:{}", JSONObject.toJSONString(newCollege), e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "插入学院记录失败");
        }
        if (insertId > 0) {
            logger.info("insert college success, save college to redis, college:{}", JSONObject.toJSONString(newCollege));
            redisUtil.put(RedisKeys.COLLEGE_CACHE_KEY + newCollege.getId(), newCollege);
            return insertId;
        }

        return 0;
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public int updateCollege(CollegeDto collegeDto) {
        checkNotNull(collegeDto, "学院不能为空");
        checkNotEmpty(collegeDto.getModifier(), "学院修改者不能为空");
        checkGreaterThanZero(collegeDto.getId(), "学院 id 不能小于或等于零");

        College updateCollege;
        int updateId;
        try {
            updateCollege = collegeDao.selectByPrimaryKey(collegeDto.getId());
            updateCollege(updateCollege, collegeDto);
            updateId = collegeDao.updateByPrimaryKey(updateCollege);
        } catch (Exception e) {
            logger.info("update collegeDto fail, college:{}, e:{}", JSONObject.toJSONString(collegeDto.convertToCollege()), e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "修改学院记录失败");
        }
        if (updateId > 0) {
            logger.info("update college success, save college to redis, collegeDto:{}", JSONObject.toJSONString(updateCollege));
            redisUtil.del(RedisKeys.COLLEGE_CACHE_KEY + updateCollege.getId());
            redisUtil.put(RedisKeys.COLLEGE_CACHE_KEY + updateCollege.getId(), updateCollege);
            return updateId;
        }

        return 0;
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void deleteCollegeById(Long id) {
        checkGreaterThanZero(id, "学院 id 不能小于或等于零");

        int deleteId;
        try {
            deleteId = collegeDao.deleteByPrimaryKey(id);
        } catch (Exception e) {
            logger.info("delete collegeDto fail, id:{}, e:{}", id, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "删除学院记录失败");
        }
        if (deleteId > 0) {
            logger.info("delete collegeDto success, id:{}", deleteId);
            redisUtil.del(RedisKeys.COLLEGE_CACHE_KEY + deleteId);
        } else {
            logger.info("delete collegeDto fail, id:{}", id);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "删除学院记录不存在");
        }
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void batchDeleteCollegesByIds(String ids) {
        checkNotEmpty(ids, "ids 不能为空");

        String[] idsStr = ids.split(",");
        for (String anIdsStr : idsStr) {
            deleteCollegeById(Long.parseLong(anIdsStr));
        }
    }

    @Override
    public CollegeDto getCollegeById(Long id) {
        checkGreaterThanZero(id, "学院 id 不能小于或等于零");

        logger.info("get college by id:{}", id);
        College college = (College) redisUtil.get(RedisKeys.COLLEGE_CACHE_KEY + id, College.class);
        if (college != null) {
            logger.info("college in redis, college:{}", JSONObject.toJSONString(college));
            return setCollegeDtoGmtCreateAndGmtModify(college);
        }
        College collegeFromMysql;
        try {
            collegeFromMysql = collegeDao.selectByPrimaryKey(id);
        } catch (Exception e) {
            logger.info("get college by primary key fail, id:{}, e:{}", id, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据 id 查询学院记录失败");
        }
        if (collegeFromMysql != null) {
            logger.info("get college from mysql and save college to redis, college:{}", JSONObject.toJSONString(collegeFromMysql));
            redisUtil.put(RedisKeys.COLLEGE_CACHE_KEY + collegeFromMysql.getId(), collegeFromMysql);
            return setCollegeDtoGmtCreateAndGmtModify(collegeFromMysql);
        }

        return null;
    }

    @Override
    public CollegeDto getCollegeByName(String name) {
        checkNotEmpty(name, "学院名不能为空");

        logger.info("get college by name:{}", name);
        College college = (College) redisUtil.get(RedisKeys.COLLEGE_CACHE_KEY + name, College.class);
        if (college != null) {
            logger.info("college in redis, collegeDto:{}", college);
            return setCollegeDtoGmtCreateAndGmtModify(college);
        }
        College collegeFromMysql;
        try {
            collegeFromMysql = collegeDao.selectByName(name);
        } catch (Exception e) {
            logger.info("get college by primary key fail, name:{}, e:{}", name, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据学院名查询学院记录失败");
        }
        if (collegeFromMysql != null) {
            logger.info("get college from mysql and save college to redis, college:{}", collegeFromMysql);
            redisUtil.put(RedisKeys.COLLEGE_CACHE_KEY + collegeFromMysql.getName(), collegeFromMysql);
            return setCollegeDtoGmtCreateAndGmtModify(collegeFromMysql);
        }

        return null;
    }

    @Override
    public List<IdAndNameDto> getCollegeIdAndNameList() {
        List<College> collegeList = collegeDao.getCollegesList();
        List<IdAndNameDto> idAndNameDtoList = new ArrayList<>();
        for (College college : collegeList) {
            idAndNameDtoList.add(idAndNameDto.createNewIdAndNameDto(college.getId(), college.getName()));
        }
        return idAndNameDtoList;
    }

    @Override
    public PageInfo<CollegeDto> getCollegesByPage(int start, int pageSize, SearchConditionsDto searchConditionsDto) {
        checkNotNull(collegeDto, "学院不能为空");
        checkGreaterThanOrIsZero(start, "分页查询的开始数必须大于零");
        checkGreaterThanZero(pageSize, "分页查询的单页总数必须大于零");

        Map<String, Object> map = new HashMap<>();
        Page page = PageHelper.startPage(start, pageSize);
        if (!StringUtils.isEmpty(collegeDto.getName())) {
            map.put("name", collegeDto.getName());
        }
        List<College> collegesList;
        try {
            collegesList = collegeDao.queryCollegesByPage(map);
        } catch (Exception e) {
            logger.info("queryCollegesByPage fail, map:{}, e:{}", map, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据条件分页查询学院记录失败");
        }
        if (collegesList != null) {
            List<CollegeDto> collegeDtosList = new Page<>(start, pageSize, true);
            Page collegeDtosListPage = (Page) collegeDtosList;
            collegeDtosListPage.setTotal(page.getTotal());
            collegeDtosList = queryCollegeDtoList(collegeDtosList, collegesList);
            return new PageInfo<>(collegeDtosList);
        }

        return null;
    }

    private void validateIsSameNameCollege(String name) {
        College college;
        try {
            college = collegeDao.selectByName(name);
        } catch (Exception e) {
            logger.info("selectByUsername fail, name:{}, e:{}", name, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据学院名查询学院记录失败");
        }
        if (college != null) {
            throw new ServiceException(ErrorStateEnum.DB_EXIST_SAME_RECORD.getState(), "添加的学院名为 " + name + " 的学院记录已存在");
        }
    }

    private void updateCollege(College updateCollege, CollegeDto collegeDto) {
        if (!StringUtils.isEmpty(collegeDto.getName())) {
            updateCollege.setName(collegeDto.getName());
        }
        if (!StringUtils.isEmpty(collegeDto.getDescription())) {
            updateCollege.setDescription(collegeDto.getDescription());
        }
        if (!StringUtils.isEmpty(collegeDto.getCode())) {
            updateCollege.setCode(collegeDto.getCode());
        }
        updateCollege.setModifier(collegeDto.getModifier());
        updateCollege.setGmtModify(new Date());
    }

    private CollegeDto setCollegeDtoGmtCreateAndGmtModify(College college) {
        CollegeDto newCollegeDto = collegeDto.convertFor(college);
        newCollegeDto.setGmtCreate(DateUtil.formatDate(college.getGmtCreate()));
        newCollegeDto.setGmtModify(DateUtil.formatDate(college.getGmtModify()));
        return newCollegeDto;
    }

    private List<CollegeDto> queryCollegeDtoList(List<CollegeDto> collegeDtosList, List<College> collegesList) {
        for (College college : collegesList) {
            collegeDtosList.add(setCollegeDtoGmtCreateAndGmtModify(college));
        }
        return collegeDtosList;
    }
}