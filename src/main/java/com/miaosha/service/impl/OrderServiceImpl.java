package com.miaosha.service.impl;

import com.miaosha.dao.OrderInfoDOMapper;
import com.miaosha.dao.SequenceDOMapper;
import com.miaosha.dataobject.OrderInfoDO;
import com.miaosha.dataobject.SequenceDO;
import com.miaosha.error.BusinessException;
import com.miaosha.error.EmBusinessError;
import com.miaosha.service.ItemService;
import com.miaosha.service.OrderService;
import com.miaosha.service.PromoService;
import com.miaosha.service.UserService;
import com.miaosha.service.model.ItemModel;
import com.miaosha.service.model.OrderModel;
import com.miaosha.service.model.PromoModel;
import com.miaosha.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderInfoDOMapper orderInfoDOMapper;

    @Autowired
    private SequenceDOMapper sequenceDOMapper;

    @Autowired
    private PromoService promoService;

    @Override
    @Transactional
    public OrderModel createOrder(Integer userId, Integer itemId,Integer promoId, Integer amount) throws BusinessException {
        // 1 校验下单状态（3个参数是否正确）
        // select * from user_info where id = userID if null throw error else create
        UserModel userModel = userService.getUserById(userId);
        ItemModel itemModel = itemService.getItemById(itemId);
        PromoModel promoModel = promoService.getPromoByItemId(promoId);
        if(userModel == null){
            throw new BusinessException(EmBusinessError.USER_NOT_EXIST);
        }
        if(itemModel == null){
            throw new BusinessException(EmBusinessError.ITEM_NOT_EXIST);
        }
        if(amount <= 0 || amount > 99){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"购买数量必须大于0小于100");
        }
        if(promoId != null){
            // 是否适用于该商品 itemModel带一个promoId,传进来又一个promoId，itemModel那个带的是实际上是刚从数据库查到的
            if(promoId.intValue() != itemModel.getPromoModel().getItemId()){
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"秒杀活动与商品不一致");
            }
            else if(itemModel.getPromoModel().getStatus() != 2){
                // 活动是否已经开始
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"秒杀活动无效");
            }
        }


        // 2 落单减库存（落单锁？） 支付减库存？
        boolean result =  itemService.decreaseStock(itemId,amount);
        if(!result){
            throw new BusinessException(EmBusinessError.ITEM_NO_STOCK,"库存不足");
        }
        itemService.increaseSales(itemId,amount);

        // 3 订单入库-生成DO

        OrderModel orderModel = new OrderModel();
        // 生成订单号
        orderModel.setId(generateOrderNO());
        orderModel.setPromoId(promoId);
        orderModel.setAmount(amount);
        orderModel.setItemId(itemId);
        orderModel.setUserId(userId);
        // 如果秒杀活动存在，则取下单价格为秒杀价格
        if(promoId != null){
            orderModel.setItemPrice(itemModel.getPromoModel().getPromoItemPrice());
        }
        else{
            orderModel.setItemPrice(itemModel.getPrice());
        }

        orderModel.setOrderPrice(orderModel.getItemPrice().multiply(new BigDecimal(amount)));
        OrderInfoDO orderInfoDO = convertOrderInfoDOFromOrderModel(orderModel);
        orderInfoDOMapper.insertSelective(orderInfoDO);

        // 4 返回前端 OrderVO 对象

        return null;
    }

    private OrderInfoDO convertOrderInfoDOFromOrderModel(OrderModel orderModel){
        OrderInfoDO orderInfoDO = new OrderInfoDO();
        BeanUtils.copyProperties(orderModel,orderInfoDO);
        orderInfoDO.setItemPrice(orderModel.getItemPrice().doubleValue());
        orderInfoDO.setOrderPrice(orderModel.getOrderPrice().doubleValue());
        return orderInfoDO;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    String generateOrderNO(){
        // 8位时间
        StringBuilder stringBuilder = new StringBuilder(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE).replace("-",""));
        // 6位自增序列
        int sequence = 0;
        SequenceDO sequenceDO = sequenceDOMapper.getSequenceByName("order_info");
        sequence = sequenceDO.getCurrentValue();
        sequenceDO.setCurrentValue(sequence + sequenceDO.getStep());
        sequenceDOMapper.updateByPrimaryKey(sequenceDO);
        //凑6位数
        String sequenceStr = String.valueOf(sequence);
        for(int i=0;i<6-sequenceStr.length();i++){
            stringBuilder.append(0);
        }
        stringBuilder.append(sequenceStr);

        // 2位分库分表位，类似哈希
        stringBuilder.append("00");
        return stringBuilder.toString();
    }


}
