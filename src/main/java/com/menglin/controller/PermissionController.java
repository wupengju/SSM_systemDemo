package com.menglin.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.menglin.common.ActionResult;
import com.menglin.common.CommonConst;
import com.menglin.dto.IdAndNameDto;
import com.menglin.dto.JWTPayloadDto;
import com.menglin.dto.PermissionDto;
import com.menglin.dto.SearchConditionsDto;
import com.menglin.service.PermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping("/permission")
@ResponseBody
public class PermissionController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(PermissionController.class);

    @Resource
    private PermissionService permissionService;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public ActionResult<?> list(
            HttpServletRequest request,
            @RequestParam(value = "page") int page,
            @RequestParam(value = "pageSize") int pageSize,
            SearchConditionsDto searchConditionsDto) {
        validateCurrentUserIdentity(request, CommonConst.ADMIN_IDENTITY);

        PageInfo<PermissionDto> pageInfo = permissionService.getPermissionsByPage(page, pageSize, searchConditionsDto);

        return getQueryByPageResult(pageInfo, "分页查询的权限不存在");
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public ActionResult<?> add(HttpServletRequest request, @RequestBody PermissionDto permissionDto) {
        JWTPayloadDto JWTPayloadDto = validateCurrentUserIdentityAndGetPayloadInfoFromRequest(request, CommonConst.ADMIN_IDENTITY);

        permissionDto.setCreator(JWTPayloadDto.getUsername());
        int insertId = permissionService.addPermission(permissionDto);
        logger.info("request: permission/add , permission:{}, insertId:{}", JSONObject.toJSONString(permissionDto.convertToPermission()), insertId);

        return insertId > 0 ? createSuccessActionResult("添加成功") : createFailActionResult("添加失败");
    }

    @RequestMapping(value = "", method = RequestMethod.PUT)
    public ActionResult<?> update(HttpServletRequest request, @RequestBody PermissionDto permissionDto) {
        JWTPayloadDto JWTPayloadDto = validateCurrentUserIdentityAndGetPayloadInfoFromRequest(request, CommonConst.ADMIN_IDENTITY);

        permissionDto.setModifier(JWTPayloadDto.getUsername());
        int updateId = permissionService.updatePermission(permissionDto);
        logger.info("request: permission/update , permission:{}, updateId:{}", JSONObject.toJSONString(permissionDto.convertToPermission()), updateId);

        return updateId > 0 ? createSuccessActionResult("修改成功") : createFailActionResult("修改失败");
    }

    @RequestMapping(value = "/{ids}", method = RequestMethod.DELETE)
    public ActionResult<?> delete(HttpServletRequest request, @PathVariable String ids) {
        validateCurrentUserIdentity(request, CommonConst.ADMIN_IDENTITY);

        permissionService.batchDeletePermissionsByIds(ids);
        logger.info("request: permission/delete , ids:{}", ids);

        return createSuccessActionResult("删除成功");
    }

    @RequestMapping(value = "/id/{id}", method = RequestMethod.GET)
    public ActionResult<?> queryById(HttpServletRequest request, @PathVariable Long id) {
        validateCurrentUserIdentity(request, CommonConst.ADMIN_IDENTITY);

        PermissionDto permissionDto = permissionService.getPermissionById(id);

        return permissionDto != null ? createSuccessActionResult(permissionDto) : createFailActionResult("查询的权限不存在");
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ActionResult<?> getPermissionIdAndNameDto(HttpServletRequest request) {
        validateCurrentUserIdentity(request, CommonConst.ADMIN_IDENTITY);

        List<IdAndNameDto> idAndNameDtoList = permissionService.getPermissionIdAndNameList();

        return idAndNameDtoList != null ? createSuccessActionResult(idAndNameDtoList) : createFailActionResult("角色列表不存在");
    }
}