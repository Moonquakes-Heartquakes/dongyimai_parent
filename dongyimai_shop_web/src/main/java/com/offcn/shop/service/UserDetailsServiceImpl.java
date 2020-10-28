package com.offcn.shop.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.offcn.pojo.TbSeller;
import com.offcn.sellergoods.service.SellerService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * @Auther: ysp
 * @Date: 2020/9/24 15:07
 * @Description: 安全验证自定义认证类
 */
public class UserDetailsServiceImpl implements UserDetailsService {

    @Reference
    private SellerService sellerService;


    public UserDetails loadUserByUsername(String sellerId) throws UsernameNotFoundException {
        //1.声明权限集合
        List<GrantedAuthority> list = new ArrayList<GrantedAuthority>();
        //2.添加权限
        list.add(new SimpleGrantedAuthority("ROLE_SELLER"));
        //根据sellerId查询商家信息
        TbSeller seller = sellerService.findOne(sellerId);
        //判断商家信息是否合法
        if (null != seller) {
            //判断审核状态为 审核通过
            if (seller.getStatus().equals("1")) {
                //3.登录认证
                System.out.println(sellerId+"---"+seller.getPassword());
                return new User(sellerId, seller.getPassword(), list);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}
