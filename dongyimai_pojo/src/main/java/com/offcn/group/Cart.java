package com.offcn.group;

import com.offcn.pojo.TbOrderItem;

import java.io.Serializable;
import java.util.List;

/**
 * @Auther: ysp
 * @Date: 2020/10/16 10:03
 * @Description:  购物车的复合实体类
 */
public class Cart implements Serializable {

    private String sellerId;   //商家ID
    private String sellerName; //商家名称
    private List<TbOrderItem> orderItemList;    //订单详情列表

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public List<TbOrderItem> getOrderItemList() {
        return orderItemList;
    }

    public void setOrderItemList(List<TbOrderItem> orderItemList) {
        this.orderItemList = orderItemList;
    }
}
