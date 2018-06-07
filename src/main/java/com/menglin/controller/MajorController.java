package com.menglin.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.menglin.common.ActionResult;
import com.menglin.common.CommonConst;
import com.menglin.dto.IdAndNameDto;
import com.menglin.dto.JWTPayloadDto;
import com.menglin.dto.MajorDto;
import com.menglin.dto.SearchConditionsDto;
import com.menglin.service.MajorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping("/major")
@ResponseBody
public class MajorController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(MajorController.class);

    @Resource
    private MajorService majorService;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public ActionResult<?> list(
            HttpServletRequest request,
            @RequestParam(value = "page") int page,
            @RequestParam(value = "pageSize") int pageSize,
            SearchConditionsDto searchConditionsDto) {
        validateCurrentUserIdentity(request, CommonConst.ADMIN_IDENTITY);

        PageInfo<MajorDto> pageInfo = majorService.getMajorsByPage(page, pageSize, searchConditionsDto);

        return getQueryByPageResult(pageInfo, "分页查询的专业不存在");
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public ActionResult<?> add(HttpServletRequest request, @RequestBody MajorDto majorDto) {
        JWTPayloadDto JWTPayloadDto = validateCurrentUserIdentityAndGetPayloadInfoFromRequest(request, CommonConst.ADMIN_IDENTITY);

        majorDto.setCreator(JWTPayloadDto.getUsername());
        int insertId = majorService.addMajor(majorDto);
        logger.info("request: major/add , major:{}, insertId:{}", JSONObject.toJSONString(majorDto.convertToMajor()), insertId);

        return insertId > 0 ? createSuccessActionResult("添加成功") : createFailActionResult("添加失败");
    }

    @RequestMapping(value = "", method = RequestMethod.PUT)
    public ActionResult<?> update(HttpServletRequest request, @RequestBody MajorDto majorDto) {
        JWTPayloadDto JWTPayloadDto = validateCurrentUserIdentityAndGetPayloadInfoFromRequest(request, CommonConst.ADMIN_IDENTITY);

        majorDto.setModifier(JWTPayloadDto.getUsername());
        int updateId = majorService.updateMajor(majorDto);
        logger.info("request: major/update , major:{}, updateId:{}", JSONObject.toJSONString(majorDto.convertToMajor()), updateId);

        return updateId > 0 ? createSuccessActionResult("修改成功") : createFailActionResult("修改失败");
    }

    @RequestMapping(value = "/{ids}", method = RequestMethod.DELETE)
    public ActionResult<?> delete(HttpServletRequest request, @PathVariable String ids) {
        validateCurrentUserIdentity(request, CommonConst.ADMIN_IDENTITY);

        majorService.batchDeleteMajorsByIds(ids);
        logger.info("request: major/delete , ids:{}", ids);

        return createSuccessActionResult("删除成功");
    }

    @RequestMapping(value = "/id/{id}", method = RequestMethod.GET)
    public ActionResult<?> queryById(HttpServletRequest request, @PathVariable Long id) {
        validateCurrentUserIdentity(request, CommonConst.ADMIN_IDENTITY);

        MajorDto majorDto = majorService.getMajorById(id);

        return majorDto != null ? createSuccessActionResult(majorDto) : createFailActionResult("查询的专业不存在");
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ActionResult<?> getMajorIdAndNameDto(@RequestParam(value = "collegeId") Long collegeId) {
        List<IdAndNameDto> idAndNameDtoList = majorService.getMajorIdAndNameList(collegeId);
        return idAndNameDtoList != null ? createSuccessActionResult(idAndNameDtoList) : createFailActionResult("专业列表不存在");
    }
}