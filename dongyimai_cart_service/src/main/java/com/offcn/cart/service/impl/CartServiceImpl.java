package com.offcn.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.offcn.cart.service.CartService;
import com.offcn.group.Cart;
import com.offcn.mapper.TbItemMapper;
import com.offcn.pojo.TbItem;
import com.offcn.pojo.TbOrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @Auther: ysp
 * @Date: 2020/10/16 10:11
 * @Description:
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 添加商品到购物车集合
     *
     * @param srcCartList 原购物车集合
     * @param itemId      SKUID
     * @param num         购买数量
     * @return
     */
    public List<Cart> addGoodsToCartList(List<Cart> srcCartList, Long itemId, Integer num) {
        //1.根据SKUId查询商品详情信息
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        //判断非法操作
        if (null == item) {
            throw new RuntimeException("该商品不存在");
        }
        if (!item.getStatus().equals("1")) {
            throw new RuntimeException("该商品状态异常");
        }
        String sellerId = item.getSellerId();    //商家ID
        String sellerName = item.getSeller();    //商家名称
        //2.根据sellerId 判断该购物车对象是否在原购物车列表中存在
        Cart cart = this.searchCartBySellerId(srcCartList, sellerId);
        if (cart == null) {   //3.如果不存在，重新创建购物车对象
            cart = new Cart();
            cart.setSellerId(sellerId);
            cart.setSellerName(sellerName);
            List<TbOrderItem> orderItemList = new ArrayList<TbOrderItem>();
            TbOrderItem orderItem = this.createOrderItem(item, num);
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);   //放置购物车对象
            srcCartList.add(cart);                   //放置购物车集合
        } else {  //添加一个商品记录
            //4.根据itemId  判断该商品在商家中是否存在
            TbOrderItem orderItem = this.searchOrderItemByItemId(cart.getOrderItemList(), itemId);
            if (orderItem == null) {    //在购物车对象中不存在，则重新创建订单详情对象
                orderItem = this.createOrderItem(item, num);
                cart.getOrderItemList().add(orderItem);      //放置订单详情集合
            } else {  //5.修改购买数量
                orderItem.setNum(orderItem.getNum() + num);     //修改购买数量
                orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue() * orderItem.getNum()));    //重新计算总价格

                //判断商品的数量如果小于1，移除该商品
                if (orderItem.getNum() < 1) {
                    cart.getOrderItemList().remove(orderItem);
                }
                //如果该商家下没有任何商品，则将该商家的购物车对象移除
                if (cart.getOrderItemList().size() == 0) {
                    srcCartList.remove(cart);
                }
            }

        }
        return srcCartList;
    }

    /**
     * 从缓存中读取购物车列表
     *
     * @param username
     * @return
     */
    public List<Cart> findCartListFromRedis(String username) {
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
        if (cartList == null) {
            cartList = new ArrayList<Cart>();
        }
        return cartList;
    }

    /**
     * 向缓存中存储购物车列表
     *
     * @param username
     * @param cartList
     */
    public void saveCartListToRedis(String username, List<Cart> cartList) {
        redisTemplate.boundHashOps("cartList").put(username, cartList);
        System.out.println("向缓存中存储购物车列表成功！");

    }

    /**
     * 合并购物车
     *
     * @param cartList_redis
     * @param cartList_cookie
     * @return
     */
    public List<Cart> mergeCartList(List<Cart> cartList_redis, List<Cart> cartList_cookie) {
        for (Cart cart : cartList_cookie) {
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                cartList_redis = this.addGoodsToCartList(cartList_redis, orderItem.getItemId(), orderItem.getNum());
            }
        }
        return cartList_redis;
    }

    //根据sellerId判断购物车是否存在
    private Cart searchCartBySellerId(List<Cart> srcCartList, String sellerId) {
        for (Cart cart : srcCartList) {
            if (cart.getSellerId().equals(sellerId)) {
                return cart;
            }
        }
        return null;
    }

    //根据skuId判断是否在该商家中存在
    private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList, Long itemId) {
        for (TbOrderItem orderItem : orderItemList) {
            if (orderItem.getItemId().longValue() == itemId.longValue()) {
                return orderItem;
            }
        }
        return null;
    }


    //创建订单详情
    private TbOrderItem createOrderItem(TbItem item, Integer num) {
        if (num < 1) {
            throw new RuntimeException("购买数量非法");
        }

        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setItemId(item.getId());                                              //skuId
        orderItem.setGoodsId(item.getGoodsId());                                        //spuId
        orderItem.setTitle(item.getTitle());                                            //商品名称
        orderItem.setPrice(item.getPrice());                                            //商品单价
        orderItem.setNum(num);                                                          //购买数量
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue() * num));   //购买总价格
        orderItem.setPicPath(item.getImage());                                          //图片地址
        orderItem.setSellerId(item.getSellerId());                                      //商家ID
        return orderItem;
    }


}
