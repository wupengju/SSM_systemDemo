package com.menglin.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.menglin.dao.ClassTeamDao;
import com.menglin.dao.CollegeDao;
import com.menglin.dao.MajorDao;
import com.menglin.dto.ClassTeamDto;
import com.menglin.dto.IdAndNameDto;
import com.menglin.dto.SearchConditionsDto;
import com.menglin.entity.ClassTeam;
import com.menglin.entity.College;
import com.menglin.entity.Major;
import com.menglin.enums.ErrorStateEnum;
import com.menglin.exception.ServiceException;
import com.menglin.redis.RedisKeys;
import com.menglin.redis.RedisUtil;
import com.menglin.service.ClassTeamService;
import com.menglin.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;

import static com.menglin.common.AssertArguments.*;

@Service("classTeamService")
public class ClassTeamServiceImpl implements ClassTeamService {
    private Logger logger = LoggerFactory.getLogger(ClassTeamServiceImpl.class);

    @Resource
    private RedisUtil redisUtil;
    @Resource
    private ClassTeamDao classTeamDao;
    @Resource
    private CollegeDao collegeDao;
    @Resource
    private MajorDao majorDao;
    @Resource
    private ClassTeamDto classTeamDto;
    @Resource
    private IdAndNameDto idAndNameDto;

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public int addClassTeam(ClassTeamDto classTeamDto) {
        checkNotNull(classTeamDto, "班级不能为空");
        checkNotEmpty(classTeamDto.getCreator(), "班级创建者不能为空");
        validateArgumentsClassTeamAdd(classTeamDto);

        validateIsSameNameClassTeam(classTeamDto.getName());
        ClassTeam newClassTeam = classTeamDto.convertToClassTeam();
        newClassTeam.setGmtCreate(new Date());
        int insertId;
        try {
            insertId = classTeamDao.insertSelective(newClassTeam);
        } catch (Exception e) {
            logger.info("insert classTeam fail, classTeam:{}, e:{}", JSONObject.toJSONString(newClassTeam), e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "插入班级记录失败");
        }
        if (insertId > 0) {
            logger.info("insert classTeam success, classTeam:{}", JSONObject.toJSONString(newClassTeam));
            redisUtil.put(RedisKeys.CLASS_TEAM_CACHE_KEY + newClassTeam.getId(), newClassTeam);
            return insertId;
        }

        return 0;
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public int updateClassTeam(ClassTeamDto classTeamDto) {
        checkNotNull(classTeamDto, "班级不能为空");
        checkNotEmpty(classTeamDto.getModifier(), "班级修改者不能为空");
        checkGreaterThanZero(classTeamDto.getId(), "班级 id 不能小于或等于零");

        ClassTeam updateClassTeam;
        int updateId;
        try {
            updateClassTeam = classTeamDao.selectByPrimaryKey(classTeamDto.getId());
            updateClassTeam(updateClassTeam, classTeamDto);
            updateId = classTeamDao.updateByPrimaryKey(updateClassTeam);
        } catch (Exception e) {
            logger.info("update classTeam fail, classTeam:{}, e:{}", JSONObject.toJSONString(classTeamDto.convertToClassTeam()), e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "修改班级记录失败");
        }
        if (updateId > 0) {
            logger.info("update classTeam success, save classTeam to redis, classTeam:{}", JSONObject.toJSONString(updateClassTeam));
            redisUtil.del(RedisKeys.CLASS_TEAM_CACHE_KEY + updateClassTeam.getId());
            redisUtil.put(RedisKeys.CLASS_TEAM_CACHE_KEY + updateClassTeam.getId(), updateClassTeam);
            return updateId;
        }

        return 0;
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void deleteClassTeamById(Long id) {
        checkGreaterThanZero(id, "班级 id 不能小于或等于零");

        int deleteId;
        try {
            deleteId = classTeamDao.deleteByPrimaryKey(id);
        } catch (Exception e) {
            logger.info("delete classTeam fail, id:{}, e:{}", id, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "删除班级记录失败");
        }
        if (deleteId > 0) {
            logger.info("delete classTeam success, id:{}", deleteId);
            redisUtil.del(RedisKeys.CLASS_TEAM_CACHE_KEY + deleteId);
        } else {
            logger.info("delete classTeam fail, id:{}", id);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "删除班级记录不存在");
        }
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void batchDeleteClassTeamsByIds(String ids) {
        checkNotEmpty(ids, "ids 不能为空");

        String[] idsStr = ids.split(",");
        for (String anIdsStr : idsStr) {
            deleteClassTeamById(Long.parseLong(anIdsStr));
        }
    }

    @Override
    public ClassTeamDto getClassTeamById(Long id) {
        checkGreaterThanZero(id, "作业 id 不能小于或等于零");

        logger.info("get classTeam by id:{}", id);
        ClassTeam classTeam = (ClassTeam) redisUtil.get(RedisKeys.CLASS_TEAM_CACHE_KEY + id, ClassTeam.class);
        if (classTeam != null) {
            logger.info("classTeam in redis, classTeam:{}", JSONObject.toJSONString(classTeam));
            return setClassTeamDtoGmtCreateAndGmtModify(setClassTeamDtoNameInfo(classTeam), classTeam);
        }
        ClassTeam classTeamFromMysql;
        try {
            classTeamFromMysql = classTeamDao.selectByPrimaryKey(id);
        } catch (Exception e) {
            logger.info("get classTeam by primary key fail, id:{}, e:{}", id, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据 id 查询班级记录失败");
        }
        if (classTeamFromMysql != null) {
            logger.info("get classTeam from mysql and save classTeam to redis, classTeam:{}", JSONObject.toJSONString(classTeamFromMysql));
            redisUtil.put(RedisKeys.CLASS_TEAM_CACHE_KEY + classTeamFromMysql.getId(), classTeamFromMysql);
            return setClassTeamDtoGmtCreateAndGmtModify(setClassTeamDtoNameInfo(classTeamFromMysql), classTeamFromMysql);
        }

        return null;
    }

    @Override
    public ClassTeamDto getClassTeamByName(String name) {
        checkNotEmpty(name, "班级名不能为空");

        logger.info("get classTeam by username:{}", name);
        ClassTeam classTeam = (ClassTeam) redisUtil.get(RedisKeys.CLASS_TEAM_CACHE_KEY + name, ClassTeam.class);
        if (classTeam != null) {
            logger.info("classTeam in redis, classTeam:{}", classTeam);
            return setClassTeamDtoGmtCreateAndGmtModify(setClassTeamDtoNameInfo(classTeam), classTeam);
        }
        ClassTeam classTeamFromMysql;
        try {
            classTeamFromMysql = classTeamDao.selectByName(name);
        } catch (Exception e) {
            logger.info("get classTeam by primary key fail, username:{}, e:{}", name, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据班级名查询班级记录失败");
        }
        if (classTeamFromMysql != null) {
            logger.info("get classTeam from mysql and save classTeam to redis, classTeam:{}", classTeamFromMysql);
            redisUtil.put(RedisKeys.CLASS_TEAM_CACHE_KEY + classTeamFromMysql.getName(), classTeamFromMysql);
            return setClassTeamDtoGmtCreateAndGmtModify(setClassTeamDtoNameInfo(classTeamFromMysql), classTeamFromMysql);
        }

        return null;
    }

    @Override
    public List<IdAndNameDto> getClassTeamIdAndNameListByCollegeIdAndMajorId(Long collegeId, Long majorId) {
        checkGreaterThanZero(collegeId, "学院 id 不能小于或等于零");
        checkGreaterThanZero(majorId, "专业 id 不能小于或等于零");

        List<ClassTeam> classTeamList = classTeamDao.getClassTeamsListByCollegeIdAndMajorId(collegeId, majorId);
        List<IdAndNameDto> idAndNameDtoList = new ArrayList<>();
        for (ClassTeam classTeam : classTeamList) {
            idAndNameDtoList.add(idAndNameDto.createNewIdAndNameDto(classTeam.getId(), classTeam.getName()));
        }
        return idAndNameDtoList;
    }

    @Override
    public List<IdAndNameDto> getClassTeamIdAndNameListByTeacherId(Long teacherId) {
        checkGreaterThanZero(teacherId, "教师 id 不能小于或等于零");

        List<ClassTeam> classTeamList = classTeamDao.getClassTeamsListByTeacherId(teacherId);
        List<IdAndNameDto> idAndNameDtoList = new ArrayList<>();
        for (ClassTeam classTeam : classTeamList) {
            idAndNameDtoList.add(idAndNameDto.createNewIdAndNameDto(classTeam.getId(), classTeam.getName()));
        }
        return idAndNameDtoList;
    }

    @Override
    public PageInfo<ClassTeamDto> getClassTeamsByPage(int start, int pageSize, SearchConditionsDto searchConditionsDto) {
        checkNotNull(searchConditionsDto, "分页查询的条件对象不能为空");
        checkGreaterThanOrIsZero(start, "分页查询的开始数必须大于零");
        checkGreaterThanZero(pageSize, "分页查询的单页总数必须大于零");

        Map<String, Object> map = new HashMap<>();
        Page page = PageHelper.startPage(start, pageSize);
        if (!StringUtils.isEmpty(searchConditionsDto.getName())) {
            map.put("name", searchConditionsDto.getName());
        }
        List<ClassTeam> classTeamsList;
        try {
            classTeamsList = classTeamDao.queryClassTeamsByPage(map);
        } catch (Exception e) {
            logger.info("queryClassTeamsByPage fail, map:{}, e:{}", map, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据条件分页查询班级记录失败");
        }
        if (classTeamsList != null) {
            List<ClassTeamDto> classTeamDtosList = new Page<>(start, pageSize, true);
            Page classTeamDtosListPage = (Page) classTeamDtosList;
            classTeamDtosListPage.setTotal(page.getTotal());
            classTeamDtosList = queryClassTeamDtoList(classTeamDtosList, classTeamsList);
            return new PageInfo<>(classTeamDtosList);
        }

        return null;
    }

    private void validateArgumentsClassTeamAdd(ClassTeamDto classTeamDto) {
        checkNotEmpty(classTeamDto.getName(), "班级名不能为空");
        checkGreaterThanZero(classTeamDto.getCollegeId(), "学院 id 不能小于或等于零");
        checkGreaterThanZero(classTeamDto.getMajorId(), "专业 id 不能小于或等于零");
    }

    private void validateIsSameNameClassTeam(String name) {
        ClassTeam classTeam;
        try {
            classTeam = classTeamDao.selectByName(name);
        } catch (Exception e) {
            logger.info("selectByUsername fail, name:{}, e:{}", name, e);
            throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), "根据班级名查询班级记录失败");
        }
        if (classTeam != null) {
            throw new ServiceException(ErrorStateEnum.DB_EXIST_SAME_RECORD.getState(), "添加的班级名为 " + name + " 的班级记录已存在");
        }
    }

    private void updateClassTeam(ClassTeam updateClassTeam, ClassTeamDto classTeamDto) {
        if (!StringUtils.isEmpty(classTeamDto.getName())) {
            updateClassTeam.setName(classTeamDto.getName());
        }
        if (classTeamDto.getCollegeId() > 0) {
            updateClassTeam.setCollegeId(classTeamDto.getCollegeId());
        }
        if (classTeamDto.getMajorId() > 0) {
            updateClassTeam.setMajorId(classTeamDto.getMajorId());
        }
        updateClassTeam.setModifier(classTeamDto.getModifier());
        updateClassTeam.setGmtModify(new Date());
    }

    private ClassTeamDto setClassTeamDtoNameInfo(ClassTeam classTeam) {
        ClassTeamDto queryClassTeamDto = classTeamDto.convertFor(classTeam);
        College college = collegeDao.selectByPrimaryKey(classTeam.getCollegeId());
        queryClassTeamDto.setCollegeName(college.getName());
        Major major = majorDao.selectByPrimaryKey(classTeam.getMajorId());
        queryClassTeamDto.setMajorName(major.getName());
        return queryClassTeamDto;
    }

    private ClassTeamDto setClassTeamDtoGmtCreateAndGmtModify(ClassTeamDto newClassTeamDto, ClassTeam classTeam) {
        newClassTeamDto.setGmtCreate(DateUtil.formatDate(classTeam.getGmtCreate()));
        newClassTeamDto.setGmtModify(DateUtil.formatDate(classTeam.getGmtModify()));
        return newClassTeamDto;
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

    private String getMajorName(Long id, List<Major> majorList) {
        String name = "";
        for (Major major : majorList) {
            if (major.getId() == id) {
                name = major.getName();
                break;
            }
        }

        return name;
    }

    private List<ClassTeamDto> queryClassTeamDtoList(List<ClassTeamDto> classTeamDtosList, List<ClassTeam> classTeamsList) {
        List<Long> collegeIdList = new ArrayList<>();
        List<Long> majorIdList = new ArrayList<>();
        for (ClassTeam classTeam : classTeamsList) {
            collegeIdList.add(classTeam.getCollegeId());
            majorIdList.add(classTeam.getMajorId());
            classTeamDtosList.add(setClassTeamDtoGmtCreateAndGmtModify(classTeamDto.convertFor(classTeam), classTeam));
        }
        Map<String, Object> connectedQueryMap = new HashMap<>();
        connectedQueryMap.put("collegeIdList", collegeIdList);
        connectedQueryMap.put("majorIdList", majorIdList);
        List<College> collegeList = collegeDao.queryCollegesByIdList(connectedQueryMap);
        List<Major> majorList = majorDao.queryMajorsByIdList(connectedQueryMap);
        for (ClassTeamDto curClassTeamDto : classTeamDtosList) {
            curClassTeamDto.setCollegeName(getCollegeName(curClassTeamDto.getCollegeId(), collegeList));
            curClassTeamDto.setMajorName(getMajorName(curClassTeamDto.getMajorId(), majorList));
        }
        return classTeamDtosList;
    }
}
