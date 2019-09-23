package com.miaosha.controller;

import com.miaosha.error.BusinessException;
import com.miaosha.error.EmBusinessError;
import com.miaosha.response.CommonReturnType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class BaseController {

    public static final String CONTENT_TYPE_FORMED = "application/x-www-form-urlencoded";


    // 定义ExceptionHandle解决Controller抛出的异常
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Object handlerException(HttpServletRequest request, Exception ex){

        Map<String,Object> responseData = new HashMap<>();

        // 会有大量的if-else处理不同业务的exception吗
        if(ex instanceof BusinessException){
            // BusinessException
            BusinessException businessEx = (BusinessException) ex;
            responseData.put("errorCode",businessEx.getErrCode());
            responseData.put("errorMsg",businessEx.getErrMsg());
        }
        else{
            // 未知Exception
            ex.printStackTrace();
            responseData.put("errorCode", EmBusinessError.UNKNOWN_ERROR.getErrCode());
            responseData.put("errorMsg",EmBusinessError.UNKNOWN_ERROR.getErrMsg());
        }

        CommonReturnType commonReturnType = new CommonReturnType();
        commonReturnType.setStatus("fail");
        commonReturnType.setData(responseData);
        return commonReturnType;
    }
}
