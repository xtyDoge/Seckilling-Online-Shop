package com.miaosha.aspect;

import com.miaosha.error.BusinessException;
import com.miaosha.error.EmBusinessError;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class LoginAuthorizationAspect {

    @Resource
    private RedisTemplate redisTemplate;

    @Pointcut("@annotation(com.miaosha.aspect.annotation.CheckAuthorization)")
    public void pointcut(){

    }

    @Around("pointcut()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();
        Cookie[] cookies =  request.getCookies();
        for(Cookie c : cookies){
            System.out.println(c.getName());
            if(StringUtils.equals(c.getName(),"JSESSIONID")){
                // c.getValue() 等于查到的sessionId吗
                String sessionId = c.getValue();
                System.out.println(sessionId);
                Boolean isLogin = (Boolean) redisTemplate.opsForValue().get(sessionId);
                // 未登录
                if(isLogin == null || !isLogin.booleanValue()){
                    throw new BusinessException(EmBusinessError.USER_NOT_LOGIN);
                }
                else{
                    // 已登录
                    return joinPoint.proceed(joinPoint.getArgs());
                }
            }
        }
        // cookie没有
        throw new BusinessException(EmBusinessError.USER_NOT_LOGIN);
    }
}
