package com.menglin.interceptor;

import com.alibaba.fastjson.JSON;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.menglin.common.ActionResult;
import com.menglin.dto.JWTPayloadDto;
import com.menglin.enums.ErrorStateEnum;
import com.menglin.util.JWTUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * JWT validate
 */
public class JWTCheckInterceptor implements HandlerInterceptor {

    private Logger logger = LoggerFactory.getLogger(JWTCheckInterceptor.class);

    /**
     * 在DispatcherServlet之前执行
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws IOException, TokenExpiredException {

        response.setCharacterEncoding("utf-8");
        String jwt = request.getHeader("Authorization");
        String currentName = request.getHeader("currentName");

        logger.info("JWTCheckInterceptor - jwt:{}, name{}", jwt, currentName);

        // 非法操作
        if (StringUtils.isEmpty(jwt) || StringUtils.isEmpty(currentName)) {
            return buildResponseResult(response, ErrorStateEnum.ILLEGAL_OPERATION);
        }

        // Token 超时
        JWTPayloadDto JWTPayloadDto = JWTUtil.decrypt(jwt, JWTPayloadDto.class);
        logger.info("decrypt jwt after:{}", JSON.toJSONString(JWTPayloadDto));
        if (JWTPayloadDto == null) {
            return buildResponseResult(response, ErrorStateEnum.TOKEN_TIMEOUT);
        }

        // 验证当前登录用户和Token是否匹配
        return currentName.equals(JWTPayloadDto.getUsername()) || buildResponseResult(response, ErrorStateEnum.TOKEN_VALIDATE_FAIL);
    }

    /**
     * 在controller执行之后的DispatcherServlet之后执行
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object o, ModelAndView modelAndView) throws Exception {

    }

    /**
     * 在页面渲染完成返回给客户端之前执行
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object o, Exception e) throws Exception {

    }

    private boolean buildResponseResult(HttpServletResponse response, ErrorStateEnum errorStateEnum) throws IOException {
        ActionResult actionResult = new ActionResult();
        actionResult.fail(errorStateEnum.getState(), errorStateEnum.getStateInfo());
        responseMessage(response, actionResult);
        return false;
    }

    // 请求不通过，返回错误信息给客户端
    private void responseMessage(HttpServletResponse response, ActionResult actionResult) throws IOException {
        response.setContentType("application/json; charset=utf-8");
        PrintWriter out = response.getWriter();
        out.print(JSON.toJSONString(actionResult));
        out.flush();
        out.close();
    }
}
