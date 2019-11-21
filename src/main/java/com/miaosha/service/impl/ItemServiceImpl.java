package com.miaosha.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.miaosha.dao.ItemDOMapper;
import com.miaosha.dao.ItemStockDOMapper;
import com.miaosha.dataobject.ItemDO;
import com.miaosha.dataobject.ItemStockDO;
import com.miaosha.error.BusinessException;
import com.miaosha.error.EmBusinessError;
import com.miaosha.service.ItemService;
import com.miaosha.service.PromoService;
import com.miaosha.service.model.ItemModel;
import com.miaosha.service.model.PromoModel;
import com.miaosha.validator.ValidationResult;
import com.miaosha.validator.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ValidatorImpl validator;

    @Autowired
    private ItemDOMapper itemDOMapper;

    @Autowired
    private ItemStockDOMapper itemStockDOMapper;

    @Autowired
    private PromoService promoService;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Override
    @Transactional
    public ItemModel createItem(ItemModel itemModel) throws BusinessException {

        // 校验入参
        ValidationResult result = validator.validate(itemModel);
        if(result.isHasErrors()){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,result.getErrMsg());
        }
        // 转化ItemModel 为 DO 写入数据库
        ItemDO itemDO = convertItemDOFromItemModel(itemModel);
        // 写入数据库
        itemDOMapper.insertSelective(itemDO);
        itemModel.setId(itemDO.getId());

        ItemStockDO itemStockDO = convertItemStockDOFromItemModel(itemModel);
        itemStockDOMapper.insertSelective(itemStockDO);
        // 返回创建完成的对象(从数据库里扒过来)
        return getItemById(itemModel.getId());
    }

    @Override
    public List<ItemModel> listItem() {

        //根据活动号列出？
        List<ItemDO> itemDOList = itemDOMapper.listItem();
        List<ItemModel> itemModelList = itemDOList.stream().map(itemDO -> {
           ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());
           ItemModel itemModel = convertItemModelFromItemDOAndItemStockDO(itemDO,itemStockDO);
           return itemModel;
        }).collect(Collectors.toList());
        return itemModelList;
    }

    @Override
    public List<ItemModel> listItemByProperty(Integer pageNum, Integer pageSize, String property) {
        // PageHelper拦截
        PageHelper.startPage(pageNum,pageSize);
        List<ItemDO> itemDOList = itemDOMapper.list(property);
        PageInfo<ItemDO> pageInfo = new PageInfo<ItemDO>(itemDOList);
        // 转换成List
        List<ItemDO> itemCut = pageInfo.getList();
        List<ItemModel> itemModels = itemCut.stream().map(itemDO -> {
            ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());
            ItemModel itemModel = convertItemModelFromItemDOAndItemStockDO(itemDO,itemStockDO);
            return itemModel;
        }).collect(Collectors.toList());
        return itemModels;
    }

    @Override
    public ItemModel getItemById(Integer id) throws BusinessException {

        // 数据库拉取 商品信息+库存数量
        ItemDO itemDO = itemDOMapper.selectByPrimaryKey(id);
        if(itemDO == null){
            return null;
        }
        ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(id);
        // 组装为ItemModel
        ItemModel itemModel = convertItemModelFromItemDOAndItemStockDO(itemDO,itemStockDO);
        // 获取秒杀活动信息
        PromoModel promoModel = promoService.getPromoByItemId(id);
        // 秒杀活动存在且未结束
        if(promoModel != null && promoModel.getStatus() != 3){
            itemModel.setPromoModel(promoModel);
        }

        return itemModel;
    }

    @Override
    @Transactional
    public boolean decreaseStock(Integer itemId, Integer amount) {
        // 出现了 加行锁
        int affectedRows = itemStockDOMapper.decreaseStock(itemId,amount);
        if(affectedRows > 0){
            //更新库存成功
            return true;
        }
        else{
            //更新库存失败
            return false;
        }
    }

    @Override
    @Transactional
    public boolean increaseSales(Integer itemId, Integer amount) throws BusinessException {
        // 取出来再加1？ 2条查询
        // 硬update 看affectedRows 一条查询
        int affectedRows = itemDOMapper.increaseSales(itemId,amount);
        if(affectedRows > 0){
            //更新销量成功
            return true;
        }
        else{
            //更新销量失败
            return false;
        }
    }

    private ItemDO convertItemDOFromItemModel(ItemModel itemModel){
        if(itemModel == null){
            return null;
        }
        ItemDO itemDO = new ItemDO();
        BeanUtils.copyProperties(itemModel,itemDO);
        itemDO.setPrice(itemModel.getPrice().doubleValue());
        return itemDO;
    }

    private ItemStockDO convertItemStockDOFromItemModel(ItemModel itemModel){
        if(itemModel == null){
            return null;
        }
        ItemStockDO itemStockDO = new ItemStockDO();
        itemStockDO.setStock(itemModel.getStock());
        itemStockDO.setItemId(itemModel.getId());
        return itemStockDO;
    }

    private ItemModel convertItemModelFromItemDOAndItemStockDO(ItemDO itemDO,ItemStockDO itemStockDO){
        ItemModel itemModel = new ItemModel();
        BeanUtils.copyProperties(itemDO,itemModel);
        itemModel.setPrice(BigDecimal.valueOf(itemDO.getPrice()));
        itemModel.setStock(itemStockDO.getStock());
        return itemModel;
    }


}
