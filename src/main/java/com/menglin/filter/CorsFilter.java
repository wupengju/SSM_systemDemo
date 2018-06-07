package com.menglin.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;

/**
 * CorsFileter 功能描述：CORS过滤器
 * 允许跨域访问
 */
@Component
public class CorsFilter implements Filter {

    private Logger logger = LoggerFactory.getLogger(CorsFilter.class);

    public void init(FilterConfig filterConfig) throws ServletException {

    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        Enumeration<String> requestHeaderNames = request.getHeaderNames();
        logger.info("before Filter Header is:");
        while (requestHeaderNames.hasMoreElements()) {
            String key = (String) requestHeaderNames.nextElement();
            String value = request.getHeader(key);
            logger.info("key:{}, value:{}", key, value);
        }
        // 设置允许访问的域名
        response.setHeader("Access-Control-Allow-Origin", "*");
        // 设置允许的方式
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, HEAD, PUT, DELETE, OPTIONS");
        // 设置preflight请求的结果能够被缓存多久
        response.setHeader("Access-Control-Max-Age", "3600");
        // 设置允许的header类型
        response.setHeader("Access-Control-Allow-Headers", "Authorization, currentName, Accept, Origin,X-Requested-With, Content-Type, Last-Modified");
        // 设置为true时允许浏览器读取response的内容
        response.setHeader("Access-Control-Allow-Credentials", "true");

        Collection<String> responseHeaderNames = response.getHeaderNames();
        logger.info("after Filter Header is:");
        for (String name : responseHeaderNames) {
            String value = response.getHeader(name);
            logger.info("name:{}, value:{}", name, value);
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    public void destroy() {

    }
}
