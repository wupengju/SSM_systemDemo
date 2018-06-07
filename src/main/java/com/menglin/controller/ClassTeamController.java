package com.menglin.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.menglin.common.ActionResult;
import com.menglin.common.CommonConst;
import com.menglin.dto.ClassTeamDto;
import com.menglin.dto.IdAndNameDto;
import com.menglin.dto.JWTPayloadDto;
import com.menglin.dto.SearchConditionsDto;
import com.menglin.service.ClassTeamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping("/classTeam")
@ResponseBody
public class ClassTeamController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(ClassTeamController.class);

    @Resource
    private ClassTeamService classTeamService;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public ActionResult<?> list(
            HttpServletRequest request,
            @RequestParam(value = "page") int page,
            @RequestParam(value = "pageSize") int pageSize,
            SearchConditionsDto searchConditionsDto) {
        validateCurrentUserIdentity(request, CommonConst.ADMIN_IDENTITY);

        PageInfo<ClassTeamDto> pageInfo = classTeamService.getClassTeamsByPage(page, pageSize, searchConditionsDto);

        return getQueryByPageResult(pageInfo, "分页查询的班级不存在");
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public ActionResult<?> add(HttpServletRequest request, @RequestBody ClassTeamDto classTeamDto) {
        JWTPayloadDto JWTPayloadDto = validateCurrentUserIdentityAndGetPayloadInfoFromRequest(request, CommonConst.ADMIN_IDENTITY);

        classTeamDto.setCreator(JWTPayloadDto.getUsername());
        int insertId = classTeamService.addClassTeam(classTeamDto);
        logger.info("request: classTeam/add , classTeam:{}, insertId:{}", JSONObject.toJSONString(classTeamDto.convertToClassTeam()), insertId);

        return insertId > 0 ? createSuccessActionResult("添加成功") : createFailActionResult("添加失败");
    }

    @RequestMapping(value = "", method = RequestMethod.PUT)
    public ActionResult<?> update(HttpServletRequest request, @RequestBody ClassTeamDto classTeamDto) {
        JWTPayloadDto JWTPayloadDto = validateCurrentUserIdentityAndGetPayloadInfoFromRequest(request, CommonConst.ADMIN_IDENTITY);

        classTeamDto.setModifier(JWTPayloadDto.getUsername());
        int updateId = classTeamService.updateClassTeam(classTeamDto);
        logger.info("request: classTeam/update , classTeam:{}, updateId:{}", JSONObject.toJSONString(classTeamDto.convertToClassTeam()), updateId);

        return updateId > 0 ? createSuccessActionResult("修改成功") : createFailActionResult("修改失败");
    }

    @RequestMapping(value = "/{ids}", method = RequestMethod.DELETE)
    public ActionResult<?> delete(HttpServletRequest request, @PathVariable String ids) {
        validateCurrentUserIdentity(request, CommonConst.ADMIN_IDENTITY);

        classTeamService.batchDeleteClassTeamsByIds(ids);
        logger.info("request: classTeam/delete , ids:{}", ids);

        return createSuccessActionResult("删除成功");
    }

    @RequestMapping(value = "/id/{id}", method = RequestMethod.GET)
    public ActionResult<?> queryById(HttpServletRequest request, @PathVariable Long id) {
        validateCurrentUserIdentity(request, CommonConst.ADMIN_IDENTITY);

        ClassTeamDto classTeamDto = classTeamService.getClassTeamById(id);

        return classTeamDto != null ? createSuccessActionResult(classTeamDto) : createFailActionResult("查询的班级不存在");
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ActionResult<?> getClassTeamIdAndNameDto(@RequestParam(value = "collegeId") Long collegeId, @RequestParam(value = "majorId") Long majorId) {
        List<IdAndNameDto> idAndNameDtoList = classTeamService.getClassTeamIdAndNameListByCollegeIdAndMajorId(collegeId, majorId);
        return idAndNameDtoList != null ? createSuccessActionResult(idAndNameDtoList) : createFailActionResult("班级列表不存在");
    }
}