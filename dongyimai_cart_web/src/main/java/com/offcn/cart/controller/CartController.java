package com.offcn.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.offcn.cart.service.CartService;
import com.offcn.entity.Result;
import com.offcn.group.Cart;
import com.offcn.utils.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Auther: ysp
 * @Date: 2020/10/16 11:05
 * @Description: 购物车的控制器
 */
@RestController
@RequestMapping("/cart")
public class CartController {

    @Reference
    private CartService cartService;


    //查询购物车列表
    @RequestMapping("/findCartList")
    public List<Cart> findCartList(HttpServletRequest request, HttpServletResponse response) {
        //判断用户是否登录
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        //1.从Cookie中查询购物车列表
        String cartListStr = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
        if (StringUtils.isEmpty(cartListStr)) {
            cartListStr = "[]";   //如果集合为空，初始化一个数据结构
        }
        List<Cart> cartList_cookie = JSON.parseArray(cartListStr, Cart.class);
        //未登录  从cookie
        if (username.equals("anonymousUser")) {
            return cartList_cookie;
        } else { //已登录 从redis
            //1.从cookie中取得数据
            List<Cart> cartList_redis = cartService.findCartListFromRedis(username);
            //2.如果cookie中购物车集合，则做合并
            if (!CollectionUtils.isEmpty(cartList_cookie)) {
                cartList_redis = cartService.mergeCartList(cartList_redis, cartList_cookie);
                //3.更新Redis
                cartService.saveCartListToRedis(username, cartList_redis);
                //4.清空cookie
                CookieUtil.deleteCookie(request, response, "cartList");
            }
            return cartList_redis;
        }


    }


    //添加购物车
    @RequestMapping("/addGoodsToCartList")
    @CrossOrigin(origins = "http://localhost:9105",allowCredentials = "true")
    public Result addGoodsToCartList(Long itemId, Integer num, HttpServletRequest request, HttpServletResponse response) {


        //允许跨域请求
        //response.setHeader("Access-Control-Allow-Origin","http://localhost:9105");
        //允许跨域请求带参数
        //response.setHeader("Access-Control-Allow-Credentials","true");

        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            //1.查询得到购物车集合
            List<Cart> cartList = this.findCartList(request, response);
            //2.添加购物车
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);
            if (username.equals("anonymousUser")) {   //未登录 则存Cookie
                //3.更新cookie
                CookieUtil.setCookie(request, response, "cartList", JSON.toJSONString(cartList), 3600 * 24, "UTF-8");
            } else {  //已登录 存Redis
                cartService.saveCartListToRedis(username, cartList);
            }
            return new Result(true, "添加购物车成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "添加购物车失败");
        }

    }


}
