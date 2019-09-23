package com.miaosha.controller;


import com.miaosha.controller.viewobject.UserVO;
import com.miaosha.error.BusinessException;
import com.miaosha.error.EmBusinessError;
import com.miaosha.response.CommonReturnType;
import com.miaosha.service.UserService;
import com.miaosha.service.model.UserModel;
import org.apache.catalina.User;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.security.MD5Encoder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.miaosha.error.EmBusinessError.USER_LOGIN_FAIL;
import static com.miaosha.error.EmBusinessError.USER_NOT_EXIST;

@CrossOrigin(allowCredentials = "true",allowedHeaders = "*")
@Controller("user")
@RequestMapping("/user")
public class UserController extends BaseController{


    @Autowired
    private UserService userService;

    @RequestMapping("/get")
    @ResponseBody
    public CommonReturnType getUser(@RequestParam(name = "id") Integer id) throws BusinessException{
        UserModel userModel = userService.getUserById(id);

        // 如果id不存在
        if(userModel == null){
            userModel.setEncrptPassword("1");
            throw new BusinessException(USER_NOT_EXIST);
        }

        // 返回CommonReturnType
        return CommonReturnType.create(convertUserVOfromUserModel(userModel));
    }

    @Autowired
    private HttpServletRequest httpServletRequest;
    // 获取手机验证码
    @RequestMapping(value = "/getotp",method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType getOtp(@RequestParam(name="telphone") String telphone){
        // 生成OTP验证码
        Random random = new Random();
        int randomInt = random.nextInt(99999);
        randomInt += 10000;
        String otpCode = String.valueOf(randomInt);
        // 关联对应手机号（Redis 反复点击getOtp 支持expire时间 原生数据结构）使用HTTPSession绑定时间与code
        httpServletRequest.getSession().setAttribute(telphone,otpCode);
        // 将OTP发送短信给用户
        System.out.println("telphone=" + telphone +",otpcode=" + otpCode);

        return CommonReturnType.create(null);

    }

    // 用户注册
    @RequestMapping(value = "/register",method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType register(
            @RequestParam(name="telphone") String telphone,
            @RequestParam(name="otpCode") String otpCode,
            @RequestParam(name="name") String name,
            @RequestParam(name="gender") Byte gender,
            @RequestParam(name="age") Integer age,
            @RequestParam(name="password") String password
            ) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        // 验证手机号和对应OTPCode是否相符合
        String sessionOtpCode = (String) this.httpServletRequest.getSession().getAttribute(telphone);
        if(!com.alibaba.druid.util.StringUtils.equals(sessionOtpCode,otpCode)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"验证码不一致");
        }
        // 用户注册流程 检查-拼装对象-传入数据库
        UserModel userModel = new UserModel();
        userModel.setAge(age);
        userModel.setName(name);
        userModel.setGender(gender);
        userModel.setTelphone(telphone);
        userModel.setRegisterMode("byphone");
        // 密码从明文到密文
        userModel.setEncrptPassword(encodeByMD5(password));
        //传入数据库
        userService.register(userModel);
        return CommonReturnType.create(null);
    }

    public String encodeByMD5(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        Base64.Encoder base64Encoder = Base64.getEncoder();
        String newStr = base64Encoder.encodeToString(md5.digest(str.getBytes("utf-8")));
        return newStr;

    }


    @RequestMapping(value = "/login",method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType login(@RequestParam String telphone, @RequestParam String password) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        // 判空
        if(StringUtils.isEmpty(telphone) || StringUtils.isEmpty(password)){
            throw new BusinessException(USER_LOGIN_FAIL);
        }
        // 送入service
        UserModel userModel = userService.validateLogin(telphone,encodeByMD5(password));
        // 加入session内
        this.httpServletRequest.getSession().setAttribute("IS_LOGIN",true);
        this.httpServletRequest.getSession().setAttribute("LOGIN_USER",userModel);

        return CommonReturnType.create(null);
    }

    // 将UserModel转为用户展示的UserViewObject
    private UserVO convertUserVOfromUserModel(UserModel userModel){
        UserVO userVO = new UserVO();
        if(userModel == null){
            return null;
        }
        BeanUtils.copyProperties(userModel,userVO);
        return userVO;
    }


}
