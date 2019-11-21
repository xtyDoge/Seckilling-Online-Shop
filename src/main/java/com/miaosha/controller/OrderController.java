package com.miaosha.controller;

import com.miaosha.aspect.annotation.CheckAuthorization;
import com.miaosha.config.RedisConfig;
import com.miaosha.error.BusinessException;
import com.miaosha.error.EmBusinessError;
import com.miaosha.response.CommonReturnType;
import com.miaosha.service.OrderService;
import com.miaosha.service.model.OrderModel;
import com.miaosha.service.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;


@Controller("order")
@RequestMapping("/order")
@CrossOrigin(origins = {"*"},allowCredentials = "true")

public class OrderController extends BaseController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private HttpServletRequest httpServletRequest;



    @CheckAuthorization
    @RequestMapping(value = "/createorder",method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType createOrder(@RequestParam(name = "itemId")  Integer itemId,
                                        @RequestParam(name = "amount")  Integer amount,
                                        @RequestParam(name = "promoId",required = false) Integer promoId) throws BusinessException {
        // 需要先获取登录中的用户id
//        Boolean isLogin = (Boolean) this.httpServletRequest.getSession().getAttribute("IS_LOGIN");
//        if(isLogin == null || !isLogin.booleanValue()){
//            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN);
//        }
//        else{
            UserModel userModel = (UserModel) this.httpServletRequest.getSession().getAttribute("LOGIN_USER");
            OrderModel orderModel = orderService.createOrder(userModel.getId(),itemId,promoId,amount);
            return CommonReturnType.create(orderModel);
//        }

    }
}
