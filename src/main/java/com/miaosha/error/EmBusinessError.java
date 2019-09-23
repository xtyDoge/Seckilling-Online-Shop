package com.miaosha.error;

public enum EmBusinessError implements CommonError{

    //10001开头为通用错误类型
    PARAMETER_VALIDATION_ERROR(10001,"参数不合法"),
    UNKNOWN_ERROR(10002,"未知错误"),

    //20000开头为用户信息相关错误定义
    USER_NOT_EXIST(20001,"用户不存在"),
    USER_LOGIN_FAIL(20002,"用户登录信息不正确"),
    USER_NOT_LOGIN(20003,"用户还未登录"),

    //30000开头为商品信息错误定义
    ITEM_NOT_EXIST(30001,"商品不存在"),
    ITEM_NO_STOCK(30002,"商品库存不足")

    ;

    private EmBusinessError(int errCode,String errMsg){
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    private int errCode;
    private String errMsg;

    @Override
    public int getErrCode() {
        return this.errCode;
    }

    @Override
    public String getErrMsg() {
        return this.errMsg;
    }

    //定制化错误信息
    @Override
    public CommonError setErrMsg(String errMsg) {
        this.errMsg = errMsg;
        return this;
    }
}
