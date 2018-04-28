package com.menglin.controller;

import com.menglin.Bo.UserBo;
import com.menglin.common.ActionResult;
import com.menglin.dto.JWTCheckInfo;
import com.menglin.dto.LoginUserInfo;
import com.menglin.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Controller
@ResponseBody
public class UserController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(UserController.class);

    @Resource
    private UserService userService;

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ActionResult<?> login(HttpServletRequest request, @RequestBody LoginUserInfo loginUserInfo) {

        String currentName = checkJWTReturnTitle(request);
        if (!"".equals(currentName)) {
            return createSuccessActionResult("已经登录用户:" + currentName);
        }

        UserBo userBo = userService.login(loginUserInfo.getUsername(), loginUserInfo.getUuid(), loginUserInfo.getSignature());

        logger.info("/login， LoginUserInfo:", loginUserInfo.toString());

        return createSuccessActionResult(userBo);
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ActionResult<?> register(HttpServletRequest request, @RequestBody LoginUserInfo loginUserInfo) {

        String currentName = checkJWTReturnTitle(request);
        if (!"".equals(currentName)) {
            return createSuccessActionResult("已经登录用户:" + currentName);
        }

        UserBo userBo = userService.login(loginUserInfo.getUsername(), loginUserInfo.getUuid(), loginUserInfo.getSignature());

        logger.info("/register test， LoginUserInfo:", loginUserInfo.toString());

        return createSuccessActionResult(userBo);
    }

    private String checkJWTReturnTitle(HttpServletRequest request) {
        JWTCheckInfo jwtCheckInfo = new JWTCheckInfo();
        jwtCheckInfo.setAuthorization(request.getHeader("Authorization"));
        jwtCheckInfo.setCurrentName(request.getHeader("currentName"));
        jwtCheckInfo.setRequestUrI(request.getRequestURI());
        if (jwtCheckInfo.getAuthorization() == null || jwtCheckInfo.getCurrentName() == null || !userService.checkJWT(jwtCheckInfo)) {
            return "";
        }
        return jwtCheckInfo.getCurrentName();
    }
}