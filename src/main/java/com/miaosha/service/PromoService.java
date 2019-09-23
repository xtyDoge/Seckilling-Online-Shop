package com.miaosha.service;

import com.miaosha.error.BusinessException;
import com.miaosha.service.model.PromoModel;

public interface PromoService {
    PromoModel getPromoByItemId(Integer itemId) throws BusinessException;
}
