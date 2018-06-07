package com.menglin.controller;

import com.github.pagehelper.PageInfo;
import com.menglin.common.ActionResult;
import com.menglin.dto.JWTCheckInfoDto;
import com.menglin.dto.JWTPayloadDto;
import com.menglin.enums.ErrorStateEnum;
import com.menglin.exception.ServiceException;
import com.menglin.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class BaseController {
    private Logger logger = LoggerFactory.getLogger(BaseController.class);

    @Resource
    private UserService userService;

    ActionResult<?> createFailActionResult(String errorMessage) {
        ActionResult actionResult = new ActionResult();
        actionResult.fail(500, errorMessage);
        return actionResult;
    }

    ActionResult<Object> createSuccessActionResult(Object data) {
        ActionResult<Object> actionResult = new ActionResult<>();
        actionResult.success(data);
        return actionResult;
    }

    ActionResult<?> getQueryByPageResult(PageInfo<?> pageInfo, String errorMessage) {
        if (pageInfo == null) {
            return createFailActionResult(errorMessage);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("totalNum", pageInfo.getTotal());
        data.put("totalPages", pageInfo.getPages());
        data.put("page", pageInfo.getPageNum());
        data.put("pageSize", pageInfo.getPageSize());
        data.put("rows", pageInfo.getList());

        return createSuccessActionResult(data);
    }

    JWTPayloadDto getPayloadInfoFromRequest(HttpServletRequest request) {
        JWTCheckInfoDto jwtCheckInfoDto = new JWTCheckInfoDto();
        jwtCheckInfoDto.setAuthorization(request.getHeader("Authorization"));
        jwtCheckInfoDto.setCurrentName(request.getHeader("currentName"));
        jwtCheckInfoDto.setRequestUrI(request.getRequestURI());
        if (!StringUtils.isEmpty(jwtCheckInfoDto.getAuthorization()) && !StringUtils.isEmpty(jwtCheckInfoDto.getCurrentName())) {
            if (userService.checkJWT(jwtCheckInfoDto)) {
                return userService.getPayloadInfo(jwtCheckInfoDto.getAuthorization());
            } else {
                throw new ServiceException(ErrorStateEnum.ILLEGAL_OPERATION.getState(), ErrorStateEnum.ILLEGAL_OPERATION.getStateInfo());
            }
        } else {
            return null;
        }
    }

    void validateCurrentUserIdentity(HttpServletRequest request, String expectedIdentity) {
        JWTPayloadDto JWTPayloadDto = getPayloadInfoFromRequest(request);
        if (!expectedIdentity.equals(JWTPayloadDto.getIdentity())) {
            logger.info("validateIdentity fail, JWTPayloadDto:{}, expectedIdentity:{}", JWTPayloadDto, expectedIdentity);
            throw new ServiceException("身份权限不够，请联系系统管理员");
        }
    }

    void validateCurrentUserIdentity(JWTPayloadDto JWTPayloadDto, String expectedIdentity) {
        if (!expectedIdentity.equals(JWTPayloadDto.getIdentity())) {
            logger.info("validateIdentity fail, JWTPayloadDto:{}, expectedIdentity:{}", JWTPayloadDto, expectedIdentity);
            throw new ServiceException("身份权限不够，请联系系统管理员");
        }
    }

    JWTPayloadDto validateCurrentUserIdentityAndGetPayloadInfoFromRequest(HttpServletRequest request, String expectedIdentity) {
        JWTPayloadDto JWTPayloadDto = getPayloadInfoFromRequest(request);
        validateCurrentUserIdentity(JWTPayloadDto, expectedIdentity);
        return JWTPayloadDto;
    }
}
