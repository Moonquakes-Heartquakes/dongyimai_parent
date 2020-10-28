package com.offcn.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.promeg.pinyinhelper.Pinyin;
import com.offcn.pojo.TbItem;
import com.offcn.search.service.ItemSearchService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Auther: ysp
 * @Date: 2020/9/30 16:15
 * @Description:
 */
@Service
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 搜索查询
     *
     * @param searchMap
     * @return
     */
    public Map<String, Object> search(Map<String, Object> searchMap) {
        Map resultMap = new HashMap();
       /* //1.创建查询条件对象
        Query query = new SimpleQuery();
        //2.创建查询条件选择器，并设置查询条件   is  进行分词查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        //3.将选择器设置回条件对象
        query.addCriteria(criteria);
        //4.执行查询
        ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);
        //5.返回查询集合
        List<TbItem> itemList = page.getContent();
        resultMap.put("rows", itemList);*/
        if (StringUtils.isNotEmpty((String) searchMap.get("keywords")) && ((String) searchMap.get("keywords")).indexOf(" ") > -1) {
            String keywords = (String) searchMap.get("keywords");
            keywords = keywords.replace(" ", "");   //去除空格
            searchMap.put("keywords", keywords);
        }


        resultMap.putAll(this.searchList(searchMap));

        List<String> categoryList = this.searchCategoryList(searchMap);
        resultMap.put("categoryList", categoryList);
        //得到查询条件为分类
        String category = (String) searchMap.get("category");
        if (StringUtils.isNotEmpty(category)) {
            //根据分类重新查询品牌和规格列表
            resultMap.putAll(this.searchBrandAndSpecList(category));
        } else {
            //判断分类列表元素是否大于0，是多个，默认查询第一个分类
            if (categoryList.size() > 0) {
                resultMap.putAll(this.searchBrandAndSpecList(categoryList.get(0)));
            }
        }
        return resultMap;
    }

    /**
     * 将审核通过的SKU集合导入到solr
     *
     * @param list
     */
    public void importItem(List<TbItem> itemList) {
        for (TbItem item : itemList) {
            Map<String, String> specMap = JSON.parseObject(item.getSpec(), Map.class);

            Map<String, String> pinyinMap = new HashMap<String, String>();
            for (String key : specMap.keySet()) {
                //将key做拼音转换
                pinyinMap.put(Pinyin.toPinyin(key, "").toLowerCase(), specMap.get(key));
            }

            item.setSpecMap(pinyinMap);

            System.out.println(item.getTitle() + "---" + item.getPrice());
        }

        solrTemplate.saveBeans(itemList);
        solrTemplate.commit();
        System.out.println("导入成功");



    }

    /**
     * 根据删除SKU的ID删除solr中的信息
     *
     * @param goodsIdsList
     */
    public void deleteByGoodsIds(List goodsIdsList) {
        Query query = new SimpleQuery();
        Criteria criteria = new Criteria("item_goodsid").in(goodsIdsList);
        query.addCriteria(criteria);
        //执行删除
        solrTemplate.delete(query);
        solrTemplate.commit();
        System.out.println("删除成功");


    }


    private Map<String, Object> searchList(Map<String, Object> searchMap) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        //1.创建高亮查询条件对象
        HighlightQuery query = new SimpleHighlightQuery();
        //2.设置需要处理高亮的字段
        HighlightOptions highlightOptions = new HighlightOptions();
        highlightOptions.addField("item_title");
        //3.设置处理高亮的属性
        highlightOptions.setSimplePrefix("<em style='color:red'>");//前缀
        highlightOptions.setSimplePostfix("</em>");  //后缀
        //4.将设置后的属性放回到查询条件对象
        query.setHighlightOptions(highlightOptions);
        //5.执行高亮查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //根据分类筛选结果
        if (!"".equals(searchMap.get("category"))) {
            //创建查询条件选择器
            Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
            //创建过滤查询对象
            FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
            //设置回原有的查询对象中
            query.addFilterQuery(filterQuery);
        }
        //根据品牌筛选结果
        if (!"".equals(searchMap.get("brand"))) {
            //创建查询条件选择器
            Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
            //创建过滤查询对象
            FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
            //设置回原有的查询对象中
            query.addFilterQuery(filterQuery);
        }
        //根据规格筛选结果
        if (null != searchMap.get("spec")) {
            Map<String, String> specMap = (Map<String, String>) searchMap.get("spec");
            //遍历Map集合的key值  key ['网络','机身内存'] ----》 item_spec_wangluo
            for (String key : specMap.keySet()) {
                Criteria filterCriteria = new Criteria("item_spec_" + Pinyin.toPinyin(key, "").toLowerCase()).is(specMap.get(key));
                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }
        //根据价格筛选结果
        if (!"".equals(searchMap.get("price"))) {
            // 0-500  拆分 [0]=0  [1]=500
            String[] strs = ((String) searchMap.get("price")).split("-");
            if (!strs[0].equals("0")) {
                Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(strs[0]);
                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
            if (!strs[1].equals("*")) {
                Criteria filterCriteria = new Criteria("item_price").lessThan(strs[1]);
                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }

        }

        //根据分页查询
        Integer pageNo = (Integer) searchMap.get("pageNo");   //当前页码
        if (pageNo == null) {
            pageNo = 1;
        }
        Integer pageSize = (Integer) searchMap.get("pageSize");  //每页显示记录数
        if (pageSize == null) {
            pageSize = 20;
        }

        query.setOffset((pageNo - 1) * pageSize);//起始记录数
        query.setRows(pageSize);//每页查询记录数

        //根据排序查询
        String sortValue = (String) searchMap.get("sortValue");//排序规则   ASC   DESC
        String sortField = (String) searchMap.get("sortField");//排序字段
        if (StringUtils.isNotEmpty(sortValue)) {
            if (sortValue.equals("ASC")) {
                Sort sort = new Sort(Sort.Direction.ASC, "item_" + sortField);
                query.addSort(sort);
            }
            if (sortValue.equals("DESC")) {
                Sort sort = new Sort(Sort.Direction.DESC, "item_" + sortField);
                query.addSort(sort);
            }
        }
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
        //6.获取高亮集合入口
        List<HighlightEntry<TbItem>> highlightEntryList = page.getHighlighted();
        for (HighlightEntry<TbItem> highlightEntry : highlightEntryList) {
            //获取基本的数据对象
            TbItem item = highlightEntry.getEntity();
            //注意：获取高亮结果之前，一定要对该集合做判空操作
            if (highlightEntry.getHighlights().size() > 0 && highlightEntry.getHighlights().get(0).getSnipplets().size() > 0) {
                List<HighlightEntry.Highlight> highlightList = highlightEntry.getHighlights();

                List<String> snipplets = highlightList.get(0).getSnipplets();
                item.setTitle(snipplets.get(0));
            }

        }

        List<TbItem> itemList = page.getContent();

        resultMap.put("rows", itemList);   //分页之后的查询集合
        resultMap.put("total", page.getTotalElements());    //总记录数
        resultMap.put("totalPages", page.getTotalPages());     //分页后总页数
        return resultMap;
    }

    //分类分组查询
    private List<String> searchCategoryList(Map<String, Object> searchMap) {
        List<String> list = new ArrayList<String>();
        //1.创建查询条件对象
        Query query = new SimpleQuery();
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //2.设置分组字段
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
        query.setGroupOptions(groupOptions);
        //3.执行分组查询
        GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
        //根据列得到分组的结果集
        GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
        //得到分组结果的入口页
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
        //得到分组的入口集合
        List<GroupEntry<TbItem>> groupEntryList = groupEntries.getContent();
        for (GroupEntry<TbItem> groupEntry : groupEntryList) {
            list.add(groupEntry.getGroupValue());
        }
        return list;
    }

    //在缓存中读取品牌和规格列表 入参：分类名称
    private Map<String, Object> searchBrandAndSpecList(String category) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        //1.根据分类名称查询模板ID
        Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
        if (null != typeId) {
            //2.根据模板ID查询品牌列表
            List<Map> brandList = (List<Map>) redisTemplate.boundHashOps("brandList").get(typeId);
            resultMap.put("brandList", brandList);
            //3.根据模板ID查询规格列表
            List<Map> specList = (List<Map>) redisTemplate.boundHashOps("specList").get(typeId);
            //4.将品牌和规格列表放入到map里
            resultMap.put("specList", specList);
        }
        return resultMap;
    }


}
