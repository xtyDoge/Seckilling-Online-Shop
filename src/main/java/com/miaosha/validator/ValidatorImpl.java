package com.miaosha.validator;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;
import java.util.function.Consumer;


@Component
public class ValidatorImpl implements InitializingBean {

    private Validator validator;

    // 实现校验方法 返回校验结果
    public ValidationResult validate(Object bean){
        ValidationResult result = new ValidationResult();
        Set<ConstraintViolation<Object>> constraintViolationSet = validator.validate(bean);
        if(constraintViolationSet.size() > 0){
            constraintViolationSet.forEach(new Consumer<ConstraintViolation<Object>>() {
                @Override
                public void accept(ConstraintViolation<Object> constraintViolation) {
                    String errMsg = constraintViolation.getMessage();
                    String propertyName = constraintViolation.getPropertyPath().toString();
                    result.getErrorMsgMap().put(propertyName,errMsg);
                }
            });
        }
        return result;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();


    }
}
