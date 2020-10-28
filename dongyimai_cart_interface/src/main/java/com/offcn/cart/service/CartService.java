package com.offcn.cart.service;

import com.offcn.group.Cart;

import java.util.List;

/**
 * @Auther: ysp
 * @Date: 2020/10/16 10:06
 * @Description:  购物车接口
 */
public interface CartService {


    /**
     * 添加商品到购物车集合
     * @param srcCartList   原购物车集合
     * @param itemId    SKUID
     * @param num       购买数量
     * @return
     */
    public List<Cart> addGoodsToCartList(List<Cart> srcCartList,Long itemId,Integer num);


    /**
     * 从缓存中读取购物车列表
     * @param username
     * @return
     */
    public List<Cart> findCartListFromRedis(String username);

    /**
     * 向缓存中存储购物车列表
     * @param username
     * @param cartList
     */
    public void saveCartListToRedis(String username,List<Cart> cartList);

    /**
     * 合并购物车
     * @param cartList_redis
     * @param cartList_cookie
     * @return
     */
    public List<Cart> mergeCartList(List<Cart> cartList_redis,List<Cart> cartList_cookie);
}
