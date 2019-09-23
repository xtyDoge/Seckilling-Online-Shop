package com.miaosha.service;

import com.miaosha.error.BusinessException;
import com.miaosha.service.model.ItemModel;

import java.util.List;

public interface ItemService {
    // 创建商品
    ItemModel createItem(ItemModel itemModel) throws BusinessException;

    // 列表浏览
    List<ItemModel> listItem();

    // 详情浏览
    ItemModel getItemById(Integer id) throws BusinessException;

    // 减少库存
    boolean decreaseStock(Integer itemId,Integer amount);

    // 增加销量
    boolean increaseSales(Integer itemId, Integer amount) throws BusinessException;
}
