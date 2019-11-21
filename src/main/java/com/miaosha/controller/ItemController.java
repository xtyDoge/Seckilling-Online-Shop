package com.miaosha.controller;

import com.miaosha.controller.viewobject.ItemVO;
import com.miaosha.error.BusinessException;
import com.miaosha.error.EmBusinessError;
import com.miaosha.response.CommonReturnType;
import com.miaosha.service.ItemService;
import com.miaosha.service.UserService;
import com.miaosha.service.model.EmailModel;
import com.miaosha.service.model.ItemModel;
import io.swagger.annotations.ApiOperation;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*",allowCredentials = "true",maxAge = 3600,methods = {RequestMethod.POST,RequestMethod.GET})
@Controller("item")
@RequestMapping("/item")
public class ItemController extends BaseController {
    // 商品Controller 提供的服务包括 1.添加商品 2.商品列表 3.商品详情
    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    // 商品详情
    @ApiOperation(value="获取商品详情", notes="根据id来获取商品详细信息")
    @GetMapping("/get")
    @ResponseBody
    public CommonReturnType getItem(@RequestParam(name = "id") Integer id) throws BusinessException {

        ItemModel itemModel = itemService.getItemById(id);
        if(itemModel == null){
            throw new BusinessException(EmBusinessError.ITEM_NOT_EXIST);
        }
        ItemVO itemVO = convertItemVOFromItemModel(itemModel);
        return CommonReturnType.create(itemVO);
    }

    @PostMapping("/list")
    @GetMapping("/list")
    @ResponseBody
    public CommonReturnType list(){
        List<ItemModel> itemModelList = itemService.listItem();
        List<ItemVO> itemVOList = itemModelList.stream().map(itemModel -> {
            ItemVO itemVO = convertItemVOFromItemModel(itemModel);
            return itemVO;
        }).collect(Collectors.toList());

        return CommonReturnType.create(itemVOList);
    }

    @GetMapping("/query")
    @ResponseBody
    public CommonReturnType query(@RequestParam(name = "property") String property,@RequestParam(name = "pageNum",required = false) Integer pageNum,@RequestParam(name = "pageSize",required = false) Integer pageSize){
        List<ItemModel> itemModelList;
        if(pageNum == null || pageSize == null){
            itemModelList = itemService.listItemByProperty(0,10,property);
        }
        else{
            itemModelList = itemService.listItemByProperty(pageNum,pageSize,property);
        }
        List<ItemVO> itemVOList = itemModelList.stream().map(itemModel -> {
            ItemVO itemVO = convertItemVOFromItemModel(itemModel);
            return itemVO;
        }).collect(Collectors.toList());

        return CommonReturnType.create(itemVOList);
    }


    // 添加商品
    @GetMapping("/create")
    @PostMapping("/create")
    @ResponseBody
    public CommonReturnType createItem(@RequestParam(name = "title") String title,
                                       @RequestParam(name = "description") String description,
                                       @RequestParam(name = "price") BigDecimal price,
                                       @RequestParam(name = "stock") Integer stock,
                                       @RequestParam(name = "imgUrl") String imgUrl) throws BusinessException, InterruptedException {
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
        //发送注册用户信息
        System.out.println("准备发送邮件");
        userService.sendEmail(new EmailModel("","新品上架-"+itemModel.getTitle(),String.format("新品%s上市，详情：%s，仅售%d元",itemModel.getTitle(),itemModel.getDescription(),itemModel.getPrice().intValue())));

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
