package com.offcn.search.service;

import com.offcn.pojo.TbItem;

import java.util.List;
import java.util.Map;

/**
 * @Auther: ysp
 * @Date: 2020/9/30 16:06
 * @Description:   商品查询接口
 */

public interface ItemSearchService {

    /**
     * 搜索查询
     * @param searchMap
     * @return
     */
    public Map<String,Object> search(Map<String,Object> searchMap);

    /**
     * 将审核通过的SKU集合导入到solr
     * @param list
     */
    public void importItem(List<TbItem> list);

    /**
     * 根据删除SKU的ID删除solr中的信息
     * @param goodsIdsList
     */
    public void deleteByGoodsIds(List goodsIdsList);
}
