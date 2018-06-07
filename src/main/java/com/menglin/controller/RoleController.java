package com.menglin.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.menglin.common.ActionResult;
import com.menglin.common.CommonConst;
import com.menglin.dto.IdAndNameDto;
import com.menglin.dto.JWTPayloadDto;
import com.menglin.dto.RoleDto;
import com.menglin.dto.SearchConditionsDto;
import com.menglin.service.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping("/role")
@ResponseBody
public class RoleController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(RoleController.class);

    @Resource
    private RoleService roleService;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public ActionResult<?> list(
            HttpServletRequest request,
            @RequestParam(value = "page") int page,
            @RequestParam(value = "pageSize") int pageSize,
            SearchConditionsDto searchConditionsDto) {
        validateCurrentUserIdentity(request, CommonConst.ADMIN_IDENTITY);

        PageInfo<RoleDto> pageInfo = roleService.getRolesByPage(page, pageSize, searchConditionsDto);

        return getQueryByPageResult(pageInfo, "分页查询的角色不存在");
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public ActionResult<?> add(HttpServletRequest request, @RequestBody RoleDto roleDto) {
        JWTPayloadDto JWTPayloadDto = validateCurrentUserIdentityAndGetPayloadInfoFromRequest(request, CommonConst.ADMIN_IDENTITY);

        roleDto.setCreator(JWTPayloadDto.getUsername());
        int insertId = roleService.addRole(roleDto);
        logger.info("request: role/add , role:{}, insertId:{}", JSONObject.toJSONString(roleDto.convertToRole()), insertId);

        return insertId > 0 ? createSuccessActionResult("添加成功") : createFailActionResult("添加失败");
    }

    @RequestMapping(value = "", method = RequestMethod.PUT)
    public ActionResult<?> update(HttpServletRequest request, @RequestBody RoleDto roleDto) {
        JWTPayloadDto JWTPayloadDto = validateCurrentUserIdentityAndGetPayloadInfoFromRequest(request, CommonConst.ADMIN_IDENTITY);

        roleDto.setModifier(JWTPayloadDto.getUsername());
        int updateId = roleService.updateRole(roleDto);
        logger.info("request: role/update , role:{}, updateId:{}", JSONObject.toJSONString(roleDto.convertToRole()), updateId);

        return updateId > 0 ? createSuccessActionResult("修改成功") : createFailActionResult("修改失败");
    }

    @RequestMapping(value = "/{ids}", method = RequestMethod.DELETE)
    public ActionResult<?> delete(HttpServletRequest request, @PathVariable String ids) {
        validateCurrentUserIdentity(request, CommonConst.ADMIN_IDENTITY);

        roleService.batchDeleteRolesByIds(ids);
        logger.info("request: role/delete , ids:{}", ids);

        return createSuccessActionResult("删除成功");
    }

    @RequestMapping(value = "/id/{id}", method = RequestMethod.GET)
    public ActionResult<?> queryById(HttpServletRequest request, @PathVariable Long id) {
        validateCurrentUserIdentity(request, CommonConst.ADMIN_IDENTITY);

        RoleDto roleDto = roleService.getRoleById(id);

        return roleDto != null ? createSuccessActionResult(roleDto) : createFailActionResult("查询的角色不存在");
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ActionResult<?> getRoleIdAndNameDto(HttpServletRequest request) {
        validateCurrentUserIdentity(request, CommonConst.ADMIN_IDENTITY);

        List<IdAndNameDto> idAndNameDtoList = roleService.getRoleIdAndNameList();

        return idAndNameDtoList != null ? createSuccessActionResult(idAndNameDtoList) : createFailActionResult("角色列表不存在");
    }
}