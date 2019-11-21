package com.miaosha.service.impl;

import com.miaosha.dao.UserDOMapper;
import com.miaosha.dao.UserPasswordDOMapper;
import com.miaosha.dataobject.UserDO;
import com.miaosha.dataobject.UserPasswordDO;
import com.miaosha.error.BusinessException;
import com.miaosha.error.EmBusinessError;
import com.miaosha.service.UserService;
import com.miaosha.service.model.EmailModel;
import com.miaosha.service.model.UserModel;
import com.miaosha.validator.ValidationResult;
import com.miaosha.validator.ValidatorImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;


@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDOMapper userDOMapper;

    @Autowired
    private UserPasswordDOMapper userPasswordDOMapper;

    @Autowired
    private ValidatorImpl validator;

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public UserModel getUserById(Integer id) {
        UserDO userDO = userDOMapper.selectByPrimaryKey(id);
        if(userDO == null){
            return null;
        }
        // 通过用户id获取加密密码
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserID(userDO.getId());
        return convertFromDataObject(userDO,userPasswordDO);
    }

    @Override
    public UserModel validateLogin(String telphone, String encrptPassword) throws BusinessException {
        UserDO userDO = userDOMapper.selectByTelphone(telphone);
        // 用户不存在
        if(userDO == null){
            throw new BusinessException(EmBusinessError.USER_NOT_EXIST);
        }
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserID(userDO.getId());
        UserModel userModel = convertFromDataObject(userDO,userPasswordDO);
        // 密码不一致
        if(!StringUtils.equals(userModel.getEncrptPassword(),encrptPassword)){
            throw new BusinessException(EmBusinessError.USER_LOGIN_FAIL);
        }
        return userModel;
    }


    @Override
    public void sendEmail(EmailModel emailModel) throws InterruptedException {
        List<UserDO> userDOList = userDOMapper.selectAll();
        System.out.println("拉取用户邮箱信息");

        Set<String> emailSet = userDOList.stream().map(userDO -> {
            if(userDO == null || userDO.getEmail() == null  || StringUtils.isEmpty(userDO.getEmail())){
                return null;
            }
            String emailAddress = userDO.getEmail();
            return emailAddress;
        }).collect(Collectors.toSet());

        ExecutorService sendEmailThreadPool = Executors.newFixedThreadPool(4);
        Set<Object> taskSet = emailSet.stream().map((emailAddress)->{
            Callable<Boolean> task = new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    System.out.println("发送邮件准备，MAILTO _ " + emailAddress);
                    sendEmailBySingleThread(emailAddress,emailModel);
                    System.out.println("发送邮件完成");
                    return true;
                }
            };
            FutureTask<Boolean> futureTask = new FutureTask<>(task);
            sendEmailThreadPool.submit(futureTask);
            return null;
        }).collect(Collectors.toSet());

        System.out.println("发送线程池准备销毁");
        sendEmailThreadPool.shutdown();
    }

    @Override
    @Transactional
    // 注册用户
    public void register(UserModel userModel) throws BusinessException {
        if(userModel == null){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"用户信息不完整");
        }

        ValidationResult result = validator.validate(userModel);
        if(result.isHasErrors()){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,result.getErrMsg());
        }

        UserDO userDO = convertUserDOFromModel(userModel);
        try{
            userDOMapper.insertSelective(userDO);
        }
        catch (DuplicateKeyException ex){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"手机号已重复注册");
        }
        userModel.setId(userDO.getId());
        UserPasswordDO userPasswordDO = convertUserPasswordDOFromModel(userModel);
        userPasswordDOMapper.insertSelective(userPasswordDO);
    }

    private UserPasswordDO convertUserPasswordDOFromModel(UserModel userModel){
        if(userModel == null){
            return null;
        }
        UserPasswordDO userPasswordDO = new UserPasswordDO();
        BeanUtils.copyProperties(userModel,userPasswordDO);
        userPasswordDO.setUserId(userModel.getId());
        return userPasswordDO;
    }

    private UserDO convertUserDOFromModel(UserModel userModel){
        if(userModel == null){
            return null;
        }
        UserDO userDO = new UserDO();
        BeanUtils.copyProperties(userModel,userDO);
        return userDO;
    }

    private UserModel convertFromDataObject(UserDO userDO, UserPasswordDO userPasswordDO){
        UserModel userModel = new UserModel();
        BeanUtils.copyProperties(userDO,userModel);
        if(userPasswordDO != null){
            userModel.setEncrptPassword(userPasswordDO.getEncrptPassword());
        }
        return userModel;
    }


    private EmailModel sendEmailBySingleThread(String emailAddress, EmailModel emailModel) {
        System.out.println("----发送简单的邮件消息启动--->");
        try {
            SimpleMailMessage message=new SimpleMailMessage();
            if(emailAddress == null || StringUtils.isEmpty(emailAddress)){
                return null;
            }
            message.setSubject(emailModel.getSubject());
            message.setText(emailModel.getContent() + "  --本消息为系统自动发送，请勿回复");
            message.setTo(emailAddress);
            message.setFrom("samluxne@bupt.edu.cn");
            mailSender.send(message);
            System.out.println("----发送简单的邮件消息完毕--->");
        }catch (Exception e){
            System.out.println("--发送简单的邮件消息,发生异常："+ e.fillInStackTrace());
        }
        return emailModel;
    }




}
