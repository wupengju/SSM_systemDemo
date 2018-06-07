package com.menglin.aop;

import org.aspectj.lang.JoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeHandler {

    private Logger logger = LoggerFactory.getLogger(TimeHandler.class);

    /*
     * Object[] getArgs：返回目标方法的参数
     * Signature getSignature：返回目标方法的签名
     * Object getTarget：返回被织入增强处理的目标对象
     * Object getThis：返回AOP框架为目标对象生成的代理对象
     * */
    public void beforeMethod(JoinPoint joinPoint) {
        printTime();
        logJoinPointInfo(joinPoint);

        // 获取传入方法实参的参数名和参数值
        /*String[] paramNames = ((CodeSignature) joinPoint.getSignature()).getParameterNames();
        Object[] paramValue = joinPoint.getArgs();
        StringBuilder paramsInfo = new StringBuilder();
        for (int i = 0, length = paramNames.length; i < length; i++) {
            paramsInfo.append(paramNames[i]).append(":").append(paramValue[i]);
        }
        logger.info("params:{}", paramsInfo);*/
    }

    public void afterMethod(JoinPoint joinPoint) {
        logJoinPointInfo(joinPoint);
    }

    private void printTime() {
        //此处准备记录运行日志
        String time = new SimpleDateFormat("yyyy-MM-dd hh24:mm:ss").format(new Date());
        System.out.println("Spring AOP >>>>> Current Time: " + time);
    }

    private void logJoinPointInfo(JoinPoint joinPoint) {
        logger.info("class:{}, method:{}", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
    }
}
