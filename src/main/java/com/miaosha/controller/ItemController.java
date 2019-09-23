package com.miaosha.controller;

import com.miaosha.controller.viewobject.ItemVO;
import com.miaosha.error.BusinessException;
import com.miaosha.error.EmBusinessError;
import com.miaosha.response.CommonReturnType;
import com.miaosha.service.ItemService;
import com.miaosha.service.model.ItemModel;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*",allowCredentials = "true",maxAge = 3600,methods = {RequestMethod.POST,RequestMethod.GET})
@Controller("item")
@RequestMapping("/item")
public class ItemController extends BaseController {
    // 商品Controller 提供的服务包括 1.添加商品 2.商品列表 3.商品详情
    @Autowired
    private ItemService itemService;

    // 商品详情
    @RequestMapping("/get")
    @ResponseBody
    public CommonReturnType getItem(@RequestParam(name = "id") Integer id) throws BusinessException {

        ItemModel itemModel = itemService.getItemById(id);
        if(itemModel == null){
            throw new BusinessException(EmBusinessError.ITEM_NOT_EXIST);
        }
        ItemVO itemVO = convertItemVOFromItemModel(itemModel);
        return CommonReturnType.create(itemVO);
    }

    @RequestMapping("/list")
    @ResponseBody
    public CommonReturnType list(){
        List<ItemModel> itemModelList = itemService.listItem();
        List<ItemVO> itemVOList = itemModelList.stream().map(itemModel -> {
            ItemVO itemVO = convertItemVOFromItemModel(itemModel);
            return itemVO;
        }).collect(Collectors.toList());

        return CommonReturnType.create(itemVOList);
    }


    // 添加商品
    @RequestMapping("/create")
    @ResponseBody
    public CommonReturnType createItem(@RequestParam(name = "title") String title,
                                       @RequestParam(name = "description") String description,
                                       @RequestParam(name = "price") BigDecimal price,
                                       @RequestParam(name = "stock") Integer stock,
                                       @RequestParam(name = "imgUrl") String imgUrl) throws BusinessException {
        // 组装ItemModel，如果符合验证规则验证器会报错
        ItemModel itemModel = new ItemModel();
        itemModel.setPrice(price);
        itemModel.setStock(stock);
        itemModel.setDescription(description);
        itemModel.setImgUrl(imgUrl);
        itemModel.setTitle(title);
        //数据库操作
        ItemModel itemModelForReturn =  itemService.createItem(itemModel);
        //返回给前端？
        ItemVO itemVO = convertItemVOFromItemModel(itemModelForReturn);
        return CommonReturnType.create(itemVO);
    }


    private ItemVO convertItemVOFromItemModel(ItemModel itemModel){
        if(itemModel == null){
            return null;
        }
        ItemVO itemVO = new ItemVO();
        BeanUtils.copyProperties(itemModel,itemVO);
        if(itemModel.getPromoModel()!=null){
            itemVO.setPromoStatus(itemModel.getPromoModel().getStatus());
            itemVO.setPromoPrice(itemModel.getPromoModel().getPromoItemPrice());
            itemVO.setPromoId(itemModel.getPromoModel().getId());
            itemVO.setStartDateTime(itemModel.getPromoModel().getStartDate().toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")));
        }
        else{
            itemVO.setPromoStatus(0);
        }
        return itemVO;
    }



}
