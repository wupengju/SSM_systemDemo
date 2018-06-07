package com.menglin.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.menglin.common.ActionResult;
import com.menglin.common.CommonConst;
import com.menglin.dto.JWTPayloadDto;
import com.menglin.dto.NoticeDto;
import com.menglin.dto.SearchConditionsDto;
import com.menglin.service.NoticeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/notice")
@ResponseBody
public class NoticeController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(NoticeController.class);

    @Resource
    private NoticeService noticeService;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public ActionResult<?> list(
            HttpServletRequest request,
            @RequestParam(value = "page") int page,
            @RequestParam(value = "pageSize") int pageSize,
            SearchConditionsDto searchConditionsDto) {
        validateCurrentUserIdentity(request, CommonConst.ADMIN_IDENTITY);

        PageInfo<NoticeDto> pageInfo = noticeService.getNoticesByPage(page, pageSize, searchConditionsDto);

        return getQueryByPageResult(pageInfo, "分页查询的公告不存在");
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public ActionResult<?> add(HttpServletRequest request, @RequestBody NoticeDto noticeDto) {
        JWTPayloadDto JWTPayloadDto = validateCurrentUserIdentityAndGetPayloadInfoFromRequest(request, CommonConst.ADMIN_IDENTITY);

        noticeDto.setCreator(JWTPayloadDto.getUsername());
        int insertId = noticeService.addNotice(noticeDto);
        logger.info("request: notice/add , notice:{}, insertId:{}", JSONObject.toJSONString(noticeDto.convertToNotice()), insertId);

        return insertId > 0 ? createSuccessActionResult("添加成功") : createFailActionResult("添加失败");
    }

    @RequestMapping(value = "", method = RequestMethod.PUT)
    public ActionResult<?> update(HttpServletRequest request, @RequestBody NoticeDto noticeDto) {
        JWTPayloadDto JWTPayloadDto = validateCurrentUserIdentityAndGetPayloadInfoFromRequest(request, CommonConst.ADMIN_IDENTITY);

        noticeDto.setModifier(JWTPayloadDto.getUsername());
        int updateId = noticeService.updateNotice(noticeDto);
        logger.info("request: notice/update , notice:{}, updateId:{}", JSONObject.toJSONString(noticeDto.convertToNotice()), updateId);

        return updateId > 0 ? createSuccessActionResult("修改成功") : createFailActionResult("修改失败");
    }

    @RequestMapping(value = "/{ids}", method = RequestMethod.DELETE)
    public ActionResult<?> delete(HttpServletRequest request, @PathVariable String ids) {
        validateCurrentUserIdentity(request, CommonConst.ADMIN_IDENTITY);

        noticeService.batchDeleteNoticesByIds(ids);
        logger.info("request: notice/delete , ids:{}", ids);

        return createSuccessActionResult("删除成功");
    }

    @RequestMapping(value = "/id/{id}", method = RequestMethod.GET)
    public ActionResult<?> queryById(HttpServletRequest request, @PathVariable Long id) {
        validateCurrentUserIdentity(request, CommonConst.ADMIN_IDENTITY);

        NoticeDto noticeDto = noticeService.getNoticeById(id);

        return noticeDto != null ? createSuccessActionResult(noticeDto) : createFailActionResult("查询的公告不存在");
    }
}