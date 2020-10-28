package com.offcn.page.service;

/**
 * @Auther: ysp
 * @Date: 2020/10/12 11:15
 * @Description:  网页静态化生成接口
 */
public interface ItemPageService {

    /**
     * 生成商品详情页
     * @param goodsId
     * @return
     */
    public boolean genItemHtml(Long goodsId);

    /**
     * 批量删除商品详情页
     * @param goodsIds
     * @return
     */
    public boolean deleteItemHtml(Long[] goodsIds);
}
