package com.menglin.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.menglin.common.ActionResult;
import com.menglin.common.CommonConst;
import com.menglin.dto.AdminDto;
import com.menglin.dto.JWTPayloadDto;
import com.menglin.dto.SearchConditionsDto;
import com.menglin.service.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/admin")
@ResponseBody
public class AdminController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Resource
    private AdminService adminService;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public ActionResult<?> list(
            HttpServletRequest request,
            @RequestParam(value = "page") int page,
            @RequestParam(value = "pageSize") int pageSize,
            SearchConditionsDto searchConditionsDto) {
        validateCurrentUserIdentity(request, CommonConst.ADMIN_IDENTITY);

        PageInfo<AdminDto> pageInfo = adminService.getAdminsByPage(page, pageSize, searchConditionsDto);

        return getQueryByPageResult(pageInfo, "分页查询的管理员不存在");
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public ActionResult<?> add(HttpServletRequest request, @RequestBody AdminDto adminDto) {
        validateCurrentUserIdentity(request, CommonConst.ADMIN_IDENTITY);

        int insertId = adminService.addAdmin(adminDto);
        logger.info("request: admin/add , admin:{}, insertId:{}", JSONObject.toJSONString(adminDto.convertToAdmin()), insertId);

        return insertId > 0 ? createSuccessActionResult("添加成功") : createFailActionResult("添加失败");
    }

    @RequestMapping(value = "", method = RequestMethod.PUT)
    public ActionResult<?> update(HttpServletRequest request, @RequestBody AdminDto adminDto) {
        validateCurrentUserIdentity(request, CommonConst.ADMIN_IDENTITY);

        int updateId = adminService.updateAdmin(adminDto);
        logger.info("request: admin/update , admin:{}, updateId:{}", JSONObject.toJSONString(adminDto.convertToAdmin()), updateId);

        return updateId > 0 ? createSuccessActionResult("修改成功") : createFailActionResult("修改失败");
    }

    @RequestMapping(value = "/{ids}", method = RequestMethod.DELETE)
    public ActionResult<?> delete(HttpServletRequest request, @PathVariable String ids) {
        validateCurrentUserIdentity(request, CommonConst.ADMIN_IDENTITY);

        adminService.batchDeleteAdminsByIds(ids);
        logger.info("request: admin/delete , ids:{}", ids);

        return createSuccessActionResult("删除成功");
    }

    @RequestMapping(value = "/id/{id}", method = RequestMethod.GET)
    public ActionResult<?> queryById(HttpServletRequest request, @PathVariable Long id) {
        validateCurrentUserIdentity(request, CommonConst.ADMIN_IDENTITY);

        AdminDto adminDto = adminService.getAdminById(id);

        return adminDto != null ? createSuccessActionResult(adminDto) : createFailActionResult("查询的管理员不存在");
    }

    @RequestMapping(value = "/personalInfo", method = RequestMethod.GET)
    public ActionResult<?> personalInfo(HttpServletRequest request) {
        JWTPayloadDto JWTPayloadDto = validateCurrentUserIdentityAndGetPayloadInfoFromRequest(request, CommonConst.ADMIN_IDENTITY);

        AdminDto adminDto = adminService.getAdminById(JWTPayloadDto.getUserId());

        return adminDto != null ? createSuccessActionResult(adminDto) : createFailActionResult("查询的管理员不存在");
    }
}