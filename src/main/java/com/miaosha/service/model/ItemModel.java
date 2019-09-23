package com.miaosha.service.model;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ItemModel {
    private Integer id;

    // 商品名
    @NotBlank(message = "商品名不能为空")
    private String title;

    // 价格
    @NotNull(message = "商品价格不能为空")
    @Min(value = 0,message = "商品价格不能为负")
    private BigDecimal price;

    // 库存
    @NotNull(message = "库存不能不填")
    @Min(value = 0,message = "商品库存必须大于0")
    private Integer stock;

    // 描述
    @NotNull(message = "描述不能为空")
    private String description;

    // 销量
    private Integer sales;

    // 描述图片URL
    @NotNull(message = "描述信息不能为空")
    private String imgUrl;

    // 使用聚合模型 集成PromoModel
    private PromoModel promoModel;

    public PromoModel getPromoModel() {
        return promoModel;
    }

    public void setPromoModel(PromoModel promoModel) {
        this.promoModel = promoModel;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSales() {
        return sales;
    }

    public void setSales(Integer sales) {
        this.sales = sales;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
