package com.miaosha.service;

import com.miaosha.error.BusinessException;
import com.miaosha.service.model.OrderModel;

public interface OrderService {
    // 1 通过前端url传过来秒杀活动id，然后下单接口内校验对应id是否属于对应商品且活动已开始
    // 2 直接在下单接口内判断对应商品是否存在秒杀会活动，若存在进行中的则以秒杀活动下单
    OrderModel createOrder(Integer userId,Integer itemId, Integer promoId,Integer amount) throws BusinessException;
}
