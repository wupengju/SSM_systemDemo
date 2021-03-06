package com.menglin.exception;

import com.menglin.common.ActionResult;
import com.menglin.enums.ErrorStateEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.thymeleaf.util.StringUtils;

/*
 * 全局异常处理
 * */
@ControllerAdvice
@ResponseBody
public class ExceptionHandlers {
    private static Logger logger = LoggerFactory.getLogger(ExceptionHandlers.class);

    private ActionResult<?> createFailActionResult(Integer code, String errorMessage) {
        ActionResult actionResult = new ActionResult();
        actionResult.fail(code, errorMessage);
        return actionResult;
    }


    /**
     * 10000 - 参数错误
     */
    @ExceptionHandler({ArgumentsException.class})
    public ActionResult<?> handleGlobalException(ArgumentsException e) {
        logger.info("ArgumentsException, exception:{}", e.getMessage());
        ActionResult actionResult;
        if (StringUtils.isEmpty(e.getMessage())) {
            actionResult = createFailActionResult(ErrorStateEnum.PARAMETER_ERROR.getState(), ErrorStateEnum.PARAMETER_ERROR.getStateInfo());
        } else {
            String errorMessage = ErrorStateEnum.PARAMETER_ERROR.getStateInfo() + ": " + e.getMessage();
            actionResult = createFailActionResult(ErrorStateEnum.PARAMETER_ERROR.getState(), errorMessage);
        }
        return actionResult;
    }


    /**
     * 500 - 业务逻辑出错
     */
    @ExceptionHandler({ServiceException.class})
    public ActionResult<?> handleBusinessException(ServiceException e) {
        logger.info("ServiceException, exception:{}", e);
        return createFailActionResult(e.getCode(), e.getMessage());
    }


    /**
     * 500 - 文件上传过大
     */
    @ExceptionHandler({MaxUploadSizeExceededException.class})
    public ActionResult<?> handleBusinessException(MaxUploadSizeExceededException e) {
        logger.info("MaxUploadSizeExceededException, exception:{}", e);
        return createFailActionResult(500, "上传文件过大");
    }


    /**
     * 400 - Bad Request
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ActionResult<?> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        logger.error("参数解析失败, exception:{}", e);
        return createFailActionResult(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase());
    }


    /**
     * 404 - Not Found
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoHandlerFoundException.class)
    public ActionResult<?> handleHttpMessageNotFoundException(NoHandlerFoundException e) {
        logger.error("未找到相关资源, exception:{}", e);
        return createFailActionResult(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase());
    }


    /**
     * 405 - Method Not Allowed
     */
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ActionResult<?> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        logger.error("不支持当前请求方法, exception:{}", e);
        return createFailActionResult(HttpStatus.METHOD_NOT_ALLOWED.value(), HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase());
    }


    /**
     * 415 - Unsupported Media Type
     */
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ActionResult<?> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
        logger.error("不支持当前媒体类型, exception:{}", e);
        return createFailActionResult(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(), HttpStatus.UNSUPPORTED_MEDIA_TYPE.getReasonPhrase());
    }


    /**
     * 500 - Internal Server Error
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ActionResult<?> handleException(Exception e) {
        logger.error("服务运行异常, exception:{}", e);
        return createFailActionResult(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
    }
}
