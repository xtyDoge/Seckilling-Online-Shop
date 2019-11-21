package com.miaosha.service.impl;

import com.miaosha.dao.PromoDOMapper;
import com.miaosha.dataobject.PromoDO;
import com.miaosha.error.BusinessException;
import com.miaosha.error.EmBusinessError;
import com.miaosha.service.ItemService;
import com.miaosha.service.PromoService;
import com.miaosha.service.model.ItemModel;
import com.miaosha.service.model.PromoModel;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PromoServiceImpl implements PromoService {

    @Autowired
    private PromoDOMapper promoDOMapper;

    @Autowired
    private ItemService itemService;

    @Override
    public PromoModel getPromoByItemId(Integer itemId) throws BusinessException {
        // 存在这件商品？
//        ItemModel itemModel = itemService.getItemById(itemId);
//        if(itemModel == null){
//            throw new BusinessException(EmBusinessError.ITEM_NOT_EXIST);
//        }
        // 存在这个秒杀活动？
        PromoDO promoDO = promoDOMapper.selectByItemId(itemId);
        PromoModel promoModel = convertPromoModelFromDO(promoDO);
        if(promoModel == null){
            return null;
//            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"秒杀活动不存在");
        }
        // 活动状态，1表示未开始，2表示进行中，3表示已结束
        if(promoModel.getStartDate().isAfterNow()){
            promoModel.setStatus(1);
        }
        else if(promoModel.getEndDate().isBeforeNow()){
            promoModel.setStatus(3);
        }
        else{promoModel.setStatus(2);}

        return promoModel;
    }


    private PromoModel convertPromoModelFromDO(PromoDO promoDO){
        if(promoDO == null){
            return null;
        }
        PromoModel promoModel = new PromoModel();
        BeanUtils.copyProperties(promoDO,promoModel);
        promoModel.setPromoItemPrice(new BigDecimal(promoDO.getPromoItemPrice()));
        promoModel.setStartDate(new DateTime(promoDO.getStartDate()));
        promoModel.setEndDate(new DateTime(promoDO.getEndDate()));

        return promoModel;
    }
}
