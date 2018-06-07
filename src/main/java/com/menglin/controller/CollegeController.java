package com.menglin.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.menglin.common.ActionResult;
import com.menglin.common.CommonConst;
import com.menglin.dto.CollegeDto;
import com.menglin.dto.IdAndNameDto;
import com.menglin.dto.JWTPayloadDto;
import com.menglin.dto.SearchConditionsDto;
import com.menglin.service.CollegeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping("/college")
@ResponseBody
public class CollegeController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(CollegeController.class);

    @Resource
    private CollegeService collegeService;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public ActionResult<?> list(
            HttpServletRequest request,
            @RequestParam(value = "page") int page,
            @RequestParam(value = "pageSize") int pageSize,
            SearchConditionsDto searchConditionsDto) {
        validateCurrentUserIdentity(request, CommonConst.ADMIN_IDENTITY);

        PageInfo<CollegeDto> pageInfo = collegeService.getCollegesByPage(page, pageSize, searchConditionsDto);

        return getQueryByPageResult(pageInfo, "分页查询的学院不存在");
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public ActionResult<?> add(HttpServletRequest request, @RequestBody CollegeDto collegeDto) {
        JWTPayloadDto JWTPayloadDto = validateCurrentUserIdentityAndGetPayloadInfoFromRequest(request, CommonConst.ADMIN_IDENTITY);

        collegeDto.setCreator(JWTPayloadDto.getUsername());
        int insertId = collegeService.addCollege(collegeDto);
        logger.info("request: college/add , college:{}, insertId:{}", JSONObject.toJSONString(collegeDto.convertToCollege()), insertId);

        return insertId > 0 ? createSuccessActionResult("添加成功") : createFailActionResult("添加失败");
    }

    @RequestMapping(value = "", method = RequestMethod.PUT)
    public ActionResult<?> update(HttpServletRequest request, @RequestBody CollegeDto collegeDto) {
        JWTPayloadDto JWTPayloadDto = validateCurrentUserIdentityAndGetPayloadInfoFromRequest(request, CommonConst.ADMIN_IDENTITY);

        collegeDto.setModifier(JWTPayloadDto.getUsername());
        int updateId = collegeService.updateCollege(collegeDto);
        logger.info("request: college/update , college:{}, updateId:{}", JSONObject.toJSONString(collegeDto.convertToCollege()), updateId);

        return updateId > 0 ? createSuccessActionResult("修改成功") : createFailActionResult("修改失败");
    }

    @RequestMapping(value = "/{ids}", method = RequestMethod.DELETE)
    public ActionResult<?> delete(HttpServletRequest request, @PathVariable String ids) {
        validateCurrentUserIdentity(request, CommonConst.ADMIN_IDENTITY);

        collegeService.batchDeleteCollegesByIds(ids);
        logger.info("request: college/delete , ids:{}", ids);

        return createSuccessActionResult("删除成功");
    }

    @RequestMapping(value = "/id/{id}", method = RequestMethod.GET)
    public ActionResult<?> queryById(HttpServletRequest request, @PathVariable Long id) {
        validateCurrentUserIdentity(request, CommonConst.ADMIN_IDENTITY);

        CollegeDto collegeDto = collegeService.getCollegeById(id);

        return collegeDto != null ? createSuccessActionResult(collegeDto) : createFailActionResult("查询的学院不存在");
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ActionResult<?> getCollegeIdAndNameDto() {
        List<IdAndNameDto> idAndNameDtoList = collegeService.getCollegeIdAndNameList();
        return idAndNameDtoList != null ? createSuccessActionResult(idAndNameDtoList) : createFailActionResult("学院列表不存在");
    }
}